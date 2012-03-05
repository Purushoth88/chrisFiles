#!/bin/sh

gitDir=$(readlink -f $(git rev-parse --git-dir))

for ref in $(git for-each-ref --format='%(refname)')
do
	echo ..$ref..
done
