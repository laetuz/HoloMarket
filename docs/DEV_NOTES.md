# HoloMarket — Dev Notes

Open items, known limitations, and environment quirks. Not a spec — living notes for the dev machine.

## MVP migration (`feature/<name>/{contract,domain,presenter,ui}`)

Convention + rules: see `ARCHITECTURE.md`. Tick each feature once migrated.

- [x] detail
- [x] auth (login, register, forgotpassword)
- [ ] applist
- [ ] category
- [ ] settings
- [ ] home (MainActivity)

> Remove this section once every feature is migrated.

### For me to check
- **Shared components** — `InfiniteAppsAdapter` / `SectionListBuilder` are still in `ui/components/`; decide whether to relocate them to a shared `common/` package (after `home` + `category` migrate).
- **Known fixes to evaluate later** (kept as-is for now) — public `GET /collections/organizer`, server-driven `GET /categories` (see TODO below).

## Trackball / D-pad navigation gaps

The hardware-input pass (v1.4.3) made section rows, screenshots, "Read more", the preview-close button, featured-carousel cards, and top-bar buttons focusable with an orange `bg_focusable` selector. Remaining edges rely on the framework's directional heuristic and are **not yet verified on device**:

- **Duplicate-id rows** — screenshot `ImageView`s (`iv_screenshot`), section rows (`item_section` root), and version rows all reuse the same id across siblings, so id-based `nextFocus*` can't be used; left/right/up/down between them is left to the default heuristic.
- **Featured Gallery ↔ section list** — transitions between a `Gallery` carousel and the vertical section list have no explicit `nextFocus*` (duplicate `gallery_featured` ids across headers).
- **Auth forms** — Login / Register / Forgot pad flow (field→field→button) is untested; the vertical chain should work, but the password eye-drawable / `inputType` behavior on a pad is unverified.
- **Screenshot preview dialog** — `gallery_preview` ↔ `iv_close_preview` are wired; verify center-press on a screenshot actually opens the preview from the pad.

## Build environment

- **Root `build.gradle` is intentionally uncommitted** (env-specific). It replaces dead `jcenter()` with:
  - `mavenCentral()`
  - `maven { url 'https://maven.aliyun.com/repository/jcenter' }` (AGP 2.3.2 exists only here — 404 on Maven Central / Google Maven)
  - `maven { url 'https://maven.google.com' }`
- `.idea/gradle.xml` `gradleJvm` points at **Corretto 8** (`.../corretto-1.8.0_462/Contents/Home`). CLI builds need `export JAVA_HOME=<that path>` — Gradle 3.5 won't run on Java 21.
- `gradle/wrapper/gradle-wrapper.properties` timestamp comment is likewise env-specific.

## Device install notes

- **Sideload over the existing install** (do **not** uninstall) to preserve the login token — `AuthManager` keeps the JWT in `SharedPreferences`, which only survives an in-place upgrade.
- CrashCatcher now stamps crashes with `versionCode` and ignores stale-version logs, so you no longer need to uninstall to clear an old crash dialog.

## API-level caveats

- Manifest `minSdkVersion` is **3** (Android 1.5+), but `android.util.Base64` is **API 8**. `AuthManager.getUsernameFromToken()` guards it with `try/catch`, so it degrades rather than crashing on API 3–7 — but don't add new API-8+ calls without a similar guard.
- The real crash-history culprit was `ImageButton.setColorFilter(int)` (API 16). New icon tinting must use `Drawable.setColorFilter(int, PorterDuff.Mode)` (API 1).

## App detail (MVP)

- `AppDetailPresenter` owns the `GET /apps/{pkg}` fetch/parse (`AppDetailModel.fromJson`), the `InstallState` computation (PackageManager installed vs latest version), and the download flow (`DownloadTask` + `download` analytics). `AppDetailActivity` implements `AppDetailView` and only renders.
- Package-private I/O seams (`requestAppDetail` / `getInstalledVersionCode` / `startDownload`) let `AppDetailPresenterTest` stub them synchronously (no `ApiTask`/`DownloadTask`).
- Download file naming: falls back to `update_v<versionCode>.apk` when the URL basename isn't a `.apk`.
- Behavior change: a JSON parse failure now surfaces via the standard error dialog (`showLoadError`) instead of a Toast.

## Auth (MVP)

- `feature/auth/{login,register,forgotpassword}`, each `{contract,presenter,ui}`; `login` also has `domain/LoginResult`.
- Presenters own validation, network (seams `requestLogin`/`requestUsername`/`requestRegister`/`requestForgot`), `AuthManager` writes, and analytics; Activities only render.
- `LoginPresenter` builds `AuthManager` from `Context` (same pattern as detail/ratings).

## Ratings / reviews

- Implemented in `AppDetailActivity` as a section before "Version History". Endpoints: `GET/POST/DELETE /apps/{pkg}/reviews` (see `../AGENTS.md` → "Review endpoints").
- **Logic/state/network live in `RatingsPresenter` (MVP)**; `AppDetailActivity` implements `RatingsView` and only renders. The presenter exposes package-private I/O seams (`requestReviews`/`requestSubmit`/`requestDelete`) so `RatingsPresenterTest` can stub them and run synchronously (no `ApiTask`).
- **Unit tests need `org.json:json:20140107`** as a `testCompile` dep — the `android.jar` `org.json` is a stub in local JVM tests.
- **Own-review detection matches `username`** (`AuthManager.getUsername()` / `getUsernameFromToken()`) against the `username` field of `GET /reviews` — there is no "my review" endpoint. If the stored username ever diverges from the review's, the app won't find the user's review.
- **Submit fires on `RatingBar` `fromUser` changes** (tap / D-pad step). An `isSubmitting` guard blocks overlapping requests, but rapid D-pad stepping can still fire a couple of POSTs.
- Logged-out users get a read-only (`isIndicator`) star showing `average_rating`; the summary comes from the app-detail response (no extra call).

## TODO / Planned

- **Public organizer** — `MainActivity` still calls the Bearer-gated `/admin/collections/organizer`; the public `/collections/organizer` exists and would work logged-out.
- **Server-driven top-level categories** — `MainActivity` hardcodes Applications/Games/Adult instead of `GET /categories`.
