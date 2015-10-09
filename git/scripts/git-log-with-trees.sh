#!/bin/sh
git log --pretty=format:'%T %h %s' $* | while read tree commit subject ;do
    echo "Commit id:$commit, tree:$tree, subject:\"$subject\""
    git ls-tree -r $tree | while read mode type blob path ;do
        echo "  Blob: path:$path, mode:$mode, type:$type, id:$blob"
    done
done
