# FrameX TV for Amazon Fire TV (starter project)

This is a minimal Kotlin Android WebView wrapper for `https://framextv.cc`, configured for landscape, full-screen display, Fire TV launcher visibility, and basic HTML5 fullscreen video support.

## Important limitations
- This is starter source code, not a prebuilt or tested APK.
- Playback depends on the website, its third-party embeds, codecs, DRM, and WebView compatibility.
- Remote navigation depends on the site's HTML/CSS focus handling. Some pages may need extra TV-specific focus styling or key handling.
- The website's current reputation is uncertain. Review the domain and embedded providers before using it; the app does not make website content safer.
- The app intentionally blocks non-HTTP(S) navigation and mixed content. Some external login or player flows may not work with these restrictions.

## Build requirements
- Android Studio with Android SDK Platform 35
- JDK 17
- Internet access to download Gradle and Android build dependencies

## Build APK
1. Open this folder in Android Studio.
2. Allow Gradle sync and install any prompted SDK components.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. The debug APK should appear at `app/build/outputs/apk/debug/app-debug.apk`.

You can also build from a terminal in this project folder after Android Studio has configured the SDK:
```bash
gradle assembleDebug
```
If you install the Gradle wrapper in Android Studio, prefer `./gradlew assembleDebug`.

## Install on Fire TV with ADB
1. On Fire TV, open Settings > My Fire TV (or Device & Software) > About, then select the device name repeatedly until Developer Options appear.
2. Enable ADB debugging. On versions that offer it, also enable installing unknown apps for the installer you plan to use.
3. Find the Fire TV IP address under Network.
4. On a computer on the same network with Android platform-tools installed:
```bash
adb connect FIRE_TV_IP:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Replace `FIRE_TV_IP` with the device's actual IP address. Confirm the debugging prompt on the TV.
5. Launch FrameX TV from Apps / Your Apps. Some Fire TV versions place sideloaded apps under Settings > Applications > Manage Installed Applications.

## First test checklist
- Can you move focus using all four directional buttons?
- Can you open a title and start playback with Select/OK?
- Does Back return to the previous page?
- Do subtitles, audio, and full-screen playback work?
- Do any pop-ups or external links prevent normal use?

Do not install an APK from an untrusted third party. Build from source you have reviewed.

## Build in GitHub Actions (no Android Studio needed locally)
1. Create a new GitHub repository and upload the contents of this project folder to it.
2. Open the repository's **Actions** tab and select **Build FrameX TV APK**. If prompted, enable workflows.
3. Choose **Run workflow** (or wait for the first push build to finish).
4. Open the completed workflow run and download the `FrameXTV-debug-apk` artifact. Extract the downloaded artifact ZIP to get `app-debug.apk`.
5. Install that APK on Fire TV using the ADB instructions above.

The APK artifact is a debug build and is intended for personal testing. GitHub Actions needs permission to run in your repository.
