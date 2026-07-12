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

Legacy Android app store client targeting **Android 2.1+ (API 7)** — Eclair / Froyo / Gingerbread.

Built to solve the TLS/SSL certificate deprecation wall that breaks standard web browsing on 2010–2013 hardware. HoloMarket speaks raw HTTP/JSON to give early Android devices a functional, on-device package manager again.

## Features

- **Browse apps** by category (APPLICATION, GAME) with paginated feeds
- **Featured apps** carousel on the home screen
- **Search** apps by keyword
- **App detail** page with version history, app icon, and download
- **APK download** via `DownloadTask` with progress bar and auto-install via system package installer
- **Authentication** (login / register) with JWT stored in `SharedPreferences`
- **Username** fetched from API and cached
- **Settings** screen with logout and 18+ content toggle
- **CrashCatcher** — saves last crash stacktrace and shows it on next launch

## Downloads
You can just go to the [releases](https://github.com/laetuz/HoloMarket/releases) section or [Download here](https://github.com/laetuz/HoloMarket/releases/download/v1.0.1/HoloMarket.apk)

## Prerequisites

- Android Studio 2.3.2
- Android SDK 7 (minimum) / SDK 21 (target)
- JDK 8

## local.properties

Required (gitignored). Must define:

```
BASE_URL
FILE_BASE_URL
AUTH_BASE_URL
```

## Build

```sh
./gradlew assembleDebug
```
