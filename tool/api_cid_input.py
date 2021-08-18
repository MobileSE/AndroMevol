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

from collections import defaultdict


def csv_write(out_path, headers, content):
    with open(out_path, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        writer.writerows(content)

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
    level_repo_apis = defaultdict(lambda : defaultdict(set))
    for method_field in method_fields:
        for i in range(19, 31):
            outs = []
            headers = []
            repo_apis = defaultdict(set)
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
#                        if True:
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
        print(method_field, len(device_outs))
    device_specific_out = os.path.join(out_base, 'device_specific_apis.csv')
    with open(device_specific_out, 'w') as f:
        writer = csv.writer(f)
        writer.writerow(device_headers)
        writer.writerows(device_apis)
    method_specific_out = os.path.join(out_base, 'method_specific.csv')
    method_missing_out = os.path.join(out_base, 'method_missing.csv')
    field_specific_out = os.path.join(out_base, 'field_specific.csv')
    field_missing_out = os.path.join(out_base, 'field_missing.csv')
    csv_write(method_specific_out, device_headers, method_specific)
    csv_write(method_missing_out, device_headers, method_missing)
    csv_write(field_specific_out, device_headers, field_specific)
    csv_write(field_missing_out, device_headers, field_missing)


if __name__ == '__main__':
    method_fields = ['methods', 'fields']
    csv_generate(method_fields)
