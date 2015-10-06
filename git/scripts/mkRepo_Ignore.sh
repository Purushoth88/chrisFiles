#!/bin/sh
set -x

git init
echo 1 >a
echo 1 >b
git add a b
git commit -m "adding a,b"

git checkout -b branch 
echo 2 >a
echo 2 >c
git add a c
git commit -m "modify a, adding c"

git checkout master
echo 1 >c
ls -la 
cat c
git status
#echo c >.gitignore
git status

jgit.sh checkout branch
