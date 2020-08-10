#!/usr/bin/env python3

import argparse
import glob
import os
import shutil
import subprocess
import sys
import time
import json

import gorilla
from gorilla import info, warn, err, Gorilla, get_devices, progress, kill_proc
from test_methods import test_methods

SOOTOUTPUT_DIR = os.path.realpath(os.path.dirname(__file__))
GATOR_DIR = os.path.realpath(os.path.join(SOOTOUTPUT_DIR, '..'))
GATOR = os.path.join(GATOR_DIR, 'gator')
INSTRUMENT_SPEC_PATH = os.path.join(GATOR_DIR, 'instrument_spec.json')
GOOGLE_SERVICES_PATH = os.path.join(SOOTOUTPUT_DIR, 'google-services.json')

ADB = os.path.join(os.environ['ANDROID_SDK'], 'platform-tools', 'adb')
EMULATOR = os.path.join(os.environ['ANDROID_SDK'], 'emulator', 'emulator')


def newline():
    sys.stdout.write('\n')
    sys.stdout.flush()


def instrument(apk, apk_name):
    if not os.path.isfile('%s/%s' % (SOOTOUTPUT_DIR, apk_name)):
        warn('Instrument and post-instrument...')
        cmd = '%s i -p %s -i %s' % (GATOR, os.path.realpath(apk), INSTRUMENT_SPEC_PATH)
        info(cmd)
        proc = subprocess.Popen(cmd.split(),
                                cwd=GATOR_DIR,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT,
                                universal_newlines=True)
        while True:
            inline = proc.stdout.readline()
            if not inline or proc.poll():
                break
            # print(inline)


        cmd = './post_instrument.sh %s %s' % (apk_name, GOOGLE_SERVICES_PATH)
        info(cmd)
        proc = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, cwd=SOOTOUTPUT_DIR)
        while True:
            inline = proc.stdout.readline()
            if not inline or proc.poll():
                break
            # print(inline)


# settings put global airplane_mode_on 0
# am broadcast -a android.intent.action.AIRPLANE_MODE
# svc wifi enable
def run(apk, avd, dev, start_idx, num_runs, window):
    apk_name = apk.split('/')[-1]
    info('---------------------- %s on %s -----------------' % (apk_name, dev))

    pkg_name = apk_name[:-len('.apk')]

    create_emulator(avd)
    x = start_idx
    while x < num_runs:
        info('------------- %d-th run ------------' % x)
        info('------------- %d-th run ------------' % x)
        info('------------- %d-th run ------------' % x)

        proc = run_on_emulator(apk, apk_name, x, avd, dev, window, True)

        dbfile = '%s/db/%s_%s.db' % (SOOTOUTPUT_DIR, pkg_name, dev)
        rdbfile = '%s/db/%s_%d_%s.random.db' % (SOOTOUTPUT_DIR, pkg_name, x, dev)
        try:
            try:
                shutil.move(dbfile, rdbfile)
                print('%s >>> %s' % (dbfile, rdbfile))
            except:
                err('Error during moving %s to %s' % (dbfile, rdbfile))
                err('Redo %d-th run, first sleep for 30 seconds' % x)
                time.sleep(30)
                continue
            # out = subprocess.check_output(
            #     ['./read_db_graph_edge_histogram.py', rdbfile],
            #     stderr=subprocess.STDOUT,
            #     cwd=SOOTOUTPUT_DIR,
            #     universal_newlines=True)
            # info('Read Database:\n%s' % out)
            # if '========' in out and 'Bad' in out:
            #     warn('Value beyond dictionary')
        except subprocess.CalledProcessError:
            err('Error read %s' % rdbfile)
            err('Redo %d-th run' % x)
            try:
                os.remove(dbfile)
                os.remove(rdbfile)
            except FileNotFoundError:
                pass
            continue
        finally:
            shutdown(avd, dev)
            kill_proc(proc)
            info('Kill emulator @%s' % proc.pid)

        x += 1


def create_emulator(avd='api_27'):
    try:
        out = subprocess.check_output(['./create_avd.sh', avd],
                                      stderr=subprocess.STDOUT,
                                      cwd=SOOTOUTPUT_DIR,
                                      universal_newlines=True)
        if 'Do you wish to create a custom hardware profile? [no] no' in out:
            info('Emulator created...')
    except subprocess.CalledProcessError as e:
        err('Crash creating emulator: %s' % (e.output))
        msg = "Error: Android Virtual Device '%s' already exists." % avd
        if msg in e.output:
            warn('%s already exists' % avd)
            pass


def shutdown(avd, dev):
    avds = get_devices()
    info('Devices: %s' % avds)
    if not avds:
        return
    while dev in avds:
        warn('Shutting down %s at %s' % (avd, dev))
        subprocess.call([ADB, '-s', dev, 'shell', 'reboot', '-p'])
        time.sleep(10)
        avds = get_devices()
        if not avds:
            break


def run_on_emulator(apk, apk_name, x, avd, dev, window, reboot):
    if reboot:
        shutdown(avd, dev)
        port = dev[len('emulator-'):]
        cmd = [
            EMULATOR, '-avd', avd, '-verbose', '-wipe-data', '-no-audio',
            '-no-snapshot', '-port', port, '-no-boot-anim', '-skin', '360x640'
        ]
        if not window:
            cmd.append('-no-window')
        info(' '.join(cmd))
        proc = subprocess.Popen(cmd,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT,
                                universal_newlines=True)
        info('Running emulator @%s' % proc.pid)
    while True:
        inline = proc.stdout.readline()
        if not inline or proc.poll():
            # time.sleep(3)
            warn('AVD %s at %s @%s closed...' % (avd, dev, proc.pid))
            break
        progress('+')
        # print(inline)
        if "emulator: ERROR: There's another emulator instance running with the current AVD" in inline:
            newline()
            warn('Multiple instances of %s at %s for %d-th run...' %
                 (avd, dev, x))
            kill_proc(proc)
            time.sleep(15)
            run_on_emulator(apk, apk_name, x, avd, dev, window, reboot)
            break
        elif 'QXcbConnection: Could not connect to display' in inline:
            newline()
            warn('Failed to start avd %s at %s with window @%s' %
                 (avd, dev, proc.pid))
            warn('Restart without window...')
            kill_proc(proc)
            time.sleep(15)
            run_on_emulator(apk, apk_name, x, avd, dev, False, reboot)
            break
        elif 'emulator: INFO: boot completed' in inline:
            newline()
            info('Emulator started...')
            instrument(apk, apk_name)
            g = Gorilla(os.path.join(SOOTOUTPUT_DIR, apk_name), device=dev)
            if not g.initialized:
                break
            g.disable_notif_bar()
            if not g.install_app():
                break
            g.clear_package()
            # g.open_app(dev)
            test_methods[g.get_pkg_name()](g, dev)
            time.sleep(5)
            info('Retrieving database...')
            g.store_dbs()
            time.sleep(5)
            break
    return proc


def main():
    args = parse_args()
    apk_name = args.apk.split('/')[-1]
    run(args.apk, args.avd, args.device, args.start_idx, args.num_runs, args.window)
    return


def parse_args():
    parser = argparse.ArgumentParser(description='Running emulators.')
    parser.add_argument('-v',
                        '--avd',
                        dest='avd',
                        metavar='AVD',
                        required=True,
                        help='specify AVD')
    parser.add_argument('-d',
                        '--device',
                        dest='device',
                        metavar='DEV',
                        required=True,
                        help='specify device name')
    parser.add_argument('-p',
                        '--apk',
                        dest='apk',
                        metavar='PATH',
                        required=True,
                        help='specify APK path')
    parser.add_argument('-s',
                        '--start-index',
                        dest='start_idx',
                        metavar='N',
                        type=int,
                        default=0,
                        help='specify start round')
    parser.add_argument('-n',
                        '--num-runs',
                        dest='num_runs',
                        metavar='N',
                        type=int,
                        default=1,
                        help='number of runs per emulator')
    parser.add_argument('-w',
                        '--window',
                        dest='window',
                        action='store_true',
                        default=False,
                        help='show GUI')
    return parser.parse_args()


if __name__ == '__main__':
    main()
