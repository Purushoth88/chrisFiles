#!/bin/bash

git init
echo 1 > a
git add a
git commit -am 1
echo 2 >> a
git commit -am 2
git checkout -b side HEAD~
echo 3 >> a
git commit -am 3
git merge -s ours master
git checkout master
git merge -s ours side~
echo 6 >> a
git commit -am 6
git checkout side
echo 7 >> a
git commit -am 7


