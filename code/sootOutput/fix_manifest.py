#!/usr/bin/env python

import sys
import os
import subprocess
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element
from pprint import pprint

if len(sys.argv) < 2:
    print('Usage: python fix_manifest.py <path-to-apk-file>')
    exit(1)

apk_path = sys.argv[1]
apk_name = os.path.split(apk_path)[-1]
app_pkg = apk_name[:-len('.apk')]

outdir_name = apk_name + '_decoded'
unpack_cmd = ['apktool', 'd', apk_path, '-s', '-o', outdir_name, '-f']
print(' '.join(unpack_cmd))
subprocess.call(unpack_cmd)

manifest_path = os.path.join(outdir_name, 'AndroidManifest.xml')
tree = ET.parse(manifest_path)
root = tree.getroot()

app = root.find('application')
child = Element('service', {'{http://schemas.android.com/apk/res/android}name': 'presto.privaid.SendJobService',
                            '{http://schemas.android.com/apk/res/android}permission': 'android.permission.BIND_JOB_SERVICE'})
app.append(child)

with open(manifest_path, 'wb') as f:
    tree.write(f)

try:
    subprocess.call('rm ~/.local/share/apktool/framework/1.apk')
except:
    # print('failed rm ~/.local/share/apktool/framework/1.apk')
    pass

repack_cmd = ['apktool', 'b', outdir_name, '-o', apk_name, '-f']
print((' '.join(repack_cmd)))
subprocess.call(repack_cmd)

subprocess.call(['rm', '-r', outdir_name])
