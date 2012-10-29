#!/bin/bash
srcRepo="$(git rev-parse --show-toplevel)"
[ -d "$srcRepo" ] || { echo "not in a non-bare git repo"; exit -1; }
tgtRepo="$srcRepo/.git/snapshot"
[ -d "$tgtRepo" ] || { git init -q "$tgtRepo"; touch -d "1974-01-01 00:00:00" "$tgtRepo/.git/ts"; }

# remove obsolete files/folders
find "$tgtRepo" -depth ! -path "$tgtRepo/.git/*" | while read tgt; do
	#echo "inspecting tgt:<<<$tgt>>> for deletion of obsolete files/folders"
	[ "$tgt" = "$tgtRepo/.git" ] && continue
	tgtPath="${tgt:$((${#tgtRepo}+1))}"
	srcPath="${tgtPath//_git/.git}"
	src="$srcRepo/$srcPath"
	#echo "src:<<<$src>>>, srcPath:<<<$srcPath>>>, tgtPath:<<<$tgtPath>>>"
	if [ -d "$tgt" ]; then
		[ -d "$src" ] || ( cd "$tgtRepo"; rm -fr "$tgtPath"; )
	else
		[ -f "$src" ] || ( cd "$tgtRepo"; git rm --cached -q "$tgtPath"; rm -f "$tgt"; )
	fi
done

# copy/describe new/modified files
find "$srcRepo" -newer "$tgtRepo/.git/ts" -a ! -path "$tgtRepo/*" | while read src; do
	#echo "inspecting src:<<<$src>>> for copy/describe new/modified files"
	[ "$src" = "$tgtRepo" ] && continue
	srcPath="${src:$((${#srcRepo}+1))}"
	tgtPath="${srcPath//.git/_git}"
	tgt="$tgtRepo/$tgtPath"
	#echo "tgt:<<<$tgt>>>, srcPath:<<<$srcPath>>>, tgtPath:<<<$tgtPath>>>"
	if [ -d "$src" ]; then
		[ -d "$tgt" ] || mkdir -p "$tgt"
		continue
	fi
	[[ "$tgtPath" =~ _git/(modules/[^/]+/)?index ]] && { git --git-dir="$(dirname $src)" ls-files --cached -s > "$tgt"; continue; }
	[[ "$tgtPath" =~ _git/(modules/[^/]+/)?objects/[0-9a-f]{2}/[0-9a-f]{38}$ ]] && { git --git-dir="$(dirname $src)/../.." cat-file -p ${tgtPath: -41:2}${tgtPath: -38:38} > "$tgt"; continue; }
	cp "$src" "$tgt"
done

touch "$tgtRepo/.git/ts"
(
	cd "$tgtRepo"
	git add .
	if [ "$1" = "-v" ]; then
		git commit -q -m "${2-snapshot taken at $(date)}" --allow-empty
		git log --format='*** Snapshot: %s (%cd) %h ***' -p --word-diff -1 | grep -v '^\(---\|+++\|new file mode\|deleted file mode\|index\|@@\) ' | sed 's/^diff --git.* b\//File: /'
	else
		git commit -q -m "${1-snapshot taken at $(date)}" --allow-empty
		git log --format='*** Snapshot: %s (%cd) %h ***' --numstat -1
	fi
	echo "*** End Snapshot ***"
)
