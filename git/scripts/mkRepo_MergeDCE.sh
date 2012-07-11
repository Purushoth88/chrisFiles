#!/bin/bash

git init
echo orig > onlyMaster
echo orig > onlySide
echo -e '1\n2\n3' > bothMergeable
echo orig > bothConflicting
echo orig > nobody
sleep 1s
git add onlyMaster onlySide bothMergeable bothConflicting nobody
git commit -m initial

echo master > onlyMaster
echo -e '1master\n2\n3' > bothMergeable
echo master > bothConflicting
sleep 1s
git commit -a -m master

strace -e trace=desc,file -o checkout.strace git checkout -b side HEAD~
echo side > onlySide
echo -e '1\n2\n3side' > bothMergeable
echo side > bothConflicting
sleep 1s
git commit -a -m side

strace -e trace=desc,file -o merge.strace git merge master

