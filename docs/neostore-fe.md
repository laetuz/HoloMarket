# neostore-fe

## 1. Client Identity
- App: neostore-fe
- Version: v0.0.20
- Target Protocol: v1.1
- Target Audience: Legacy Android Devices (Donut / Eclair / Froyo / Gingerbread)
- Role: The native portal to browse and install ecosystem APKs.

## 2. Technology Stack & Constraints
- Minimum API: `minSdkVersion 3` (Android 1.6)
- Target API: `targetSdkVersion 21` (Lollipop)
- Language: Java 7
- Network: `HttpURLConnection`, `AsyncTask`, `org.json` (Strictly NO Retrofit/OkHttp)
- UI: Native `ListView` with ViewHolder pattern (Strictly NO RecyclerView/AppCompat-v7)

## 3. Strict Legacy Development Rules (The "Gotchas")
To prevent instant force-closes on API 7, all code must adhere to these historical constraints:
1. String Checking: Never use `String.isEmpty()`. Always use `TextUtils.isEmpty(string)` or `string.length() > 0`.
2. ListView Footers: `listView.addFooterView()` MUST be called strictly *before* `listView.setAdapter()`.
3. Memory Management: `ListView` adapters must implement the `convertView.setTag(viewHolder)` recycling pattern to prevent OutOfMemory crashes on 512MB RAM devices.
4. Downloads: Use the custom `DownloadTask` to stream APKs directly to `Environment.getExternalStorageDirectory()`, followed by an `Intent.ACTION_VIEW` to trigger the system Package Installer.
2. No Silent Installs: Legacy apps lack system-level root permissions. Neostore cannot install APKs in the background.
3. The Install Intent:
- Step 1: Download the APK to the device's external storage (`Downloads` folder).
- Step 2: Fire an `ACTION_VIEW` intent with the `application/vnd.android.package-archive` MIME type.
- Step 3: This hands control to the Android OS Package Installer, which prompts the user to confirm the installation.


## 4. Current Focus (Milestone 0.2: Authentication & Navigation)
*Objective: Allow users to log in without relying on modern ActionBars.*
- [x] Redesigned homepage, with section lists to know which page we are going.
- [x] Build `LoginActivity.java` UI.
- [x] Create `AuthManager` to handle `SharedPreferences` JWT storage.
- [x] Wire up POST request to `neoserver/auth` endpoint.

## 5. Backlog (Upcoming Milestones)
- [x] Milestone 0.3 (Media): Integrate `Universal Image Loader` (v1.9.5) to fetch `icon_url` from the feed.
- [x] Milestone 0.4 (Updates): Implement "Self-Update" check on app launch hitting `/apps/id.neotica.neostore/latest`.
- [x] Milestone 1.0: Final UI Polish & Release.

## 6. Completed Features
- [x] Global Crash Catcher utility mapped to SharedPreferences.
- [x] `/feed` API integration with `AppModel` mapping.
- [x] Explicit Pagination ("Load More" footer button).
- [x] App Detail Page with nested Version History list.
- [x] SD Card Downloader with auto-install trigger.
- [x] Paginated feeds.