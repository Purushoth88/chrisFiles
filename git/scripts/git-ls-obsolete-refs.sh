#!/bin/sh

gitDir=$(git rev-parse --git-dir)
for i in $(find $gitDir/refs/heads)
do
	echo $i
done

