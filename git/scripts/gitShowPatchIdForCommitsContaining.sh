#!/bin/bash

for i in $(git log --all --pretty=format:%H --grep "$1") ;do
	patchId=$(git show $i | git patch-id | cut -d ' ' -f 1 | cut -c1-7)
	logDecorated=$(git log --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)" -1 $i)
	echo "patchId: $patchId, log:$logDecorated"
done
