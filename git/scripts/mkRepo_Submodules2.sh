#!/bin/bash
# Creates a new git repo for demonstrating submodules
set -x 

# create all the independent repos
for i in root child ;do
  rm -fr $i
  mkdir -p $i
  git init $i
  cd $i
  echo $i>content.txt
  git add content.txt
  git commit -m "initial in $i"
  cd ..
done

# start a git daemon serving all the repos (only if it is not already running)
git ls-remote git://localhost/root || git daemon --export-all --base-path="$(pwd)" &
sleep 5

# add two submodules feature1,feature2 to repo "main"
cd root
git submodule add git://localhost/child plugins/child
git commit -m "adding child submodule" 
cd ..

# clone the repo into root-clone folder
rm -fr root-clone
git clone --recursive git://localhost/root root-clone

# Now update the child repo, make sure new commit is not visible from refs/heads/*
cd child
echo "new content in submodule">content.txt
git commit -a -m "modfied child"
git update-ref refs/changes/newChange HEAD
git reset --hard HEAD~
cd ..

# Update the root repo to point to the new commit
cd root/plugins/child
git fetch git://localhost/child refs/changes/newChange 
git checkout FETCH_HEAD
cd ../..
git add plugins/child
git commit -a -m "updated the submodule"
cd ..

# try to update your clone -> will fail
cd root-clone
git pull --recurse-submodules
GIT_TRACE_PACKET=1 GIT_TRACE=1 git submodule update
cd ..

# fix it by explicitly fetch
cd root-clone
cd plugins/child
git fetch origin refs/changes/newChange
cd ../..
git submodule update 
