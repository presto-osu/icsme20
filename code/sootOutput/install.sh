#!/bin/bash

pkg=${1::-4}

echo ..... Uninstall
${ANDROID_SDK}/platform-tools/adb shell pm uninstall $pkg

echo ..... Sign
./sign.sh $1

echo ..... Install
${ANDROID_SDK}/platform-tools/adb install -r $1

