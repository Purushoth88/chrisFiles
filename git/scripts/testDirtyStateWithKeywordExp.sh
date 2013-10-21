#!/bin/sh
# test different jgit versions regarding how they interprete the status of a 
# git repo using keyword expansion

set -x

# get jgit 2.0 and 3.1
wget -nc -O ./jgit.2.0.sh http://search.maven.org/remotecontent?filepath=org/eclipse/jgit/org.eclipse.jgit.pgm/2.0.0.201206130900-r/org.eclipse.jgit.pgm-2.0.0.201206130900-r.sh
wget -nc -O ./jgit.3.1.sh http://search.maven.org/remotecontent?filepath=org/eclipse/jgit/org.eclipse.jgit.pgm/3.1.0.201310021548-r/org.eclipse.jgit.pgm-3.1.0.201310021548-r.sh
chmod +x ./jgit.2.0.sh ./jgit.3.1.sh

# exit if the directory testStrangeAttributes already exist
[ -d testStrangeAttributes ] && exit 1

# clone a test repo and print some low-level status.
# notice that in the index the entries are smudged (len=0)
git clone https://github.com/chalstrick/testStrangeAttributes.git
cd testStrangeAttributes
ls -l --full-time .git/index alg.code
../jgit.3.1.sh debug-show-dir-cache

# get the diff with jgit.2.0 and jgit.3.1 : both jgits report changes
../jgit.2.0.sh diff
../jgit.3.1.sh diff

# check the status with native git
# see how a git status modifies the index by unsmuding files
git status
ls -l --full-time .git/index alg.code
../jgit.3.1.sh debug-show-dir-cache

# get the diff with jgit.2.0 and jgit.3.1
../jgit.2.0.sh diff
../jgit.3.1.sh diff

cd ..
