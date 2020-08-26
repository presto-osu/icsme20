#!/usr/bin/env python3

import glob
import sqlite3
from pprint import pprint
import json
import random
import os
from metadata import *

DB_DIR = '%s/db/*.db' % DIR


# returns list of db files for a package
def load_dbs(pkg_name):
    return list(filter(lambda x: x.split('/')[-1].split('_')[0] == pkg_name, glob.glob(DB_DIR)))


# returns values from a specified column of a table
def read_from_db(db, tab_name, column):
    conn = sqlite3.connect(db)
    c = conn.cursor()
    c.execute('SELECT ' + column + ' FROM ' + tab_name)
    rows = c.fetchall()
    conn.close()
    return rows


# returns a list of json field values from a column of a table in db
def read_json_field_from_db(db, tab_name, column, json_field):
    return [json.loads(x[0])[json_field] for x in read_from_db(db, tab_name, column) if json_field in json.loads(x[0])]


# reads the dictionary and logged events from a db
def read_dict_and_events(db, dict_table, json_field):
    dict_elems = set(read_json_field_from_db(db, dict_table, 'item', json_field))
    events = set(read_json_field_from_db(db, EVENTS_TABLE_NAME, 'params', json_field))
    return dict_elems, events


# reads the dictionaries and event lists for all runs of an app
def read_all_dicts_and_events(pkg_name):
    dbs = load_dbs(pkg_name)
    return [read_dict_and_events(db, db_specs[pkg_name]['event'], db_specs[pkg_name]['param']) for db in dbs]


SYN_DATA_DIR = '%s/synthetic_user_data' % DIR


# creates dictionary and events set for a synthetic user from 2 actual actual users
def create_synthetic_user(u1, u2):
    return [u1[0] | u2[0], random.sample(u1[1] | u2[1], (len(u1[1]) + len(u2[1])) // 2)]


# creates a list of specified number of synthetic users from actual user data
def get_synthetic_users(data, count):
    syn = []
    for i in range(count - len(data)):
        u1, u2 = random.sample(data, 2)
        syn.append(create_synthetic_user(u1, u2))
    return [[sorted(list(d[0])), sorted(list(d[1]))] for d in data + syn]


# creates and stores synthetic user data in json format
def store_events_to_file(pkg):
    data = read_all_dicts_and_events(pkg)

    for count in [100, 1000, 10000, 100000]:
        os.makedirs(SYN_DATA_DIR, exist_ok=True)
        with open('%s/%s_%d.json' % (SYN_DATA_DIR, pkg, count), 'w') as f:
            json.dump(get_synthetic_users(data, count), f)


# reads synthetic user data for specified number of users
def read_synthetic_user_data(pkg, n_users):
    if not os.path.exists('%s/%s_%d.json' % (SYN_DATA_DIR, pkg, n_users)):
        raise FileNotFoundError
    with open('%s/%s_%d.json' % (SYN_DATA_DIR, pkg, n_users)) as f:
        return [[set(c), set(e)] for c, e in json.load(f)]


if __name__ == '__main__':
    for pkg in pkgs:
        print(pkg)
        store_events_to_file(pkg)
