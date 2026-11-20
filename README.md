# LiveCall AI

Native Kotlin Android project for a private, universal phone-call assistant.

## What is implemented

- Cellular call state receiver: starts a foreground microphone service when a call is answered and stops it when the call ends.
- Draggable floating overlay with caller transcript, Roman Urdu meaning, and short English suggested reply.
- Continuous Android `SpeechRecognizer` loop with explicit unavailable/error states.
- Configurable OpenAI-compatible chat-completions endpoint, model, API key, recognition language, and local-history preference.
- Context-aware assistant prompt that is topic-agnostic and refuses to invent personal facts.
- Speakerphone + microphone fallback, with no fake audio or fabricated transcription.
- Runtime permissions, overlay permission flow, foreground notification, and privacy disclosure.
- Optional notification-listener entry point for WhatsApp call popups. This detects a call notification only; it cannot access WhatsApp's protected audio.

## Important Android limitation

Normal third-party apps cannot reliably capture the far-end audio stream of a cellular call on modern Android devices. This app does not bypass that restriction, root the device, or pretend that audio was received. When the device/provider does not route call audio to the microphone, recognition reports an unavailable/error state. For best-effort operation, grant microphone and overlay permission and enable speakerphone during the call. Device/OEM behavior varies.

## Build

Requirements: JDK 17+, Android SDK 35, and Gradle 8.9+ (or generate a wrapper with Android Studio).

```powershell
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

This workspace did not contain Android SDK/Java/Gradle/ADB at generation time, so the APK could not be compiled or device-tested here. Open the project in Android Studio, sync, build, and test on a physical device. Configure the endpoint and API key in the app before a real call.

## Build without Android Studio

The included `.github/workflows/build-apk.yml` can build the debug APK on GitHub's hosted runner. Upload this project to a GitHub repository, open the **Actions** tab, run **Build LiveCall AI APK**, then download the `LiveCallAI-debug-apk` artifact from the completed run and extract the APK on your phone.

## Security notes

API keys are stored in app-private SharedPreferences for this starter build. For production distribution, replace this with Android Keystore-backed encryption and a streaming backend that avoids putting provider secrets on-device. Do not enable call recording without separate legal/privacy review and user consent.
