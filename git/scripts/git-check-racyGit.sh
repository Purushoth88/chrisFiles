#!/bin/bash

rm -fr testRacy
git init testRacy
cd testRacy

echo `date`

echo "clean" > a
echo "clean" > b
sleep 2
git add a b
git commit -m initial

echo "Status after initial commit"
#ls -l --time-style=+%T  a b .git/index
ls -l --time-style=full-iso  a b .git/index
git ls-files --debug -s -- a b
~/git/jgit/org.eclipse.jgit.pgm/target/jgit debug-show-dir-cache
 
echo "mod1" > a
git add a

# echo "Status after mod+add a" 
# ls -l --time-style=+%T  a b .git/index
# git ls-files --debug -s -- a b
# ~/git/jgit/org.eclipse.jgit.pgm/target/jgit debug-show-dir-cache
 
echo "mod2" > b
git add b

echo "Status after mod+add b" 
#ls -l --time-style=+%T  a b .git/index
ls -l --time-style=full-iso  a b .git/index
git ls-files --debug -s -- a b
~/git/jgit/org.eclipse.jgit.pgm/target/jgit debug-show-dir-cache
