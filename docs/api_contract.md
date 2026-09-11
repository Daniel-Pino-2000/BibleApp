# BibleApp API Contract

**Status:** Draft v1 — covers **Notes** and **Highlights** only.
**Not yet covered:** Auth (`/api/v1/auth/...`). Every endpoint below assumes a JWT issued by
that (still-to-be-designed) flow; see [Ktor_Backend_Contract_And_Roadmap.pdf](Ktor_Backend_Contract_And_Roadmap.pdf)
Section 1 for the outline. This file gets an `Auth` section appended in the next pass.

This is a living document — update it *before* changing a route, not after.

---

## Conventions (apply to every endpoint below)

- **Base path:** `/api/v1`
- **Auth:** every endpoint requires `Authorization: Bearer <token>` unless marked `Auth: none`.
  All Notes/Highlights endpoints are implicitly scoped to the authenticated user — nobody can
  read, edit, or delete another user's data, and the user is never passed as a request field
  (it comes from the JWT).
- **IDs:** server-generated UUID strings.
- **Timestamps:** ISO-8601 strings, UTC (e.g. `2026-09-11T14:32:00Z`) — never epoch millis.
- **Errors:** every non-2xx response uses the shared `ErrorResponse` shape (below).

---

## Design decisions specific to Notes & Highlights

These are the calls made during contract design, written down so they don't get re-litigated
per-endpoint later — see [Section 1 of the roadmap PDF](Ktor_Backend_Contract_And_Roadmap.pdf)
for why this step happens before code.

1. **A highlight or note covers an explicit list of verses** (`verses: List<VerseLocation>`),
   not a range. This matches the actual UX directly — the user taps/selects some verses (a
   contiguous passage, a couple of scattered cross-references, whatever) and saves them as one
   highlight or one note, which then shows up as a single card in the user's library. There's no
   separate "range" concept to reconcile with "list of verses" — a contiguous selection is just a
   list that happens to be consecutive.
2. **Verses may span multiple chapters or books.** The API doesn't restrict a highlight/note to a
   single chapter — each entry in `verses` carries its own `bookId`/`chapter`/`verse`. Most
   highlights will probably stay within one passage in practice, but nothing in the contract
   enforces that, since there's no real cost to allowing it and an artificial restriction would
   just get lifted later anyway.
3. **`versionId` lives on the highlight/note itself, not per verse.** A highlight is anchored to
   the translation the user was reading in — verse numbering (versification) can differ between
   translations, so mixing versions within one highlight isn't meaningful. If the user switches
   translations, that's a new highlight.
4. **Display order is a client concern.** The server doesn't guarantee `verses` comes back in any
   particular order (selection order, insertion order, etc. are all implementation details). If
   the library card needs to show verses in Bible order, sort client-side by
   `(bookId, chapter, verse)`.
5. **Deletions are soft, not hard** (`deletedAt: String?` on every resource). This is a sync API:
   if device A deletes a highlight and device B only ever sees "it's gone from the list," device
   B can't tell the difference between "deleted" and "server hasn't sent it yet." A plain `GET`
   returns only active items; passing `updatedSince` also returns tombstones for anything
   changed (including deleted) since that time, so a syncing client can reconcile its local copy.
6. **Highlights are recolor-only after creation; Notes allow editing both text and the verse
   list.** Recoloring is the only realistic post-creation edit for a highlight (if the selection
   was wrong, delete and recreate — cheap, since it's one call either way). Notes are more often
   revised from a dedicated editor screen, where re-picking which verses the note covers is a
   reasonable edit, not just a fix-up.
7. **No conflict resolution beyond last-write-wins.** Fine for one person syncing across their
   own devices. Would need real thought (versioning, merge rules) if this ever became
   multi-user/collaborative — not a v1 concern.
8. **No pagination yet**, but filtering semantics matter now that verses are a list: `GET
   .../?bookId=&chapter=` matches a highlight/note if **any** entry in its `verses` list is in
   that book/chapter — not an exact single-location match like before. Revisit with real
   pagination if a user's total note/highlight count ever makes an unfiltered `GET` expensive.
9. **Server does not validate verse locations against the real verse count for that
   version/chapter** (e.g. that "Genesis 1:35" doesn't exist). The client already has this data
   (`ChapterStructure`) and is trusted to send valid locations. Documented gap, not an oversight.
10. **`verses` must be non-empty**, and is capped at a sane maximum (e.g. 500 entries) as a
    payload sanity guard, not a real UX limit — returned as `VALIDATION_ERROR` if violated.

---

## Shared types

```kotlin
@Serializable
data class VerseLocation(
    val bookId: Int,   // 1..66, standard canon numbering
    val chapter: Int,
    val verse: Int
)

@Serializable
data class ErrorResponse(
    val code: String,                        // e.g. "VALIDATION_ERROR", "NOT_FOUND"
    val message: String,                     // human-readable, safe to show or log
    val fieldErrors: Map<String, String>? = null
)
```

**Error codes used below:**

| Code | Status | Meaning |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Malformed body — e.g. empty or oversized `verses`, blank `versionId`/`text`, a `bookId` outside 1..66 |
| `UNAUTHORIZED` | 401 | Missing, invalid, or expired token |
| `NOT_FOUND` | 404 | No resource with that id — **also returned when the id belongs to another user**, so ownership is never leaked by a 403 |

---

## Highlights

A highlight = a set of verses (in one version) + a color. The verse list is immutable after
creation; only the color can be changed.

```kotlin
@Serializable
data class CreateHighlightRequest(
    val versionId: String,
    val verses: List<VerseLocation>,   // non-empty, see decision 10
    val color: Int                     // ARGB, same convention as UserVerse.highlightColor in the app today
)

@Serializable
data class UpdateHighlightRequest(
    val color: Int   // the only editable field — see decision 6 above
)

@Serializable
data class HighlightResponse(
    val id: String,
    val versionId: String,
    val verses: List<VerseLocation>,
    val color: Int,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class HighlightListResponse(
    val highlights: List<HighlightResponse>
)
```

### `GET /api/v1/highlights`
Auth: required
Query params: `bookId` (optional Int), `chapter` (optional Int, requires `bookId`), `updatedSince` (optional ISO-8601 — includes soft-deleted tombstones when present)
Success: `200 OK` → `HighlightListResponse` (active highlights only, unless `updatedSince` is set; see decision 8 for filter semantics)
Errors: `401 UNAUTHORIZED`

### `POST /api/v1/highlights`
Auth: required
Request: `CreateHighlightRequest`
Success: `201 Created` → `HighlightResponse`
Errors: `400 VALIDATION_ERROR`, `401 UNAUTHORIZED`

### `PATCH /api/v1/highlights/{id}`
Auth: required
Request: `UpdateHighlightRequest`
Success: `200 OK` → `HighlightResponse`
Errors: `400 VALIDATION_ERROR`, `401 UNAUTHORIZED`, `404 NOT_FOUND`

### `DELETE /api/v1/highlights/{id}`
Auth: required
Success: `204 No Content` (soft delete — sets `deletedAt`; see decision 5)
Errors: `401 UNAUTHORIZED`, `404 NOT_FOUND`

---

## Notes

A note = a set of verses (in one version) + free text. Unlike highlights, both the text and the
verse list can be edited in place (decision 6).

```kotlin
@Serializable
data class CreateNoteRequest(
    val versionId: String,
    val verses: List<VerseLocation>,   // non-empty, see decision 10
    val text: String
)

@Serializable
data class UpdateNoteRequest(
    val verses: List<VerseLocation>,   // stays within the same versionId it was created with;
    val text: String                   // to move a note to a different version, delete and recreate
)

@Serializable
data class NoteResponse(
    val id: String,
    val versionId: String,
    val verses: List<VerseLocation>,
    val text: String,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String? = null
)

@Serializable
data class NoteListResponse(
    val notes: List<NoteResponse>
)
```

### `GET /api/v1/notes`
Auth: required
Query params: `bookId` (optional Int), `chapter` (optional Int, requires `bookId`), `updatedSince` (optional ISO-8601)
Success: `200 OK` → `NoteListResponse` (active notes only, unless `updatedSince` is set; see decision 8 for filter semantics)
Errors: `401 UNAUTHORIZED`

### `GET /api/v1/notes/{id}`
Auth: required
Success: `200 OK` → `NoteResponse`
Errors: `401 UNAUTHORIZED`, `404 NOT_FOUND`
*(No equivalent single-highlight GET: highlights are always acted on from the already-loaded
list in the reading view. Notes may be opened in a dedicated editor navigated to independently,
so they need a direct fetch.)*

### `POST /api/v1/notes`
Auth: required
Request: `CreateNoteRequest`
Success: `201 Created` → `NoteResponse`
Errors: `400 VALIDATION_ERROR`, `401 UNAUTHORIZED`

### `PUT /api/v1/notes/{id}`
Auth: required
Request: `UpdateNoteRequest`
Success: `200 OK` → `NoteResponse`
Errors: `400 VALIDATION_ERROR`, `401 UNAUTHORIZED`, `404 NOT_FOUND`

### `DELETE /api/v1/notes/{id}`
Auth: required
Success: `204 No Content` (soft delete)
Errors: `401 UNAUTHORIZED`, `404 NOT_FOUND`

---

## Open items for the next contract pass

- Auth endpoints (`register`/`login`/`refresh`) — outlined in the roadmap PDF, not finalized here.
- Whether highlight `color` needs validation against a fixed palette vs. accepting any ARGB int.
- Whether verse locations should eventually be validated server-side against real per-version
  verse counts (see decision 9) — needs the server to hold each version's `ChapterStructure`.
- Whether `GET /highlights` needs a single-resource `GET /highlights/{id}` once/if a highlight
  detail view is added.
- How `verses` is persisted server-side (a `jsonb` column vs. a child table joined to the
  highlight/note) is a Phase 3 storage decision — it doesn't change this contract either way.
