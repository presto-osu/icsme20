#!/usr/bin/env python3

import glob
import sqlite3
from pprint import pprint
import json
import random
from metadata import *

DB_DIR = '%s/../db/*.db' % DIR


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

