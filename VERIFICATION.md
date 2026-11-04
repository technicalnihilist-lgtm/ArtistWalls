# Verification report

Everything below was produced by running the app's own queries and licence rules against the
live Wikimedia Commons API (`tools/verify_licenses.py`), not from memory or documentation.

**Result: all 43 feeds (31 artists + 12 collections) return freely licensed photographs.**
In the first page of results alone that is **760 accepted photographs**, of which
**419 are 1000 px or wider** (full wallpaper quality); the rest are licensed but small and
are labelled LOW-RES in the UI. Feeds with no licensed photograph — the empty-state case — 
numbered **0** in this run.

## 1. Build verification

| Check | Result |
| --- | --- |
| Kotlin compilation (`:app:compileDebugKotlin`) | **BUILD SUCCESSFUL** |
| Room code generation (KSP) | **BUILD SUCCESSFUL** |
| Resource processing (`:app:processDebugResources`) | **BUILD SUCCESSFUL** |
| Compile errors found during the first pass | 5, all fixed (serialization converter package, `contentOrNull` property, `glass` import) |
| Source files | 43 Kotlin files |

## 2. API integration checks

| Check | Result |
| --- | --- |
| `action=query` + `generator=search`, namespace 6 | works, `formatversion=2` |
| `prop=imageinfo&iiprop=url\|size\|mime\|extmetadata` | returns URL, thumbnail, size, MIME and full licence block |
| Thumbnails | `iiurlwidth=1200` returns a Wikimedia-served preview URL |
| Pagination | `continue.gsroffset` present and advancing; consecutive pages do not repeat files |
| Search suggestions | Wikipedia `action=opensearch` returns artist names |
| `incategory:` search | works for some categories, returns 0 for others — handled by the candidate chain, plain search is tried first |
| `gsrsort=create_timestamp_desc` | returns newest music uploads (drives "Recently added") |
| `gsrsort=incoming_links_desc` | returns the most-linked music photographs (drives "Trending") |

## 3. Per-artist licence audit

Accepted = passes `LicensePolicy` and is a JPEG/PNG/WebP. HD = original is 1000 px wide or more.

### Indian singers

| Artist / collection | Accepted | HD | First credit |
| --- | --- | --- | --- |
| Arijit Singh | 20 | 12 | Itssme.jarvis · CC BY-SA 3.0 |
| Shreya Ghoshal | 18 | 3 | Bollywood Hungama · CC BY 3.0 |
| Sonu Nigam | 18 | 8 | Falak11mufc · CC BY-SA 3.0 |
| Armaan Malik | 20 | 6 | emily faulk from Saudi Arabia · CC BY 2.0 |
| Jubin Nautiyal | 6 | 0 | Bollywood Hungama · CC BY 3.0 |
| Atif Aslam | 16 | 4 | Vineethkuruvath · CC BY-SA 3.0 |
| KK (singer) | 20 | 13 | Jaison Salu John · Public domain |
| Mohit Chauhan | 9 | 3 | Bollywood Hungama · CC BY 3.0 |
| Vishal Mishra | 3 | 2 | Fatimanoor125 · CC BY-SA 4.0 |
| Neha Kakkar | 17 | 0 | Bollywood Hungama · CC BY 3.0 |
| Sunidhi Chauhan | 20 | 5 | Prometheus.pyrphoros · CC BY-SA 3.0 |
| Kumar Sanu | 19 | 4 | Bollywood Hungama · CC BY 3.0 |
| Udit Narayan | 17 | 7 | Bollywood Hungama · CC BY 3.0 |
| Alka Yagnik | 15 | 3 | Bollywood Hungama · CC BY 3.0 |
| Asha Bhosle | 15 | 3 | Aditijain · Public domain |
| Lata Mangeshkar | 20 | 1 | Bollywood Hungama · CC BY 3.0 |

### International artists

| Artist / collection | Accepted | HD | First credit |
| --- | --- | --- | --- |
| Michael Jackson | 20 | 12 | White House Photo Office · Public domain |
| Taylor Swift | 20 | 13 | minds-eye · CC BY-SA 2.0 |
| Ed Sheeran | 20 | 19 | Eva Rinaldi from Sydney, Australia · CC BY-SA 2.0 |
| Justin Bieber | 20 | 10 | Heather Sokol heathersokol.com · CC BY-SA 2.0 |
| Adele | 20 | 18 | Christopher Macsurak · CC BY 2.0 |
| Bruno Mars | 20 | 14 | Chrizta T. · CC BY 2.0 |
| The Weeknd | 20 | 18 | Fred von Lohmann · CC BY 2.0 |
| Billie Eilish | 20 | 16 | MTV UK · CC BY 3.0 |
| Eminem | 20 | 13 | Unknown artist Unknown artist · Public domain |
| Rihanna | 20 | 8 | Gemma Mary from Gympie, Australia · CC BY 2.0 |
| Beyoncé | 20 | 11 | Philip Nelson · CC BY-SA 2.0 |
| Freddie Mercury | 20 | 12 | Bernd Brägelmann · CC BY-SA 3.0 |
| Elvis Presley | 20 | 12 | Ollie Atkins, chief White House photographer at the time. See ARC reco |
| John Lennon | 19 | 13 | United Press International, photographer unknown · Public domain |
| Madonna | 19 | 17 | Raphael · Public domain |

### Other collections

| Artist / collection | Accepted | HD | First credit |
| --- | --- | --- | --- |
| Bollywood Singers | 19 | 8 | Sumitbhagwani · CC BY-SA 3.0 |
| Classical Musicians | 19 | 10 | Michael8407 · CC BY-SA 4.0 |
| Music Composers | 20 | 14 | Daviddolez · CC BY-SA 3.0 |
| Guitarists | 20 | 14 | Djangoinsane · CC BY-SA 3.0 |
| Pianists | 20 | 17 | Tyler Christopher · CC BY-SA 4.0 |
| Drummers | 20 | 14 | Wikimedia Commons contributor · CC BY-SA 2.5 |
| Rock Bands | 20 | 13 | Andrea R. Baker aka User:Immunity · CC BY-SA 2.5 |
| Pop Artists | 20 | 13 | Por Daves tone - - photography · Public domain |
| Legendary Artists | 8 | 5 | Takkk · CC BY-SA 3.0 |
| Female Artists | 20 | 15 | הזמרת שרי סולטאן · CC BY 3.0 |
| Male Artists | 20 | 13 | Uerick · Public domain |
| Live Performers | 3 | 3 | Vincenzo Facino · CC BY-SA 4.0 |

## 4. Re-running this audit

```bash
python3 tools/verify_licenses.py --json report.json
python3 tools/verify_licenses.py --artist "Adele"
```

Counts move over time on purpose: Commons gains new uploads and new licences every day, and the
app is built to pick them up without a release.
