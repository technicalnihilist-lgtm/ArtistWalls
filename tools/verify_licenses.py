#!/usr/bin/env python3
"""
ArtistWalls — live licence verifier.

Runs the same Wikimedia Commons queries the Android app runs, applies the same
licence policy, and prints exactly which photographs the app would display.

Usage:
    python3 tools/verify_licenses.py                 # all artists + collections
    python3 tools/verify_licenses.py --artist "Arijit Singh"
    python3 tools/verify_licenses.py --json report.json

No third-party packages required (Python 3.9+).
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor

API = "https://commons.wikimedia.org/w/api.php"
USER_AGENT = (
    "ArtistWalls-verify/1.0 (licence audit; contact: nihaonihao54059@gmail.com)"
)

# --- the same catalogue the app ships -----------------------------------------
INDIAN = [
    "Arijit Singh", "Shreya Ghoshal", "Sonu Nigam", "Armaan Malik", "Jubin Nautiyal",
    "Atif Aslam", "KK (singer)", "Mohit Chauhan", "Vishal Mishra", "Neha Kakkar",
    "Sunidhi Chauhan", "Kumar Sanu", "Udit Narayan", "Alka Yagnik", "Asha Bhosle",
    "Lata Mangeshkar",
]
INTERNATIONAL = [
    "Michael Jackson", "Taylor Swift", "Ed Sheeran", "Justin Bieber", "Adele",
    "Bruno Mars", "The Weeknd", "Billie Eilish", "Eminem", "Rihanna", "Beyoncé",
    "Freddie Mercury", "Elvis Presley", "John Lennon", "Madonna",
]
COLLECTIONS = {
    "Bollywood Singers": "Bollywood playback singer filetype:bitmap",
    "Classical Musicians": 'incategory:"Classical musicians" filetype:bitmap',
    "Music Composers": 'incategory:"Composers" filetype:bitmap',
    "Guitarists": 'incategory:"Guitarists" filetype:bitmap',
    "Pianists": 'incategory:"Pianists" filetype:bitmap',
    "Drummers": 'incategory:"Drummers" filetype:bitmap',
    "Rock Bands": 'incategory:"Rock music groups" filetype:bitmap',
    "Pop Artists": 'incategory:"Pop musicians" filetype:bitmap',
    "Legendary Artists": "legendary musician portrait filetype:bitmap",
    "Female Artists": 'incategory:"Female vocalists" filetype:bitmap',
    "Male Artists": 'incategory:"Male vocalists" filetype:bitmap',
    "Live Performers": 'incategory:"Live performances" filetype:bitmap',
}

TAG = re.compile(r"<[^>]+>")
SPACES = re.compile(r"\s+")
ACCEPTED_MIME = {"image/jpeg", "image/png", "image/webp"}
MIN_WALLPAPER_WIDTH = 1000          # mirrors CommonsMapper.MIN_WALLPAPER_WIDTH
REJECT = ("non-free", "nonfree", "fair use", "fairuse", "all rights reserved",
          "copyright", "no license", "unknown", "gfdl", "permission")
NC = ("-nc", " nc ", "nc-", "noncommercial", "non-commercial")
ND = ("-nd", " nd ", "nd-", "noderiv", "no derivative")


def clean(raw: str | None) -> str:
    if not raw:
        return ""
    text = TAG.sub(" ", raw)
    for a, b in (("&nbsp;", " "), ("&amp;", "&"), ("&quot;", '"'), ("&#039;", "'"),
                 ("&lt;", "<"), ("&gt;", ">")):
        text = text.replace(a, b)
    return SPACES.sub(" ", text).strip()


def licence_accepted(name: str | None) -> bool:
    """Identical logic to LicensePolicy.isAccepted()."""
    n = (name or "").strip().lower()
    if not n:
        return False
    if any(marker in n for marker in REJECT):
        return False
    if any(marker in n for marker in NC) or any(marker in n for marker in ND):
        return False
    if n.startswith("public domain") or n == "pd" or n.startswith("pd-") \
            or n == "public domain mark 1.0" or n == "no restrictions":
        return True
    if n.startswith("cc0") or n.startswith("cc-0") or n.startswith("cc zero"):
        return True
    if n.startswith("cc by") or n.startswith("cc-by") or n.startswith("attribution"):
        return True
    return False


def query(params: dict) -> dict:
    params = {**params, "format": "json", "formatversion": "2"}
    url = API + "?" + urllib.parse.urlencode(params)
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=45) as response:
        return json.load(response)


def inspect(query_string: str, limit: int = 20) -> dict:
    payload = query({
        "action": "query", "generator": "search", "gsrsearch": query_string,
        "gsrnamespace": "6", "gsrlimit": str(limit), "prop": "imageinfo",
        "iiprop": "url|size|mime|extmetadata", "iiurlwidth": "1200",
    })
    pages = payload.get("query", {}).get("pages", [])

    accepted, rejected = [], []
    for page in pages:
        info = (page.get("imageinfo") or [None])[0]
        if not info:
            continue
        meta = info.get("extmetadata", {})
        licence = clean(meta.get("LicenseShortName", {}).get("value"))
        mime = (info.get("mime") or "").lower()
        width = info.get("width") or 0
        record = {
            "title": page.get("title", ""),
            "width": width,
            "height": info.get("height") or 0,
            "mime": mime,
            "licence": licence,
            "licenceUrl": meta.get("LicenseUrl", {}).get("value", ""),
            "author": clean(meta.get("Artist", {}).get("value"))
                      or clean(meta.get("Credit", {}).get("value"))
                      or "Wikimedia Commons contributor",
            "source": info.get("descriptionurl", ""),
            "hd": width >= MIN_WALLPAPER_WIDTH,
        }
        if mime in ACCEPTED_MIME and width > 0 and licence_accepted(licence):
            accepted.append(record)
        else:
            rejected.append(record)

    return {
        "query": query_string,
        "raw": len(pages),
        "accepted": len(accepted),
        "hd": sum(1 for item in accepted if item["hd"]),
        "sample": accepted[:3],
        "rejected": [{"title": r["title"], "licence": r["licence"]} for r in rejected[:3]],
    }


EXTRA_CANDIDATES = {
    "KK (singer)": ["Krishnakumar Kunnath filetype:bitmap", "KK singer playback filetype:bitmap"],
}


def artist_chain(name: str) -> list[str]:
    """Same candidate order as ArtistCatalog: plain search first, then category."""
    return ([f"{name} filetype:bitmap"]
            + EXTRA_CANDIDATES.get(name, [])
            + [f'incategory:"{name}" filetype:bitmap', f"{name} singer filetype:bitmap"])


def run_one(entry: tuple[str, list[str]]) -> tuple[str, dict]:
    """Mirrors the app: try each candidate until one returns licensed images."""
    label, candidates = entry
    for query_string in candidates:
        try:
            result = inspect(query_string)
        except Exception as error:                  # noqa: BLE001 - report, do not crash
            return label, {"error": str(error)}
        if result["accepted"] > 0:
            return label, result
    return label, {"query": candidates[0], "raw": 0, "accepted": 0, "hd": 0,
                   "sample": [], "rejected": []}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--artist", help="check a single artist name")
    parser.add_argument("--json", help="write the full report to this path")
    parser.add_argument("--limit", type=int, default=20)
    args = parser.parse_args()

    if args.artist:
        jobs = [(args.artist, artist_chain(args.artist))]
    else:
        jobs = [(name, artist_chain(name)) for name in INDIAN + INTERNATIONAL]
        jobs += [(name, [q]) for name, q in COLLECTIONS.items()]

    report = {}
    with ThreadPoolExecutor(max_workers=4) as pool:
        for label, result in pool.map(run_one, jobs):
            report.setdefault(label, []).append(result)

    summary = {label: results[0] for label, results in report.items()}

    print(f"{'ARTIST / COLLECTION':<24}{'RAW':>5}{'LICENSED':>10}{'HD':>5}   SAMPLE CREDIT")
    print("-" * 110)
    for label, data in summary.items():
        if "error" in data:
            print(f"{label:<24}{'ERR':>5}  {data['error'][:60]}")
            continue
        sample = data["sample"][0] if data["sample"] else None
        credit = (f"{sample['author'][:28]} · {sample['licence']}" if sample else "— none —")
        print(f"{label:<24}{data['raw']:>5}{data['accepted']:>10}{data['hd']:>5}   {credit}")

    empty = [k for k, v in summary.items() if v.get("accepted", 0) == 0]
    print()
    print(f"Feeds with no licensed photograph (app shows an empty state): {len(empty)}")
    for name in empty:
        print(f"  · {name}")

    if args.json:
        with open(args.json, "w", encoding="utf-8") as handle:
            json.dump(summary, handle, indent=2, ensure_ascii=False)
        print(f"\nFull report written to {args.json}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
