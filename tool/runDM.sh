#!/bin/bash

device_path='../frameworks'

for repo in `ls $device_path`;
do
    repo_path=$device_path'/'$repo
    for framework in `ls $repo_path`;
    do
        framework_path=$repo_path'/'$framework
        for dex in `ls $framework_path`;
        do
            dex_path=$framework_path'/'$dex
            java -jar ./AndroMevol.jar $dex_path
        done
    done
done

python api_cid_input.py
