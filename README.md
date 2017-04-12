# Agora Customized Video Source Android Tutorial

## Project Description

This 8-step tutorial will help you get video chat integrated directly into your Android applications using the Agora.io Real Time Communications SDK. This first app includes the following features:
- Make 1:1 Video Call
- Mute
- Hang Up

## Main APIs

In this sample, we use deprecated android.hardware.Camera to capture the video frame, you can use android.hardware.camera2 instead.

some Agora RTC SDK APIs used:

- `RtcEngine.create(Context context, String appId, IRtcEngineEventHandler handler)`
- `RtcEngine.setChannelProfile(int profile)`
- `RtcEngine.enableVideo()`
- `RtcEngine.setVideoProfile(int profile, boolean swapWidthAndHeight)`
- `RtcEngine.setupRemoteVideo(VideoCanvas remote)`
- `RtcEngine.setClientRole(int role, String permissionKey)`
- `RtcEngine.joinChannel(String channelKey, String channelName, String optionalInfo, int optionalUid)`
- `RtcEngine.leaveChannel()`
- `RtcEngine.setExternalVideoSource(boolean enable, boolean useTexture, boolean pushMode)`
- `RtcEngine.pushExternalVideoFrame(AgoraVideoFrame frame)`

## Developer Environment Requirements

- Android Studio 2.0 or above
- Real devices (Nexus 5X or other devices)
- Note: Android simulator is NOT supported for rendering video, which is a requirement of this app

## Instructions

1. Create a developer account and obtain an App ID at http://www.agora.io
2. Update "app/src/main/res/values/strings.xml" with your App ID
3. Download the Android SDK from the "Getting Started" tab
4. Unzip the downloaded SDK package and copy the "*.jar" to "app/libs", "arm64-v8a"/"x86"/"armeabi-v7a" to "app/src/main/jniLibs"

## Enjoy!

This is an introduction to the Agora.io Real Communications SDK on Android. You can also find Agora.io Real Time Communications SDK for other platforms: Web, iOS, Windows, and macOS(OSX) at http://www.agora.io. From here, you can start building applications and integrate video chat.

## License

MIT
