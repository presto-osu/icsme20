#!/bin/bash

for name in "$@"; do
  ${ANDROID_SDK}/tools/bin/avdmanager delete avd -n "$name"
  # avdmanager -s create avd -n "$name" -k "system-images;android-28;google_apis;x86"
  expect create_avd.expect "$name" "${ANDROID_SDK}/tools/bin/avdmanager"
done
