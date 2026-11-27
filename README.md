# HoloMarket

  <img height="420" alt="2014_06_25_23 35 39" src="https://github.com/user-attachments/assets/ca842a69-bbd8-4642-88e0-b9949e61b01c" />
  <img height="420" alt="2014_06_25_23 35 51" src="https://github.com/user-attachments/assets/cba9ba15-24a5-42ab-9766-ab488d21db71" />
  <img height="420" alt="2014_06_25_23 35 51" src="https://github.com/user-attachments/assets/c5f18947-1e46-40f2-b1c2-764e9ad8b1df" />

<details>
  <summary><b>View Screenshots (Lollipop)</b></summary>
  <br>

  <img height="420" alt="2014_06_25_23 35 39" src="https://github.com/user-attachments/assets/de3d51fc-0cc6-4590-b67f-c93559e12e7c" />
  <img height="420" alt="2014_06_25_23 35 51" src="https://github.com/user-attachments/assets/4dfaf3b5-266d-4586-833d-9c7fb5f5b7f2" />
  <img height="420" alt="2014_06_25_23 35 51" src="https://github.com/user-attachments/assets/52bb2753-af6d-4102-a75f-498692b925fa" />
</details>

<!-- <details>
  <summary><b>View Screenshots [beta]</b></summary>
  <br>

  <img width="320" height="480" alt="2014_06_25_23 35 39" src="https://github.com/user-attachments/assets/3e847ebe-ead8-45dc-8afb-8b10f7f9a4f6" />
  <img width="320" height="480" alt="2014_06_25_23 35 51" src="https://github.com/user-attachments/assets/e3e6dbbe-db4f-4acb-877d-89cd7776e715" />
</details> -->

Legacy Android app store client with `minSdkVersion 3` (Android 1.5+ / API 3, Cupcake) — practically exercised on Eclair / Froyo / Gingerbread.

Built to solve the TLS/SSL certificate deprecation wall that breaks standard web browsing on 2010–2013 hardware. HoloMarket speaks raw HTTP/JSON to give early Android devices a functional, on-device package manager again.

## Features

- **Browse apps** by category with paginated feeds and collections
- **Featured apps** carousels (home + categories)
- **Search** apps by keyword, or tap a developer to list their apps
- **App detail** page with screenshots, version history, developer, and install state (Download / Update / Open)
- **Ratings & reviews** — rate an app; update or delete your own review (login required)
- **APK download** via the downloads manager (`DownloadService`) with progress notifications, cancel, and auto-install
- **Authentication** (login / register / forgot password) with JWT stored in `SharedPreferences`
- **Settings** screen with self-update check, clear cache (app + icon caches), logout, root auto-install, and 18+ content toggle
- **Trackball / D-pad navigation** — focusable rows with an orange selection highlight
- **CrashCatcher** — saves the last crash stacktrace and shows it on next launch (current build only)

## Downloads
You can just go to the [releases](https://github.com/laetuz/HoloMarket/releases) section or [Download here](https://github.com/laetuz/HoloMarket/releases/download/v1.4.5/HoloMarket.apk)

## Prerequisites

- Android Studio 2.3.2
- Android SDK 3 (minimum) / SDK 21 (target)
- JDK 8

## local.properties

Required (gitignored). Must define:

```
BASE_URL
FILE_BASE_URL
AUTH_BASE_URL
NEOMETRICS_BASE_URL
```

## Build

```sh
./gradlew assembleDebug
```

## CI / Releasing

- **CI** (`.github/workflows/ci.yml`) — builds a debug APK and runs the unit tests on pushes and pull requests to `main`/`master`. The APK is uploaded as a workflow artifact.
- **Release** (`.github/workflows/release.yml`) — runs on `v*` tags and via manual dispatch. It builds a release APK, verifies the signature, uploads an artifact, and attaches `HoloMarket-<tag>.apk` to the GitHub Release. Tags of the form `v<versionName>.<suffix>` (e.g. `v1.5.0.1`) and four-component versions (e.g. `v1.5.1.2`) are published as **prereleases**.

Both workflows inject `local.properties` from repository secrets (Settings → Secrets and variables → Actions), using bare URLs without quotes:

```
BASE_URL
FILE_BASE_URL
AUTH_BASE_URL
NEOMETRICS_BASE_URL
```

They also seed the signing keystore from the `KEYSTORE_BASE64` secret (base64 of the release keystore): the release workflow **requires** it, while CI only uses it when present (fork PRs fall back to a throwaway key). The keystore uses the standard debug credentials (`androiddebugkey` / `android`), so `assembleRelease` keeps using `signingConfig signingConfigs.debug` — but every release is signed with the same key, so users can update in place.

Release flow:

1. Bump `versionCode` / `versionName` in `app/build.gradle` for a real release.
2. Commit and push to `main`.
3. `git tag vX.Y.Z && git push origin vX.Y.Z`.
4. The workflow builds `HoloMarket-vX.Y.Z.apk` and attaches it to the GitHub Release.

> Installs signed with an older/throwaway debug key (e.g. `v1.5.1` or earlier CI builds) need a **one-time uninstall** before switching to the stable-key releases; after that, updates install over the previous version.

## License

- **Code** — GNU General Public License v3.0 (see [`LICENSE`](LICENSE)).
- **Documentation** (`docs/`) — MIT License (see [`docs/LICENSE`](docs/LICENSE)), so the architecture guide stays reusable.
- **Third-party components** — see [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES).
- **Brand** — "HoloMarket" / "Neotica.id" names and logos are trademarks of Neotica.id; see [`TRADEMARKS.md`](TRADEMARKS.md).
