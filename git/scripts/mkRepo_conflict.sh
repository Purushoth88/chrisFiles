#!/bin/bash
set -x

git init
echo -e 'line1\nline2\nline3' > a
echo -e 'line1\nline2\nline3' > b 
git add a b 
git commit -m "adding a,b"
git checkout -b side
echo -e 'line1\nline2(side)\nline3' > a
echo -e 'line1\nline2\nline3(side)' > b 
git commit -a -m "modifying a,b on side"
git checkout master
echo -e 'line1\nline2(master)\nline3' > a
echo -e 'line1(master)\nline2\nline3' > b 
git commit -a -m "modifying a,b on master"
git merge side
