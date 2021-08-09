#!/usr/env bash

curr_dir='/Users/pliu0032/Documents/android-dev/AndroMevol/res/official'
src_field_dir='/Users/pliu0032/Documents/Github/Mevol/outs'
src_method_dir='/Users/pliu0032/Documents/Github/Mevol/outs'
END=30
for ((i=19;i<=END;i++)); do
    if [ $i -eq 20 ];
    then
        continue
    else
        dirname=$curr_dir/framework-$i
        echo $dirname
        if [ -d "$dirname" ];
        then
            cp $src_field_dir/framework-$i/fields.txt $dirname/fields.txt
            cat $src_method_dir/framework-$i/methods.txt $src_method_dir/framework-$i/methods.hide.txt > $dirname/methods.txt
        else
            mkdir $dirname
            cp $src_field_dir/android-$i.txt $dirname/fields.txt
            cp $src_method_dir/android-$i.txt $dirname/methods.txt
        fi
        echo $i
    fi
done
