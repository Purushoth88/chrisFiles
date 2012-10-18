#!/bin/bash

git init
echo -e 'line1 \nline2 \nline3 ' > file.txt
git add file.txt
git commit -m "adding file.txt"
git checkout -b side
sed -i 's/line3/side_content/' file.txt
git commit -a -m "modification on side branch"
git checkout master
sed -i 's/line1/master_content/' file.txt
git commit -a -m "modification on master branch"
git merge side
git checkout side
git merge master@{1}
echo "site_content2" >> file.txt
git commit -a -m "another modification on side branch"
git checkout master

git merge-base --all master side
git log --oneline --decorate HEAD..side
