#!/bin/bash
set -x
git init stashTest
cd stashTest/
echo -e '1\n2\n3\n' >a
git add a
git commit -m addA
echo -e '1 - modified\n2\n3\n' >a
git add a
echo -e '1 - modified\n2\n3 - modified\n' >a
git stash
cat a 
echo -e '1\n2 - modified\n3\n' >a
git stash apply    # this will fail
git commit -a -m workInProgress
git stash apply    # now, with a clean workingtree, I get more info
cat a
