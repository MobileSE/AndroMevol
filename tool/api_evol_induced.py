#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import csv

from collections import defaultdict
from collections import Counter

def txt_load(txt_path):
    with open(txt_path) as f:
        lines = [line.strip() for line in f.readlines()]
    return lines

def evol_induced_count():
    absent = 0
    exclusive = 0
    base_dir = '../res/official'
    official_apis = defaultdict(set)
    for i in range(19, 31):
        if i == 20:
            continue
        api_method_path = os.path.join(base_dir, 'framework-' + str(i), 'methods_unique.txt')
        api_field_path = os.path.join(base_dir, 'framework-' + str(i), 'fields_unique.txt')
        api_methods = txt_load(api_method_path)
        api_fields = txt_load(api_field_path)
        official_apis['{}'.format(i)].update(api_methods, api_fields)
    visited_apis = set()
    diff_apis = 0
    api_outs = ''
    for i in range(19, 31):
        if i == 20:
            continue
        for api in official_apis['{}'.format(i)]:
            if visited_apis and api in visited_apis:
                continue
            curr_apis = defaultdict(int)
            curr_apis['{}'.format(i)] = 1
            for j in range(19, 31):
                if j == 20 or j == i:
                    continue
                if api in official_apis['{}'.format(j)]:
                    curr_apis['{}'.format(j)] = 1
                else:
                    curr_apis['{}'.format(j)] = 0
            visited_apis.add(api)
            curr_exist = []
            for k in range(19, 31):
                if k == 20:
                    continue
                curr_exist.append(curr_apis['{}'.format(k)])
            if 0 in curr_exist:
                diff_apis += 1
                api_outs += api + '\n'
                counter = Counter(curr_exist)
#                if counter[0] == 1:
#                    absent += 1
                if counter[1] == 1:
                    exclusive += 1
                else:
                    absent += 1
#                for item in curr_exist:
#                    if item == 0:
#                        absent += 1
#                    else:
#                        exclusive += 1
    print(exclusive, absent)
    print('visited:', len(visited_apis))
    print('diff apis:', diff_apis)
    out_path = './evol_induced_apis.txt'
    with open(out_path, 'w') as f:
        f.write(api_outs)

if __name__ == '__main__':
    evol_induced_count()
