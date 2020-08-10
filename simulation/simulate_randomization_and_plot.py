#!/usr/bin/env python3

import glob
import sqlite3
import random
from pprint import pprint
import json
import math
import os
import sys
import time
import matplotlib.pyplot as plt
import numpy as np
import scipy.stats as st
from read_data import *
from metadata import *


# returns the counts of every event from all runs of an app
def get_histogram(data):
    n_user = len(data)
    hist = {}

    for dict_elems, events in data:
        for c in dict_elems:
            if c not in hist:
                hist[c] = 0
        for e in events:
            hist[e] += 1

    for c in hist:
        hist[c] /= n_user

    return hist


# returns the randomized set of events over the dictionary
def get_randomized_events(dict_elems, events, epsilon, prng):
    p_high = math.exp(epsilon) / (1 + math.exp(epsilon))
    p_low = 1 / (1 + math.exp(epsilon))
    rand_events = set()

    for e in dict_elems:
        if e in events:
            if prng.uniform() <= p_high:
                rand_events.add(e)
        elif prng.uniform() <= p_low:
            rand_events.add(e)

    return rand_events


# returns dictionary and randomized events for all runs on the app
def get_all_dicts_and_randomized_events(data, epsilon, prng, times):
    ret = []
    for dict_elems, events in data:
        for i in range(times):
            ret.append((dict_elems, get_randomized_events(dict_elems, events, epsilon, prng)))
    return ret


# returns the histograms generated from the randomized events
def get_estimated_histogram(data, epsilon):
    n_user = len(data)
    n_dicts = {}
    n_events = {}
    hist = {}

    for dict_elems, events in data:
        for c in dict_elems:
            hist[c] = 0
            if c not in n_dicts:
                n_dicts[c] = 0
            n_dicts[c] += 1
        for c in events:
            if c not in n_events:
                n_events[c] = 0
            n_events[c] += 1

    for c in hist:
        if c not in n_events:
            n_events[c] = 0
        hist[c] = ((1 + math.exp(epsilon)) * n_events[c] - n_dicts[c]) / (math.exp(epsilon) - 1)

    for c in hist:
        hist[c] = min(1, max(0, hist[c] / n_user))

    return hist


# returns maximum error for an app
def max_error(hist, rhist):
    err = []
    for c in hist:
        err.append(abs(hist[c] - rhist[c]))

    return max(err)


# returns median error for an app
def median_error(hist, rhist):
    err = []
    for c in hist:
        err.append(abs(hist[c] - rhist[c]))

    return np.median(err)


# returns relative error for an app
def relative_error(hist, rhist):
    err = 0
    for c in hist:
        err += abs(hist[c] - rhist[c])

    return err / sum(hist.values())


error_funcs = {
    'Max error': max_error,
    'Median error': median_error,
    'Relative error': relative_error
}


# return list of errors from n runs with specified error function
def get_errors(data, epsilon, err_func, prng, n, times=1):
    hist = get_histogram(data)
    err = []

    for i in range(n):
        rdata = get_all_dicts_and_randomized_events(data, epsilon, prng, times)
        rhist = get_estimated_histogram(rdata, epsilon)
        err.append(err_func(hist, rhist))

    return err


def plot_errors_all(ax, epsilon, err_func, prng, n):
    print('epsilon = ', EPSILON_LABEL[epsilon])
    ax.set_ylabel(err_func)
    ax.set_title(EPSILON_LABEL[epsilon])
    xtick_labels = [short_names[p] for p in pkgs]
    width = 0.3
    reps = [1, 10, 100]
    colors = ['lightgray', 'darkgray', 'gray']
    dx = [((1 - len(reps)) / 2 + i) * width for i in range(len(reps))]

    for t in range(len(reps)):
        n_users = 0
        print(reps[t] * 100, 'users')
        mean_err = []
        yerr = []
        x = [i + dx[t] for i in range(len(pkgs))]

        for pkg in pkgs:
            print(pkg)
            data = read_all_dicts_and_events(pkg)
            n_users = len(data) * reps[t]
            errors = get_errors(data, epsilon, error_funcs[err_func], prng, n, reps[t])
            mean_err.append(np.mean(errors))
            conf = 0.95
            yerr.append(st.sem(errors) * st.t.ppf((1 + conf) / 2, len(errors) - 1))
        ax.bar(x, mean_err, width, color=colors[t], label='%5d users' % n_users)
        ax.errorbar(x, mean_err, yerr=yerr, color='0', ls='none', lw=0.5, capsize=2)

    ax.set_xticks(range(len(pkgs)))
    ax.set_xticklabels(xtick_labels, rotation=30, size='small')
    ax.legend(fontsize='small')


def plot_errors_subset(ax, epsilon, err_func, cluster_alg, cluster_size, prng, n, rep):
    print('epsilon = ', EPSILON_LABEL[epsilon])
    ax.set_ylabel(err_func)
    ax.set_title(EPSILON_LABEL[epsilon] + ', %d users' % (rep * cluster_size))
    xtick_labels = [short_names[p] for p in pkgs]
    width = 0.3
    colors = ['lightgray', 'darkgray']
    dx = [-width / 2, width / 2]
    high_sim = [True, False]
    labels = ['High-similarity subset', 'Low-similarity subset']

    for i in range(len(high_sim)):
        print('High similarity: %s' % high_sim[i])
        mean_err = []
        yerr = []
        x = [j + dx[i] for j in range(len(pkgs))]

        for pkg in pkgs:
            print(pkg)
            data = read_all_dicts_and_events(pkg)
            cl_ind = cluster_alg(data, cluster_size, high_sim[i])
            cluster = [data[i] for i in cl_ind]

            errors = get_errors(cluster, epsilon, error_funcs[err_func], prng, n, rep)
            mean_err.append(np.mean(errors))
            conf = 0.95
            yerr.append(st.sem(errors) * st.t.ppf((1 + conf) / 2, len(errors) - 1))
        ax.bar(x, mean_err, width, color=colors[i], label=labels[i])
        ax.errorbar(x, mean_err, yerr=yerr, color='0', ls='none', lw=0.5, capsize=2)

    ax.legend(fontsize='small')
    ax.set_xticks(range(len(pkgs)))
    ax.set_xticklabels(xtick_labels, rotation=30, size='small')


def jaccard_similarity(v1, v2):
    return len(v1 & v2) / len(v1 | v2)


def create_cluster(data, size, high_priority):
    priority = -1 if high_priority else 1

    avg_sim = lambda x, cl: sum(jaccard_similarity(data[x][0], data[y][0]) for y in cl) / len(cl)\
        if len(cl) > 0 else (1 if high_priority else 0)
    comp = lambda a, b: a > b if high_priority else a < b

    start_sim = 0 if high_priority else 1
    start_pair = None

    for i in range(len(data)):
        for j in range(len(data)):
            if i == j:
                continue
            sim = jaccard_similarity(data[i][0], data[j][0])
            if comp(sim, start_sim):
                start_pair = i, j

    cl = set(start_pair)

    while len(cl) < size:
        d = min([i for i in range(len(data)) if i not in cl], key=lambda x: priority * avg_sim(x, cl))
        cl.add(d)

    return cl


def avg_cluster_similarity(data, cluster):
    sim = 0
    count = 0
    for i in cluster:
        for j in cluster:
            if i == j:
                continue
            count += 1
            sim += jaccard_similarity(data[i][0], data[j][0])
    return sim / count


def get_stats():
    for pkg in pkgs:
        data = read_all_dicts_and_events(pkg)
        avg_len = sum(len(d[0]) for d in data) / len(data)
        union_len = len(get_histogram(data))
        print('%-24s & %4d & %.2f \\\\' % ('\\texttt{' + short_names[pkg] + '}', union_len, avg_len))


def calculate_subset_stats(pkg, size):
    data = read_all_dicts_and_events(pkg)
    global_hist = get_histogram(data)
    subset_high_sim = create_cluster(data, size, True)
    subset_low_sim = create_cluster(data, size, False)

    high_sim_count = {}
    low_sim_count = {}

    for elem in global_hist:
        for i in subset_high_sim:
            if elem in data[i][0]:
                high_sim_count[elem] = high_sim_count.get(elem, 0) + 1
        for i in subset_low_sim:
            if elem in data[i][0]:
                low_sim_count[elem] = low_sim_count.get(elem, 0) + 1

    high_avg_dist = avg_cluster_similarity(data, subset_high_sim)
    high_sets_per_elem = sum(high_sim_count.values()) / len(high_sim_count)
    low_avg_dist = avg_cluster_similarity(data, subset_low_sim)
    low_sets_per_elem = sum(low_sim_count.values()) / len(low_sim_count)
    print('%s\t%.5f\t%.5f\t%.5f\t%.5f' %
          (short_names[pkg], high_avg_dist, high_sets_per_elem, low_avg_dist, low_sets_per_elem))


# get_stats()

seed = int(time.time())
prng = np.random.RandomState(seed)

fig, (ax1, ax2) = plt.subplots(2, 1)
plot_errors_all(ax1, EPSILON[0], 'Relative error', prng, 20)
plot_errors_all(ax2, EPSILON[1], 'Relative error', prng, 20)
fig.tight_layout()
plt.savefig('acc_all.pdf', bbox_inches='tight')
# plt.show()

rep = 1
cl_size = 50
fig, (ax1, ax2) = plt.subplots(2, 1)
plot_errors_subset(ax1, EPSILON[0], 'Relative error', create_cluster, cl_size, prng, 20, rep)
plot_errors_subset(ax2, EPSILON[1], 'Relative error', create_cluster, cl_size, prng, 20, rep)
fig.tight_layout()
plt.savefig('acc_subset_%d.pdf' % (rep * cl_size), bbox_inches='tight')
# plt.show()
