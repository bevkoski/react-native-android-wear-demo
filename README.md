# React Native-Android Wear Communication Demo

Showcase of an established two-way communication between a React Native app and an Android Wear app using the [MessageAPI](https://developers.google.com/android/reference/com/google/android/gms/wearable/MessageApi).

## Running from Android Studio

### Clone the repository

`git clone https://github.com/bevkoski/react-native-android-wear-demo.git`

`cd react-native-android-wear-demo`

### Install dependencies

`yarn` or `npm install`

### Start the packager

`react-native start`

### Open the project in Android Studio

1. Start Android Studio
2. Choose "Open an existing Android Studio project"
3. Select the `/react-native-android-wear-demo/android` folder

### Run the mobile app

1. Connect your Android phone via USB
2. Select the `app` module as a run configuration
3. Run the `app` module
4. Select your phone from the available connected devices

If you get one of the following error messages:

**Could not connect to development server.**

**Could not get BatchedBridge, make sure your bundle is packaged properly.**

Try executing `adb reverse tcp:8081 tcp:8081` from the command line and reloading the app.

### Run the watch app

1. Connect your Android watch via USB
2. Select the `watch` module as a run configuration
3. Run the `watch` module
4. Select your watch from the available connected devices
