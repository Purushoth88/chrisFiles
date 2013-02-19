#!/bin/sh
git init
touch f
git add f
git commit -m add_f
git checkout -b side
touch g
git add g
git commit -m add_g
git checkout master
echo 2 > f
git commit -a -m modify_f
mkdir g
git status
#git merge side
