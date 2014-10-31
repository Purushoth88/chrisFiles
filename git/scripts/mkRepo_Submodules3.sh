#!/bin/bash
# Creates a git repo's for demonstrating submodules

serverUrl=file://$PWD/server
rm -fr server client

# create all needed repos 
for i in rootPrj child1_ModulePrj child2_Module child3_Module ;do
  git init server/$i
  cd server/$i
  echo $i>content.txt
  git add content.txt
  git commit -m "initial in $i"
  cd -
done

# create eclipse projects
for i in rootPrj child1_ModulePrj rootPrj/child4_Prj ;do
  mkdir -p server/$i
  cd server/$i
  [ -f content.txt ] || echo $i>content.txt
  echo '<?xml version="1.0" encoding="UTF-8"?><projectDescription><name>'ProjectIn$(basename $i)'</name></projectDescription>'>.project
  git add .project content.txt
  git commit -m "add .project in $i"
  cd -
done

# add two submodules feature1,feature2 to repo "main"
cd server/child2_Module
git submodule add $serverUrl/child3_Module child3_Module
git commit -m "adding submodules" 
cd -
cd server/rootPrj
git submodule add $serverUrl/child1_ModulePrj child1_ModulePrj
git submodule add $serverUrl/child2_Module child2_Module
git commit -m "adding submodules" 
cd -

# clone the root repo recursively
git clone --recursive --no-local $serverUrl/rootPrj client/rootPrj 

# 
cd client/rootPrj
git checkout -b side1
touch a
git add a
git commit -m addA
git checkout -b side2 master
touch b
git add b
git commit -m addB
cd child1_ModulePrj
touch c
git add c
git commit -m addC
cd -
