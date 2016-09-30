APP_ABI := arm64-v8a armeabi armeabi-v7a mips  x86 x86_64
APP_PLATFORM := android-14

APP_STL:=gnustl_static
#APP_STL:=gnustl_shared


#APP_OPTIM := release

#LOCAL_ARM_MODE := arm

#NDK_TOOLCHAIN_VERSION=clang
APP_CPPFLAGS += -std=c++11
APP_CFLAGS += -funwind-tables
