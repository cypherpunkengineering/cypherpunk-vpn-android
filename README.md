# cypherpunk-vpn-android

Dependencies:

* Android SDK
* Android NDK
* $PATH must contain NDK folder with ndk-build

Building:

```
git submodule update --init
cd openvpn/main/
./misc/build.sh
cd ..
./gradlew build (or just use Android Studio)
```
