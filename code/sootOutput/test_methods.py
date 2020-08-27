#!/usr/bin/env python3

from uiautomator import Device
import os
import time
import random
import subprocess
from gorilla import info, warn, err, Gorilla


def sparkpeople_test(g, dev):
    max_events = 100
    n_events = 0
    d = Device(dev)
    g.open_app(dev)
    time.sleep(3)

    while n_events < max_events:
        if d(text='NO THANKS').exists:
            d(text='NO THANKS').click.wait()
        if not d(resourceId='com.sparkpeople.android.cookbook:id/lvMainListView').exists:
            g.open_app(dev)
            time.sleep(3)
            continue
        if d(resourceId='com.sparkpeople.android.cookbook:id/main_notification_close_id').exists:
            d(resourceId='com.sparkpeople.android.cookbook:id/main_notification_close_id').click.wait()
        listView = d(resourceId='com.sparkpeople.android.cookbook:id/lvMainListView')
        listView.scroll(steps=random.randint(40, 200))
        # time.sleep(1)
        items = listView.child(resourceId='com.sparkpeople.android.cookbook:id/lvHSImageID')
        available = []
        for item in items:
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue
        c_item = random.choice(available)
        c_item.click.wait()
        n_events += 1
        if n_events % 10 == 0:
            print('#events: ', n_events)
        d.press.back()
        time.sleep(1)


def infowars_test(g, dev):
    max_events = 50
    n_events = 0
    d = Device(dev)
    g.open_app(dev)
    # time.sleep(3)
    d(resourceId='com.infowars.official:id/news').click.wait()

    while n_events < max_events:
        if d(resourceId='com.infowars.official:id/bbn_layoutManager').exists:
            d.swipe(100, 160, 100, 80)
        listView = d(resourceId='com.infowars.official:id/article_recycler_view')
        listView.scroll(steps=50)
        items = listView.child(resourceId='com.infowars.official:id/article_header')
        available = []
        for item in items:
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue
        c_item = random.choice(available)
        c_item.click.wait()

        save_btn = d(resourceId='com.infowars.official:id/action_save')
        if random.random() < 0.8:
            save_btn.click.wait()
            n_events += 1
        if n_events % 10 == 0:
            print('#events: ', n_events)
        d.press.back()
        time.sleep(1)


def aggrego_test(g, dev):
    max_events = 100
    n_events = 0
    d = Device(dev)
    g.open_app(dev)
    time.sleep(10)

    d(resourceId='com.aggrego.loop:id/btnnext').click.wait()
    d(resourceId='com.aggrego.loop:id/linearbarbadose').click.wait()
    d(resourceId='com.android.packageinstaller:id/permission_allow_button').click.wait()

    while n_events < max_events:
        if not d(resourceId='com.aggrego.loop:id/listview_latest').exists:
            g.open_app(dev)
            time.sleep(10)
            if d(resourceId='com.aggrego.loop:id/btnnext').exists:
                d(resourceId='com.aggrego.loop:id/btnnext').click.wait()
            if d(resourceId='com.aggrego.loop:id/linearbarbadose').exists:
                d(resourceId='com.aggrego.loop:id/linearbarbadose').click.wait()
            continue
        listView = d(resourceId='com.aggrego.loop:id/listview_latest')
        listView.scroll(steps=random.randint(25, 40))
        available = []
        # items = listView.child(resourceId='com.aggrego.loop:id/linearlargelatest')
        for item in d(resourceId='com.aggrego.loop:id/linearlargelatest'):
            if item.exists:
                available.append(item)
        # items = listView.child(resourceId='com.aggrego.loop:id/linearsmalllatest')
        for item in d(resourceId='com.aggrego.loop:id/linearsmalllatest'):
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue
        c_item = random.choice(available)
        c_item.click.wait()
        time.sleep(2)
        n_events += 1
        if n_events % 10 == 0:
            print('#events: ', n_events)
        d.press.back()
        time.sleep(2)


def reststops_test(g, dev):
    max_events = 100
    n_events = 0
    d = Device(dev)
    g.open_app(dev)

    d(resourceId='com.android.packageinstaller:id/permission_allow_button').click.wait()

    while n_events < max_events:
        state_listView = d(resourceId='com.insofttech.reststops:id/home_recycler')
        state_listView.scroll.vert.backward(steps=2)
        if random.random() < 0.8:
            state_listView.scroll(steps=random.randint(2, 10))
        available = []
        for item in d(resourceId='com.insofttech.reststops:id/card_name'):
            if item.exists:
                available.append(item)
        c_item = random.choice(available)
        # print(c_item.text)
        c_item.click.wait()

        highway_listView = d(resourceId='com.insofttech.reststops:id/my_list_view')
        if not highway_listView.exists:
            d.press.back()
            continue
        n_highways = random.randint(1, d(resourceId='com.insofttech.reststops:id/card_name').count)

        for i in range(n_highways):
            if random.random() < 0.8:
                highway_listView.scroll(steps=random.randint(2, 10))
            available = []
            for item in d(resourceId='com.insofttech.reststops:id/card_name'):
                if item.exists:
                    available.append(item)
            c_item = random.choice(available)
            # print(c_item.text)
            c_item.click.wait()
            time.sleep(5)

            tabs = d(resourceId='com.insofttech.reststops:id/tabs')
            if not tabs.exists:
                d.press.back()
                continue
            tabs.child(className='android.widget.TextView', instance=random.randint(0, 1)).click.wait()
            reststop_listView = d(resourceId='com.insofttech.reststops:id/home_recycler')
            if not reststop_listView.exists or d(resourceId='com.insofttech.reststops:id/rslist_r_title').count == 0:
                d.press.back()
                continue
            n_reststops = random.randint(1, d(resourceId='com.insofttech.reststops:id/rslist_r_title').count)

            for j in range(n_reststops):
                if random.random() < 0.8:
                    reststop_listView.scroll(steps=random.randint(2, 10))
                available = []
                for item in d(resourceId='com.insofttech.reststops:id/rslist_r_title'):
                    if item.exists:
                        available.append(item)

                if available:
                    c_item = random.choice(available)
                    # print(c_item.text)
                    c_item.click.wait()
                    n_events += 1
                    if n_events % 10 == 0:
                        print('#events: ', n_events)
                    d.press.back()

            d.press.back()

        d.press.back()


def opensnow_test(g, dev):
    max_events = 120
    n_events = 0
    d = Device(dev)
    g.open_app(dev)

    d(resourceId='com.opensnow.android:id/fragment_intro_lets_do_this_button').click.wait()
    d(resourceId='com.opensnow.android:id/fragment_intro_location_skip_button').click.wait()
    d(resourceId='com.opensnow.android:id/fragment_intro_customize_customize_later_button').click.wait()
    d(resourceId='com.opensnow.android:id/menu_navigation_news').click.wait()

    while n_events < max_events:
        listView = d(resourceId='com.opensnow.android:id/recycler')
        listView.scroll(steps=random.randint(25, 40))
        available = []
        for item in d(resourceId='com.opensnow.android:id/item_news_image'):
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue
        c_item = random.choice(available)
        if random.random() < 0.8:
            c_item.click.wait()
            time.sleep(2)
            n_events += 1
            if n_events % 10 == 0:
                print('#events: ', n_events)
            d.press.back()
        time.sleep(2)


def shipmate_test(g, dev):
    max_events = 130
    n_events = 0
    d = Device(dev)
    g.open_app(dev)
    bound = d(resourceId='shipmate.carnival:id/bottom_nav').bounds
    d.click(d.info['displayWidth'] // 2, 2 * bound['top'] - bound['bottom'] - 10)
    d(resourceId='shipmate.carnival:id/photos').click.wait()

    while n_events < max_events:
        listView = d(resourceId='shipmate.carnival:id/recycler_view')
        available = []
        for item in d(resourceId='shipmate.carnival:id/photo_list_item_photo'):
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue
        c_item = random.choice(available)
        c_item.click.wait()
        n_events += 1
        if n_events % 10 == 0:
            print('#events: ', n_events)
        d.press.back()
        # if n_events % 2 == 0:
        #     listView.scroll(steps=50)


def channels_test(g, dev):
    max_events = 150
    n_events = 0
    d = Device(dev)
    g.open_app(dev)

    while n_events < max_events:
        d(description='Open navigation drawer').click.wait()
        navView = d(resourceId='com.channelstv.channels.mobile:id/design_navigation_view')
        category = navView.child_by_instance(random.randint(0, 6),
                                  resourceId='com.channelstv.channels.mobile:id/design_menu_item_text')
        # print(category.info['text'])
        category.click.wait()

        for i in range(random.randint(1, 10)):
            listView = d(className='android.support.v7.widget.RecyclerView')
            available = []
            for item in d(className='android.widget.ImageView'):
                if item.info.get('contentDescription', None) == 'More options':
                    continue
                if item.exists:
                    available.append(item)
            if len(available) == 0:
                continue

            c_item = random.choice(available)
            # print(c_item.sibling(className='android.widget.TextView').info['text'])
            c_item.click.wait()
            d(className='android.widget.ScrollView').scroll(steps=20)
            # time.sleep(20)
            n_events += 1
            if n_events % 10 == 0:
                print('#events: ', n_events)
            d.press.back()
            # time.sleep(2)
            listView.scroll(steps=50)
    # time.sleep(40)


def apartmentguide_test(g, dev):
    max_events = 120
    n_events = 0
    d = Device(dev)

    g.open_app(dev)
    time.sleep(3)

    continue_button = d(resourceId='com.primedia.apartmentguide:id/continue_on')
    if continue_button.exists:
        continue_button.click.wait()
    ok_button = d(resourceId='android:id/button1', text='OK')
    if ok_button.exists:
        ok_button.click.wait()
    set_location = d(resourceId='com.primedia.apartmentguide:id/location_hint')
    if set_location.exists:
        set_location.click.wait()
    location_edit = d(resourceId='com.primedia.apartmentguide:id/place_edittext')
    if location_edit.exists:
        location_edit.set_text('Atlanta, GA')
    menu_item = d(resourceId='android:id/text1')
    if menu_item.exists:
        menu_item.click.wait()
    list_button = d(resourceId='com.primedia.apartmentguide:id/map_list_button')
    if list_button.exists:
        list_button.click.wait()

    while n_events < max_events:
        # if d(resourceId='com.primedia.apartmentguide:id/toolbar').exists:
        #     d.swipe(100, 160, 100, 40)
        listView = d(resourceId='com.primedia.apartmentguide:id/listview')
        available = []
        for item in d(resourceId='com.primedia.apartmentguide:id/photo_container'):
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue

        if random.random() < 0.8:
            c_item = random.choice(available)
            c_item.click.wait()
            time.sleep(10)
            # d(resourceId='com.primedia.apartmentguide:id/listing_container').scroll(steps=20)
            n_events += 1
            if n_events % 10 == 0:
                print('#events: ', n_events)
            d.press.back()
        listView.scroll(steps=50)

    time.sleep(60)


def rent_test(g, dev):
    max_events = 150
    n_events = 0
    d = Device(dev)

    g.open_app(dev)
    time.sleep(3)

    continue_button = d(resourceId='com.rent:id/continue_on')
    if continue_button.exists:
        continue_button.click.wait()
    ok_button = d(resourceId='android:id/button1', text='OK')
    if ok_button.exists:
        ok_button.click.wait()
    set_location = d(resourceId='com.rent:id/location_hint')
    if set_location.exists:
        set_location.click.wait()
    location_edit = d(resourceId='com.rent:id/place_edittext')
    if location_edit.exists:
        location_edit.set_text('Atlanta, GA')
    menu_item = d(resourceId='android:id/text1')
    if menu_item.exists:
        menu_item.click.wait()
    list_button = d(resourceId='com.rent:id/map_list_button')
    if list_button.exists:
        list_button.click.wait()

    while n_events < max_events:
        # if d(resourceId='com.primedia.apartmentguide:id/toolbar').exists:
        #     d.swipe(100, 160, 100, 40)
        listView = d(resourceId='com.rent:id/listview')
        available = []
        for item in d(resourceId='com.rent:id/photo_container'):
            if item.exists:
                available.append(item)
        if len(available) == 0:
            continue

        if random.random() < 0.9:
            c_item = random.choice(available)
            c_item.click.wait()
            search_now = d(resourceId='com.rent:id/search_now')
            if search_now.exists:
                search_now.click.wait()
                continue
            time.sleep(10)
            # d(resourceId='com.rent:id/listing_container').scroll(steps=20)
            n_events += 1
            if n_events % 10 == 0:
                print('#events: ', n_events)
            d.press.back()
        listView.scroll(steps=50)

    time.sleep(60)


test_methods = {
    'com.sparkpeople.android.cookbook': sparkpeople_test,
    'com.infowars.official': infowars_test,
    'com.aggrego.loop': aggrego_test,
    'com.insofttech.reststops': reststops_test,
    'com.opensnow.android': opensnow_test,
    'shipmate.carnival': shipmate_test,
    'com.channelstv.channels.mobile': channels_test,
    'com.primedia.apartmentguide': apartmentguide_test,
    'com.rent': rent_test,
}
