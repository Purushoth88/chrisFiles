#!/bin/bash
for i in `git tag -l change/*/* | sort -t/ -n -k 2 -k 3 -r`
do
	if [ ${i%/*} == ${lastProp-""} ]; then
		git tag -d $i
	fi
	lastProp=${i%/*}
done
