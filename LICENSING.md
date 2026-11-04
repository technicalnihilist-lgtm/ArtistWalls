# Licensing and attribution policy

ArtistWalls shows photographs of real people. Getting this right matters more than any
other part of the app, so the rules are enforced in code, not in a checklist.

## 1. Where images come from

Wikimedia Commons only — `https://commons.wikimedia.org/w/api.php`, the official API, with
a descriptive `User-Agent`. No website scraping, no HTML parsing, no access-restriction
bypassing, no third-party wallpaper sites, no search-engine image results.

Commons is queried for a search term, and the metadata for each hit is read from
`prop=imageinfo&iiprop=url|size|mime|extmetadata`, which returns the licence
(`LicenseShortName`, `LicenseUrl`, `UsageTerms`), the creator (`Artist`, `Credit`,
`Attribution`), the description and the original file page.

## 2. The gate — `LicensePolicy.isAccepted()`

Accepted:

| Licence | Notes |
| --- | --- |
| Public domain (`Public domain`, `PD-*`, Public Domain Mark, `No restrictions`) | free reuse |
| CC0 / CC Zero | free reuse |
| CC BY 1.0 – 4.0, any jurisdiction port | attribution only |
| CC BY-SA 1.0 – 4.0, any jurisdiction port | attribution + ShareAlike |

Refused, always:

- Non-free / fair-use files, press-kit photographs, promotional handouts
- Album covers and other copyrighted artwork without permission
- Files with an empty, missing or unrecognised licence
- `-NC` (NonCommercial) terms — incompatible with an app distributed commercially
- `-ND` (NoDerivatives) terms — kept out because the app cannot guarantee that no
  derivative will ever be produced from a scaled or cropped copy
- GFDL-only files (not a free image licence on Commons)
- Any file whose MIME type is not `image/jpeg`, `image/png` or `image/webp`

A file that fails any check is dropped in `CommonsMapper.toWallpaper()` before it can
reach a feed, a cache, Room or the UI. There is no "override" path in the app.

## 3. What is shown for every wallpaper

`Wallpaper.credit` is a required field, so a photograph cannot be displayed without:

1. **Artist name** (the subject the feed was queried for)
2. **Image title** — the Commons file name
3. **Photographer / creator** — `Artist`, falling back to `Credit`, then `Attribution`
4. **Licence name** and a clickable **licence URL**
5. **Original source link** — the Commons file page (`descriptionurl`)
6. **Modification note** — see below
7. Description and original date when Commons provides them

This block appears on the wallpaper detail screen (expandable "Credits & licence"), as a
list at the bottom of every artist page, on the dedicated **Credits** screen, and in the
text caption attached when a wallpaper is shared.

## 4. Modification statement

The app does not edit photographs. It displays the original file and, for lists, the
thumbnail that Wikimedia's own servers generate (`iiurlwidth=1200`). Nothing is cropped,
recoloured, upscaled or composited by ArtistWalls, and no watermark or logo is added.

The wording shown to users:

> Unmodified. Original file shown as published on Wikimedia Commons; the smaller preview
> is scaled by Wikimedia's thumbnail servers, not by ArtistWalls. (ShareAlike files add:
> Shared under the same licence.)

Android's own wallpaper cropper may let the *user* choose a crop on their device; that is
a device-side display choice, and the app does not redistribute the cropped result.

## 5. ShareAlike

CC BY-SA files are shared unmodified under the same licence, with a link to the licence
deed and to the file page. Anyone receiving the image through the share sheet gets the
credit text with it. If you later modify a CC BY-SA file distributed through this app,
you must release your version under the same licence.

## 6. Empty states instead of substitutes

When Commons has no freely licensed photograph of an artist, the gallery shows an
explicit empty state:

> Wikimedia Commons currently holds no freely licensed photograph of <artist>. Rather
> than use a press image without permission, this gallery stays empty until a licensed
> photograph is published.

This is a feature, not a bug. It is also self-healing: because the app queries live, the
gallery fills in by itself once a licensed photograph is uploaded.

## 7. Ownership

ArtistWalls does not own the artist photographs, does not license them to anyone, and
does not imply endorsement by the artists or the photographers. The app is a browser and
a wallpaper setter over freely licensed media; every right stays with the creator.

## 8. Reporting an image

If you are a rights holder and believe a file should not be listed, the correct first
step is the Commons file page linked in the credit — Commons hosts the file and handles
takedowns there. Removing a file on Commons removes it from ArtistWalls the next time the
feed is queried, with no app update required.

## 9. Tooling

`tools/verify_licenses.py` re-runs the app's queries and the app's policy from the command
line, printing the creator and licence of each accepted photograph. Use it before adding
an artist and as a periodic audit.
