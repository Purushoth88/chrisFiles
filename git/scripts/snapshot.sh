#!/bin/bash
[ -d .git ] || { echo ".git folder not found"; exit -1; }
[ -d .git/snapshot ] || { mkdir -p .git/snapshot/_git; git init .git/snapshot; touch -d "1974-01-01 00:00:00" .git/snapshot; }
find . -newer .git/snapshot -a ! -path './.git/snapshot/*' | while read src; do
	tgt=".git/snapshot/${src/#.\/.git\//_git/}"
	[ "$tgt" = ".git/snapshot/./.git" ] && tgt=".git/snapshot/_git"
	path=${tgt:14}
#	echo "src=<<<$src>>>, tgt=<<<$tgt>>>, path=<<<$path>>>"
	if [ -d "$src" ]; then
		[ -f "$tgt" ] && rm -f "$tgt"
		[ -e "$tgt" ] || { mkdir "$tgt"; continue; }
		[ -d "$tgt" ] && find "$tgt" -depth -maxdepth 1 -mindepth 1 -printf "%f\n" | while read child; do
#			echo "looking at child: <<<$child>>>"
			[ "$src/$child" == "./_git" ] && continue
			[ -e "$src/$child" ] || { cd .git/snapshot; git rm -r "$path/$child"; cd ../..; }
		done
	else
		[ "$src" = ./.git/index ] && { git ls-files --cached -s > "$tgt"; continue; }
		[[ "$src" =~ ./.git/objects/[0-9a-f]{2}/[0-9a-f]{38}$ ]] && { git cat-file -p ${src:15:2}${src:18:38} > "$tgt"; continue; }
		cp "$src" "$tgt"
	fi
done
touch .git/snapshot
(
	cd .git/snapshot
	git add .
	git commit -m "${1-snapshot taken at $(date)}" --allow-empty
	git log --numstat -1
)
