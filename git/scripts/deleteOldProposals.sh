#!/bin/bash
#
# Delete unneeded refs

oldChangeNr="0"
oldPatchSet="0"
git show-ref | cut  -d ' ' -f 2 | grep '^refs/[^/]\+/change/[0-9]\+/[0-9]\+$' | sort -n -t/ -k4 -k5 -r | while read ref ;do
	change=${ref##refs/*/change/}
 	changeNr=${change%%/*}
	patchSet=${change##*/}
	echo "found patcheset $patchSet of change $changeNr. ref=$ref."
	if [[ "$changeNr" == "$oldChangeNr" ]] && [[ "$patchSet" -lt "$oldPatchSet" ]] ;then
		echo "delete patcheset $patchSet of change $changeNr."
		if [[ $ref == refs/heads/* ]] ;then
			git branch -D ${ref##refs/heads/}
		elif [[ $ref == refs/tags/* ]] ;then
			git tag -d ${ref##refs/tags/}
		fi
	fi
	oldPatchSet=$patchSet
	oldChangeNr=$changeNr
done

