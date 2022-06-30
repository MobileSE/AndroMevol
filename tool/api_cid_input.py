#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Objective: used to generate csv that stores APIs
used in different Android OSs including customized
ones and official Android

outs:
    0: the api or field not exists in the current framework
    1: the api or field exits in the current framework
    -1: we cannot successfully retrieve the api or fields provided by the current framework
"""

import os
import re
import csv
import ast

from collections import defaultdict
from collections import Counter

REPO_NAMES = ['official', 'huawei', 'miui', 'oneplus', 'oppo', 'samsung']

evol_methods = {}
evol_fields = {}
comp_apis = set()

def evol_api_load():
    api_path = './evol_induced_apis.txt'
    with open(api_path) as f:
        lines = [line.strip() for line in f.readlines()]
    return set(lines)

def csv_write(out_path, headers, content):
    with open(out_path, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        writer.writerows(content)

def is_evol_method(api_level, api, m_f):
    if m_f == 'methods':
        if api in evol_methods.keys() and api_level in evol_methods[api]:
            return True
        else:
            return False
    else:
        if api in evol_fields.keys() and api_level in evol_fields[api]:
            return True
        else:
            return False

def official_method_load(api_version):
    hide_txt_path = '/Users/pliu0032/Documents/Github/Mevol/outs/framework-{}/methods.all.txt'.format(api_version)
    with open(hide_txt_path) as f:
        lines = [line.strip() for line in f.readlines()]
    methods = set()
    for idx, line in enumerate(lines):
        if idx % 2 == 1:
            methods.add(line)
    return methods

def official_field_load(api_version):
    hide_txt_path = '/Users/pliu0032/Documents/Github/Mevol/outs/framework-{}/fields.all.txt'.format(api_version)
    with open(hide_txt_path) as f:
        lines = [line.strip() for line in f.readlines()]
    fields = set()
    for line in lines:
        pos = line.find('>:<')
        end = pos + 1
        field = line[:end].strip()
        if '=' in line:
            end = line.find('=')
            field = line[:end].strip() + '>'
        fields.add(field)
    return fields

def evol_mf_load(evol_path):
    api_levels = {}
    with open(evol_path) as f:
        lines = [line.strip() for line in f.readlines()]
    for line in lines:
        c_pos = line.find(':[')
        api = line[:c_pos]
        levels = ast.literal_eval(line[c_pos+1:])
        api_levels[api] = levels
    return api_levels

def evol_compatible_api_load():
    compatible_api_path = './compatible_api.txt'
    apis = set()
    with open(compatible_api_path) as f:
        lines = [line.strip() for line in f.readlines()]
    for line in lines:
        apis.add(line)
    return apis

def evol_field_method_retrieve():
    evol_m_path = '/Users/pliu0032/Documents/Github/CiD/apis/android/android_api_lifetime.txt'
    evol_f_path = '/Users/pliu0032/Documents/Github/CiD/apis/android/android_field_lifetime.txt'
    comp_apis = evol_compatible_api_load()
    evol_methods = evol_mf_load(evol_m_path)
    evol_fields = evol_mf_load(evol_f_path)

def is_aidl_interface(api):
    splits = api.split(' ')
    rdot_pos = splits[0].strip().rfind('.')
    cls_name = splits[0].strip()[rdot_pos + 1:]
    if cls_name.startswith('I'):
        return True
    return False

def csv_generate(method_fields):
    base_dir = '../res'
    out_base = '../res'
    total_repos = os.listdir(base_dir)
    repos = [repo for repo in total_repos if not repo.endswith('.csv')]
    repos.sort()
    device_outs = []
    device_headers = []
    device_apis = []
    method_missing = []
    method_specific = []
    field_missing = []
    field_specific = []
    method_out_lst = []
    field_out_lst = []
    dist_apis = set()
    total_exl = defaultdict(set)
    total_abs = defaultdict(set)
    level_repo_apis = defaultdict(lambda : defaultdict(set))
    for method_field in method_fields:
        for i in range(19, 31):
            if i == 20:
                continue
            official_methods = official_method_load(i)
            official_fields = official_field_load(i)
            outs = []
            headers = []
            repo_apis = defaultdict(set)
            repo_method_exl = defaultdict(set)
            repo_method_abs = defaultdict(set)
            method_exclusive = defaultdict(set)
            field_exclusive = defaultdict(set)
            repo_field_exl = defaultdict(set)
            repo_field_abs = defaultdict(set)
            for repo in repos:
                api_txt = os.path.join(base_dir, repo, 'framework-' + str(i), '{}_unique.txt'.format(method_field))
                if os.path.exists(api_txt):
                    apis = []
                    with open(api_txt) as f:
                        lines = [line.strip() for line in f.readlines()]
                    if lines:
                        for line in lines:
                            if line.startswith('<java.') or line.startswith('<javax.'):
                                continue
                            apis.append(line)
                        repo_apis[repo].update(set(apis))
            visited_apis = set()
            if repo_apis:
                for repo in repos:
                    if repo not in repo_apis:
                        continue
                    for api in repo_apis[repo]:
                        if visited_apis and api in visited_apis:
                            continue
                        visited_apis.add(api)
                        device_repo_api = defaultdict(int)
                        device_repo_api[repo] = 1
                        for inner_repo in repos:
                            if inner_repo == repo:
                                continue
                            if inner_repo not in repo_apis:
                                device_repo_api[inner_repo] = -1
                                continue
                            if api in repo_apis[inner_repo]:
                                device_repo_api[inner_repo] = 1
                            else:
                                device_repo_api[inner_repo] = 0
                        device_curr = [i, api]
                        for name in repos:
                            device_curr.append(device_repo_api[name])
                        if 0 in device_curr:
                            dist_apis.add(api)
                            if is_evol_method(i, api, method_field) or api.startswith('<org.apache.commons.') or api.startswith('<org.apache.http.') or api.endswith(' INTERNAL_CONTENT_URI>') or api.endswith(' EXTERNAL_CONTENT_URI>') or api in comp_apis:
                                continue
                            if method_field == 'methods':
                                counter = Counter(device_curr[2:])
                                if api in official_methods:
                                    if counter[0] == 1 and device_curr[4] == 0:
#                                    print(api)
                                        continue
                                    else:
                                        if device_curr[4] == 0:
                                            device_curr[4] = 1
                                else:
                                    if counter[0] == 1 and device_curr[4] == 0:
                                        continue
#                                        if 'org.apache.' in api or '.Stub' in api or '<init>' in api or ' clone()' in api or '.Default' in api or '<android.nfc.tech.' in api or '<com.android.internal.logging.nano.' in api or '.internal.' in api or '<android.service.power.' in api or '<android.text.Spann' in api or '<android.widget.' in api or '<android.webkit.CookieSyncManager' in api or is_aidl_interface(api):
#                                            continue
                            if method_field == 'fields':
                                counter = Counter(device_curr[2:])
                                if api in official_fields:
                                    if counter[0] == 1 and device_curr[4] == 0:
#                                    print(api)
                                        continue
                                    else:
                                        if device_curr[4] == 0:
                                            device_curr[4] = 1
                                else:
                                    if counter[0] == 1 and device_curr[4] == 0:
                                        continue
#                                        if 'org.apache.' in api or '.Stub' in api or '<init>' in api or ' clone()' in api or '.Default' in api or '<android.nfc.tech.' in api or '<com.android.internal.logging.nano.' in api or  '.internal.' in api or '<android.service.power.' in api or '<android.text.Spann' in api or '<android.widget.' in api or '<android.webkit.CookieSyncManager' in api or is_aidl_interface(api):
#                                            continue
#                        if True:
                            if method_field == 'methods':
                                for name in repos:
                                    if device_repo_api[name] == 1:
                                        repo_method_exl[name].add(api)
                                    elif device_repo_api[name] == 0:
                                        repo_method_abs[name].add(api)
                                counter = Counter(device_curr[2:])
                                if counter[1] == 1:
                                    for name in repos:
                                        if device_repo_api[name] == 1:
                                            method_exclusive[name].add(api)
                            else:
                                for name in repos:
                                    if device_repo_api[name] == 1:
                                        repo_field_exl[name].add(api)
                                    elif device_repo_api[name] == 0:
                                        repo_field_abs[name].add(api)
                                    counter = Counter(device_curr[2:])
                                    if counter[1] == 1:
                                        for name in repos:
                                            if device_repo_api[name] == 1:
                                                field_exclusive[name].add(api)
                            device_apis.append(device_curr)
                            if method_field == 'methods':
                                if device_curr[4] == 1:
                                    method_specific.append(device_curr)
                                else:
                                    method_missing.append(device_curr)
                            else:
                                if device_curr[4] == 1:
                                    field_specific.append(device_curr)
                                else:
                                    field_missing.append(device_curr)
                        if not device_headers:
                            device_headers = ['level', 'api']
                            device_headers.extend(repos)
                        device_outs.append(device_curr)
            ## methods:
            if method_field == 'methods':
                method_outs = ''
                total_methods = set()
                for name in REPO_NAMES:
                    exl_num = len(method_exclusive[name])
                    abs_num = len(repo_method_abs[name])
                    total_exl[name].update(method_exclusive[name])
                    total_abs[name].update(repo_method_abs[name])
                    total_methods.update(repo_method_exl[name], repo_method_abs[name])
                    method_outs += ('{:^8,d}'.format(exl_num)) + '&' + ('{:^8,d}'.format(abs_num)) + '&'
#                print(method_outs + '{:^8,d}'.format(len(total_methods)) + ' \\\\')
                method_out_lst.append(method_outs + '{:^8,d}'.format(len(total_methods)) + ' \\\\')
            if method_field == 'fields':
                ## fields:
                field_outs = ''
                total_fields = set()
                for name in REPO_NAMES:
                    exl_num = len(field_exclusive[name])
                    abs_num = len(repo_field_abs[name])
                    total_exl[name].update(field_exclusive[name])
                    total_abs[name].update(repo_field_abs[name])
                    total_fields.update(repo_field_exl[name], repo_field_abs[name])
                    field_outs += ('{:^8,d}'.format(exl_num)) + '&' + ('{:^8,d}'.format(abs_num)) + '&'
#                print(field_outs + '{:^8,d}'.format(len(total_fields)) + ' \\\\')
                field_out_lst.append(field_outs + '{:^8,d}'.format(len(total_fields)) + ' \\\\')
        print(method_field, len(device_outs))
    total_outs = ''
    total_repo_dist = set()
    for name in REPO_NAMES:
        total_outs +=  ('{:^8,d}'.format(len(total_exl[name]))) + '&' + ('{:^8,d}'.format(len(total_abs[name]))) + '&'
        total_repo_dist.update(total_exl[name])
        total_repo_dist.update(total_abs[name])
    print('total:', len(total_repo_dist))
    for i in range(len(method_out_lst)):
        print(method_out_lst[i])
        print(field_out_lst[i])
    print(total_outs[:-1])
    print('distinct apis no evol:', len(dist_apis))
    evol_apis = evol_api_load()
    dist_apis.update(evol_apis)
    print('distinct apis:', len(dist_apis))
    device_specific_out = os.path.join(out_base, 'device_specific_apis.csv')
    with open(device_specific_out, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(device_headers)
        writer.writerows(device_apis)
    method_specific_out = os.path.join(out_base, 'method_specific.csv')
    method_missing_out = os.path.join(out_base, 'method_missing.csv')
    field_specific_out = os.path.join(out_base, 'field_specific.csv')
    field_missing_out = os.path.join(out_base, 'field_missing.csv')
#    csv_write(method_specific_out, device_headers, method_specific)
#    csv_write(method_missing_out, device_headers, method_missing)
#    csv_write(field_specific_out, device_headers, field_specific)
#    csv_write(field_missing_out, device_headers, field_missing)


if __name__ == '__main__':
    method_fields = ['methods', 'fields']
    evol_field_method_retrieve()
    csv_generate(method_fields)
