#!/bin/bash
# Creates a new git repo for demonstrating submodules

rm -fr server client
# create root, child, nonPrjChild as independent repos 
for i in root child nonPrjChild ;do
  mkdir -p server/$i
  git init server/$i
  cd server/$i
  echo $i>content.txt
  git add content.txt
  git commit -m "initial in $i"
  cd ../..
done
# create root, child should contain eclipse projects
for i in root child ;do
  cd server/$i
  echo '<?xml version="1.0" encoding="UTF-8"?><projectDescription><name>'$i'</name></projectDescription>'>.project
  git add .project
  git commit -m "add .project in $i"
  cd ../..
done

# start a git daemon serving all the repos (only if it is not already running)
git ls-remote git://localhost/root || ( git daemon --export-all --base-path="$(pwd)/server" & ; sleep 5; )

# add two submodules feature1,feature2 to repo "main"
cd server/root
git submodule add git://localhost/child plugins/child
git submodule add git://localhost/child plugins/nonPrjChild
git commit -m "adding child, nonPrjChild submodules" 
cd ../..

# clone the root repo recursively
git clone --recursive git://localhost/root client/root

# Now update the child repo, make sure new commit is not visible from refs/heads/*
cd server/child
echo "new content in submodule">content.txt
git commit -a -m "modfied child"
git update-ref refs/changes/newChange HEAD
git reset --hard HEAD~
cd ..

# Update the root repo to point to the new commit
cd server/root/plugins/child
git fetch git://localhost/child refs/changes/newChange 
git checkout FETCH_HEAD
cd ../..
git add plugins/child
git commit -a -m "updated the submodule"
cd ../..

# try to update your clone -> will fail
cd client/root
git pull --recurse-submodules
GIT_TRACE_PACKET=1 GIT_TRACE=1 git submodule update
cd ../..

# fix it by explicitly fetch
cd client/root/plugins/child
git fetch origin refs/changes/newChange
cd ../..
git submodule update 
cd ../..

