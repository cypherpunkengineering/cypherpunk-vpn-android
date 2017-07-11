# cypherpunk-vpn-android

Dependencies:

* Android SDK
* Android NDK
* $PATH must contain NDK folder with ndk-build

Building:

```
git submodule update --init
cd openvpn/main/
./misc/build-native.sh
cd ../../
./gradlew build (or just use Android Studio)
```


## structure

```
IdentifyEmailActivity
  email is registered ?
  yes --> LoginActivity
  no  --> SignUpActivity

LoginActivity
  success login -- f --> TutorialActivity

SignUpActivity
  success sign up     --> ConfirmationEmailActivity
  email is registered --> LoginActivity

ConfirmationEmailActivity
  -- f --> TutorialActivity

TutorialActivity
  -- f --> MainActivity
```
