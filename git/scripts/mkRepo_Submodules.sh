#!/bin/sh
# Creates a new git repo for demonstrating submodules

base=$(pwd)
for i in main feature1 feature2 subfeature1a subfeature1b ;do mkdir $i; cd $i; git init; echo $i>content.txt; git add content.txt; git commit -m "initial in $i" ; cd ..;done
cd main
../snapshot.sh "after initializing all repos"
for i in feature1 feature2 ;do git submodule add $base/$i $i ;done
../snapshot.sh "after adding submodules feature1, feature2 to main"
git commit -m "adding feature1, feature2 to main"
../snapshot.sh "after commiting the addition of submodules"
cd ..
cd feature1
for i in subfeature1b subfeature1a ;do git submodule add $base/$i $i ;done
git commit -m "adding subfeature1a and subfeature1b to feature1"
cd ../main
git submodule update --recursive
../snapshot.sh "after modifying all submodule feature1 and calling submodule update recursive"
git submodule foreach --recursive git pull
../snapshot.sh "after calling git submodule foreach recurssive git pull"
cd feature1
echo B > b
git add b
git commit -m "adding b"
cd subfeature1b
echo C > c
git add c
git commit -m "adding c"
cd ../..
git submodule update --recursive
../snapshot.sh "after changing feature1,subfeature1b and calling git submodule update"
