#!/bin/sh

apk=$1
googleServices=$2

python3 add_api_keys.py $apk $googleServices
python3 fix_manifest.py $apk
bash sign.sh $apk
