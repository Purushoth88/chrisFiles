#/!bin/sh

git repack -a -d
git prune-packed
