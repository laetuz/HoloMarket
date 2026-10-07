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
- **APK download** via `DownloadTask` with progress bar and auto-install via the system package installer
- **Authentication** (login / register / forgot password) with JWT stored in `SharedPreferences`
- **Settings** screen with self-update check, logout, and 18+ content toggle
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

## License

- **Code** — GNU General Public License v3.0 (see [`LICENSE`](LICENSE)).
- **Documentation** (`docs/`) — MIT License (see [`docs/LICENSE`](docs/LICENSE)), so the architecture guide stays reusable.
- **Third-party components** — see [`THIRD_PARTY_NOTICES`](THIRD_PARTY_NOTICES).
- **Brand** — "HoloMarket" / "Neotica.id" names and logos are trademarks of Neotica.id; see [`TRADEMARKS.md`](TRADEMARKS.md).
