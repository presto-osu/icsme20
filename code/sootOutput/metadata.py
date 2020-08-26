#!/usr/bin/env python3

import math
import os

DIR = os.path.realpath(os.path.dirname(__file__))

EPSILON = [math.log(3), math.log(9), math.log(49)]
EPSILON_LABEL = {
    math.log(3): r'$\epsilon=\ln(3)$',
    math.log(9): r'$\epsilon=\ln(9)$',
    math.log(49): r'$\epsilon=\ln(49)$'
}
EPSILON_NAME = {
    math.log(3): 'ln3',
    math.log(9): 'ln9',
    math.log(49): 'ln49'
}

pkgs = [
    'com.primedia.apartmentguide',
    'com.insofttech.reststops',
    'com.rent',
    'shipmate.carnival',
    'com.sparkpeople.android.cookbook',
    'com.channelstv.channels.mobile',
    'com.infowars.official',
    'com.aggrego.loop',
    'com.opensnow.android',
]

short_names = {
    'com.sparkpeople.android.cookbook': 'cookbook',
    'com.infowars.official': 'infowars',
    'com.aggrego.loop': 'loop',
    'com.insofttech.reststops': 'reststops',
    'com.opensnow.android': 'opensnow',
    'shipmate.carnival': 'shipmate',
    'com.channelstv.channels.mobile': 'channels',
    'com.primedia.apartmentguide': 'apartmentguide',
    'com.rent': 'rent'
}

EVENTS_TABLE_NAME = 'events'

db_specs = {
    'com.infowars.official': {
        'event': 'article_action',
        'param': 'article_url'
    },
    'com.sparkpeople.android.cookbook': {
        'event': 'select_content',
        'param': 'item_id'
    },
    'com.aggrego.loop': {
        'event': 'select_content',
        'param': 'search_term'
    },
    'com.insofttech.reststops': {
        'event': 'select_content',
        'param': 'item_id'
    },
    'com.opensnow.android': {
        'event': 'View_News_Detail',
        'param': 'news_id'
    },
    'shipmate.carnival': {
        'event': 'view_item',
        'param': 'item_id'
    },
    'com.channelstv.channels.mobile': {
        'event': 'select_content',
        'param': 'item_id'
    },
    'com.primedia.apartmentguide': {
        'event': 'view_item',
        'param': 'item_id'
    },
    'com.rent': {
        'event': 'view_item',
        'param': 'item_id'
    }
}
