#!/bin/sh

apkPath=$1
# password: wy092883
# zip -d $apkPath META-INF/\*
# jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.keystore $apkPath  alias_name

zip -d $apkPath META-INF/\*
expect ./sign.expect $apkPath ./my-release-key.keystore

echo ..... Zipalign
mv $apkPath .$apkPath
${ANDROID_SDK}/build-tools/28.0.3/zipalign -v 4 .$apkPath $apkPath
${ANDROID_SDK}/build-tools/28.0.3/zipalign -c -v 4 $apkPath
rm .$apkPath

