# Pronunciation Coach (Android)

A personal Android prototype for practising American English pronunciation.

## Features already in the code
- Word-stress, R, TH, and vowel practice
- Stressed-syllable guides and example sentences
- American English text-to-speech with slow playback
- Microphone recording stored locally in the app cache
- Next-word navigation

## Important limitation
This first prototype does **not** yet provide pronunciation scores, automated feedback, or recording playback. These features require further development and a validated pronunciation assessment service. We will not invent scores.

## Getting started
1. Open this repository in Android Studio with JDK 17 and Android SDK 35.
2. Sync Gradle, then run the app on an Android device.
3. Grant microphone permission when prompted.
4. This repository currently does not include Gradle wrapper binaries. Generate a wrapper using Gradle 8.9 if needed (`gradle wrapper --gradle-version 8.9`).

The prototype has not yet been compiled or tested on a device.

## Next steps
- Reliable pronunciation scoring, including sounds and stress
- Ability to enter your own sentence
- Recording playback and history
- More words and adaptive practice
- Clear privacy choices for any cloud-based assessment

The repository is currently public. Never commit personal recordings or API keys.
