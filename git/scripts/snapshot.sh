#!/bin/bash

[ -d .git ] || ( echo ".git folder not found" ; exit -1 )
[ -d .git/snapshot ] || ( mkdir -p .git/snapshot/_git ; git init .git/snapshot; touch -d "1974-01-01 00:00:00" .git/.snapshot_tmsp )

find . -newer .git/.snapshot_tmsp -a ! -path './.git/snapshot*' | while read file ;do
	echo "file:<<$file>>"
	tgt=".git/snapshot/${file/#.\/.git\//_git/}"
	if [ -d "$file" ] ;then
		mkdir "$tgt"
	else
		[ "$file" = ./.git/index ] && git ls-files --cached -s > "$tgt" && continue
		[[ "$file" =~ ./.git/objects/[0-9a-f]{2}/[0-9a-f]{38}$ ]] && git cat-file -p ${file:15:2}${file:18:38} > "$tgt" && continue
		cp "$file" "$tgt"
	fi
done
(
	cd .git/snapshot
	find . -depth -a ! -path './.git/*' | while read file ;do
		src="../../${file/#.\/_git\//.git/}"
		echo "tgt=<<<$file>>>, src=<<<$src>>>"
		if [ ! -e "$src" ] ;then
			rm "$file"
		fi
	done
)
touch .git/.snapshot_tmsp
(
	cd .git/snapshot
	git add .
	defname="snapshot taken at $(date)"
	git commit -m "${1-$defname}"
)
