#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import csv

def level_api_extract(line):
    pos = line.find(':[')
    if '<android.view.View: void onStartTemporaryDetach()>' in line:
        print('contain the method:')
    return line[:pos].strip()

def txt_load(txt_path):
    with open(txt_path) as f:
        lines = [line.strip() for line in f.readlines()]
    apis = set()
    for line in lines:
        apis.add(level_api_extract(line))
    return lines, set(apis)

def csv_load(csv_path):
    rows = []
    mapis = set()
    fapis = set()
    with open(csv_path) as f:
        for idx, row in enumerate(csv.reader(f)):
            rows.append(row)
            if idx == 0:
                continue
            if '(' in row[1] and ')' in row[1]:
                mapis.add(row[1])
            else:
                fapis.add(row[1])
    return rows, mapis, fapis

def csv_dump(csv_path, csv_content):
    with open(csv_path, 'w') as f:
        writer = csv.writer(f)
        writer.writerows(csv_content)

def device_evol_merge():
    evol_method_life_path = './android_api_lifetime_trim.txt'
    evol_field_life_path = './android_field_lifetime_trim.txt'
    device_life_path = '/Users/pliu0032/Documents/Github/AndroMevol/res/device_specific_apis.csv'
    evol_methods, evol_mapis = txt_load(evol_method_life_path)
    evol_fields, evo_fapis = txt_load(evol_field_life_path)
    device_mfs, dev_mapis, dev_fapis = csv_load(device_life_path)
    device_evol_out_path = '/Users/pliu0032/Documents/Github/AndroMevol/res/device_evol_specific_apis.csv'
    for method in evol_methods:
        device_mfs.append([method])
    for field in evol_fields:
        device_mfs.append([field])
    csv_dump(device_evol_out_path, device_mfs)
    if '<android.view.View: void onStartTemporaryDetach()>' in evol_mapis:
        print('contain the method:')
    else:
        print('###################')
    print('length :', len(dev_mapis))
    print('union set length:', len(dev_mapis | evol_mapis))
    incom_apis = dev_mapis | evol_mapis
    with open('/Users/pliu0032/Documents/Github/AndroMevol/res/dev_evol_imcomp_methods.txt', 'w') as f:
        f.write('\n'.join(list(incom_apis)))

if __name__ == '__main__':
    device_evol_merge()
