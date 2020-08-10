#!/usr/bin/env python

import json
import sys
import os
import subprocess
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element
from pprint import pprint

if len(sys.argv) < 3:
    print('Usage: python add_api_keys.py <path-to-apk-file> <path-to-api-keys-json-file>')
    exit(1)

apk_path = sys.argv[1]
apk_name = os.path.split(apk_path)[-1]
app_pkg = apk_name[:-len('.apk')]
keys_path = sys.argv[2]
keys_file = os.path.split(keys_path)[-1]

outdir_name = apk_name + '_decoded'
unpack_cmd = ['apktool', 'd', apk_path, '-s', '-o', outdir_name, '-f']
subprocess.call(unpack_cmd)

with open(keys_path) as f:
    key_data = json.load(f)
    # pprint(key_data)

string_res_path = os.path.join(outdir_name, 'res', 'values', 'strings.xml')
tree = ET.parse(string_res_path)
root = tree.getroot()

child_map = {
    # 'firebase_database_url': key_data['project_info']['firebase_url'],
    # 'gcm_defaultSenderId': key_data['project_info']['project_number'],
    # 'google_storage_bucket': key_data['project_info']['storage_bucket'],
    # 'project_id': key_data['project_info']['project_id']
}

found = False

for client in key_data['client']:
    if client['client_info']['android_client_info']['package_name'] == app_pkg:
        found = True
        # child_map['default_web_client_id'] = client['oauth_client'][0]['client_id']
        child_map['google_api_key'] = client['api_key'][0]['current_key']
        child_map['google_app_id'] = client['client_info']['mobilesdk_app_id']
        # child_map['google_crash_reporting_api_key'] = client['api_key'][0]['current_key']

if not found:
    print(f'client_info for {app_pkg} not found')
    exit(1)

# pprint(child_map)

updated = []

for child in root:
    key = child.attrib['name']
    if key in child_map:
        print('Replacing', key)
        child.text = child_map[key]
        updated.append(key)

for key in child_map:
    if key in updated:
        continue
    print('Adding', key)
    child = Element('string', {'name': key})
    child.text = child_map[key]
    root.append(child)

with open(string_res_path, 'wb') as f:
    tree.write(f)

try:
    subprocess.call('rm ~/.local/share/apktool/framework/1.apk')
except:
    # print('failed rm ~/.local/share/apktool/framework/1.apk')
    pass

repack_cmd = ['apktool', 'b', outdir_name, '-o', apk_name, '-f']
subprocess.call(repack_cmd)

subprocess.call(['rm', '-r', outdir_name])

