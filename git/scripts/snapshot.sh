#!/bin/bash

[ -d .git ] || ( echo ".git folder not found" ; exit -1 )
[ -d .git/snapshot ] || { mkdir -p .git/snapshot/_git ; git init .git/snapshot; touch -d "1974-01-01 00:00:00" .git/snapshot }

find . -newer .git/snapshot -a ! -path './.git/snapshot*' | while read file ;do
	echo "file:<<$file>>"
	tgt=".git/snapshot/${file/#.\/.git\//_git/}"
	if [ -d "$file" ] ;then
		[ -f "$target" ] && rm -f "$target"
		[ -e "$target" ] || { mkdir "$target" ; continue }
		if [ -d "$target" ] ;then
			find "$(target)" -depth -maxdepth 1 | while read child ;do
				[ -e "$file/$child" ] || { git rm "$target/$child" ; continue }
			done
			continue
		fi
	else
		[ -e "$target" ] || ( cd .git/snapshot; git add "${target:14}" )
		[ "$file" = ./.git/index ] && { git ls-files --cached -s > "$tgt" ; continue }
		[[ "$file" =~ ./.git/objects/[0-9a-f]{2}/[0-9a-f]{38}$ ]] &&  { git cat-file -p ${file:15:2}${file:18:38} > "$tgt" ; continue }
		cp "$file" "$tgt"
	fi
done
touch .git/snapshot
(
	cd .git/snapshot
	git commit -m "${1-snapshot taken at $(date)}"
)
