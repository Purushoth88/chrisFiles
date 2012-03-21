#!/bin/sh
# Creates a new git repo for demonstrating git features.

if [ -d .git ] ;then
	echo "fatal: git repo already exists. Run in empty dir or use -f "
	exit 2
fi

git init

echo 'a
b
c
d
e
f
g
h
i
j
k' > a
git add a
git commit -m "initial"
			
git branch initial
git branch side
git branch side1
git branch side2
git branch side3

sed -i 's/b/b(master)/' a
git commit -a -m "master changes b"

git checkout side1
sed -i 's/b/b(side)/' a
git commit -a -m "side changes b"

git checkout side2
sed -i 's/c/c(side)/' a
git commit -a -m "side changes c"

git checkout side3
sed -i 's/d/d(side)/' a
git commit -a -m "side changes d"

git checkout side4
sed -i 's/e/e(side)/' a
git commit -a -m "side changes e"

git checkout side5
sed -i 's/f/f(side)/' a
git commit -a -m "side changes f"

git checkout side6
sed -i 's/g/g(side)/' a
git commit -a -m "side changes g"

