#!/bin/sh
git log --pretty=format:'%T %h %s' | while read tree commit subject ;do
    git ls-tree -r $tree | grep "$1" | while read mode type blob path ;do
        echo "Blob: path:$path, mode:$mode, type:$type, id:$blob, Commit: id:$commit, subject:\"$subject\""
    done
done
