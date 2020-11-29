#!/usr/bin/env python3

import glob
import sqlite3
import random
from collections import defaultdict
from pprint import pprint
import json
import math
import os
import sys
import time
import timeit
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


def get_estimated_histogram(data, epsilon, prng):
    hist = defaultdict(float)
    n_dicts = defaultdict(float)
    n_reports = defaultdict(float)
    p_high = math.exp(epsilon) / (1 + math.exp(epsilon))
    p_low = 1 / (1 + math.exp(epsilon))

    for dict_elems, events in data:
        for e in dict_elems:
            n_dicts[e] += 1
            if e in events:
                if prng.uniform() <= p_high:
                    n_reports[e] += 1
            else:
                if prng.uniform() <= p_low:
                    n_reports[e] += 1

    for dict_elems, _ in data:
        for e in dict_elems:
            hist[e] = min(1, max(0, (n_reports[e] - p_low * n_dicts[e]) / (p_high - p_low) / len(data)))

    return hist


# returns relative error for actual and estimated histograms of an app
def relative_error(hist, rhist):
    return sum(abs(hist[k] - rhist[k]) for k in hist) / sum(hist.values())


error_funcs = {
    'Relative error': relative_error
}


# return list of errors from n runs with specified error function
def get_errors(data, epsilon, prng, n):
    hist = get_histogram(data)
    err = []

    for i in range(n):
        rhist = get_estimated_histogram(data, epsilon, prng)
        err.append(relative_error(hist, rhist))
        print('*', end='')
    print()

    return err


# plot relative error for all users (actual and synthetic) of all apps
def plot_errors_all(epsilon, prng, n):
    n_users = [100, 1000, 10000, 100000]
    results = {}

    if os.path.exists('%s.json' % EPSILON_NAME[epsilon]):
        with open('%s.json' % EPSILON_NAME[epsilon]) as f:
            results = json.load(f)
    else:
        for u in range(len(n_users)):
            nu = n_users[u]
            print('%d users' % nu)
            results[str(nu)] = {}
            for pkg in pkgs:
                print(short_names[pkg])
                data = read_synthetic_user_data(pkg, nu)
                results[str(nu)][short_names[pkg]] = get_errors(data, epsilon, error_funcs[err_func], prng, n)
        with open('%s.json' % EPSILON_NAME[epsilon], 'w') as f:
            json.dump(results, f)

    width = 0.2
    colors = ['gainsboro', 'lightgray', 'darkgray', 'gray']
    dx = [((1 - len(n_users)) / 2 + i) * width for i in range(len(n_users))]
    fig, ax = plt.subplots(1, 1)
    title = EPSILON_LABEL[epsilon]
    ax.set_title(title)
    print(title)
    ax.set_ylabel('Relative error')
    ax.set_xticks(range(len(pkgs)))
    ax.set_xticklabels([short_names[p] for p in pkgs], rotation=30, size='small')

    for u in range(len(n_users)):
        nu = n_users[u]
        print(nu, 'users')
        mean_errs = []
        conf_ints = []
        x = [i + dx[u] for i in range(len(pkgs))]

        for pkg in pkgs:
            print(pkg)
            errors = results[str(nu)][short_names[pkg]]
            mean_errs.append(np.mean(errors))
            conf = 0.95
            if n < 30:
                conf_ints.append(st.sem(errors) * st.t.ppf((1 + conf) / 2, len(errors) - 1))
            else:
                conf_ints.append(st.sem(errors) * st.norm.ppf((1 + conf) / 2))
            print(errors)

        ax.bar(x, mean_errs, width, color=colors[u], label='#users = %d' % nu)
        ax.errorbar(x, mean_errs, yerr=conf_ints,  color='0', ls='none', lw=0.5, capsize=2)

    ax.legend(fontsize='small')
    fig.tight_layout()
    plt.savefig('acc_all_%s.pdf' % EPSILON_NAME[epsilon])
    plt.show()


# calculates jaccard similarity of two event sets
def jaccard_similarity(v1, v2):
    return len(v1 & v2) / len(v1 | v2)


# creates (high/low-similarity) subset of specified number of users
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


# calculates the average pairwise similarity of a set of users
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


# creates high and low-similarity subsets from set of users, calculates and plots relative errors for each subset
def plot_errors_subset(epsilon, n_users, cluster_size, prng, n):
    print('epsilon = ', EPSILON_LABEL[epsilon])
    fig, ax = plt.subplots(1, 1)
    ax.set_ylabel('Relative error')
    ax.set_title(EPSILON_LABEL[epsilon] + ', %d users' % cluster_size)
    xtick_labels = [short_names[p] for p in pkgs]
    ax.set_xticks(range(len(pkgs)))
    ax.set_xticklabels(xtick_labels, rotation=30, size='small')
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
            data = read_synthetic_user_data(pkg, n_users)
            cl_ind = create_cluster(data, cluster_size, high_sim[i])
            cluster = [data[i] for i in cl_ind]

            errors = get_errors(cluster, epsilon, prng, n)
            print(errors)
            mean_err.append(np.mean(errors))
            conf = 0.95
            if n < 30:
                yerr.append(st.sem(errors) * st.t.ppf((1 + conf) / 2, len(errors) - 1))
            else:
                yerr.append(st.sem(errors) * st.norm.ppf((1 + conf) / 2))
        ax.bar(x, mean_err, width, color=colors[i], label=labels[i])
        ax.errorbar(x, mean_err, yerr=yerr, color='0', ls='none', lw=0.5, capsize=2)

    ax.legend(fontsize='small')
    fig.tight_layout()
    plt.savefig('acc_subset_%d_%s.pdf' % (cluster_size, EPSILON_NAME[epsilon]))
    plt.show()


# prints event set statistics for all apps
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


seed = int(time.time())
prng = np.random.RandomState(seed)
plt.rcParams['figure.figsize'][1] *= 0.75
plot_errors_all(EPSILON[0], prng, 30)
plot_errors_all(EPSILON[1], prng, 30)
plot_errors_all(EPSILON[2], prng, 30)
plot_errors_subset(EPSILON[0], 100, 50, prng, 30)
plot_errors_subset(EPSILON[1], 100, 50, prng, 30)
plot_errors_subset(EPSILON[2], 100, 50, prng, 30)
