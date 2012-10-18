#!/bin/sh
# Creates a new git repo for demonstrating submodules

base=$(pwd)
for i in main feature1 feature2 subfeature1a subfeature1b ;do mkdir $i; cd $i; git init; echo $i>content.txt; git add content.txt; git commit -m "initial in $i" ; cd ..;done
cd main
for i in feature1 feature2 ;do git submodule add $base/$i $i ;done
git commit -m "adding feature1 and feature2 to main"
cd ..
cd feature1
for i in subfeature1b subfeature1a ;do git submodule add $base/$i $i ;done
git commit -m "adding subfeature1a and subfeature1b to feature"
cd ..
cd main
git submodule update
