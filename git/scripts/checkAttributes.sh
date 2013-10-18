#!/bin/sh
# test different jgit versions regarding how they interprete the status of a 
# git repo using keyword expansion
set -x

# get jgit 2.0 and 3.1
curl -o jgit.2.0.sh http://search.maven.org/remotecontent?filepath=org/eclipse/jgit/org.eclipse.jgit.pgm/2.0.0.201206130900-r/org.eclipse.jgit.pgm-2.0.0.201206130900-r.sh
curl -o jgit.3.1.sh http://search.maven.org/remotecontent?filepath=org/eclipse/jgit/org.eclipse.jgit.pgm/3.1.0.201310021548-r/org.eclipse.jgit.pgm-3.1.0.201310021548-r.sh

# clone a test repo
git clone https://github.com/chalstrick/testStrangeAttributes.git

# do a status with native git, jgit2.0 and jgit3.1
( cd testStrangeAttributes 
ls -la alg.code .git/index
../jgit.2.0.sh diff
../jgit.3.1.sh diff
git diff )

# touch the index file to avoid smudged entries
touch testStrangeAttributes/.git/index
( cd testStrangeAttributes 
ls -la alg.code .git/index
../jgit.2.0.sh diff
../jgit.3.1.sh diff
git diff )

