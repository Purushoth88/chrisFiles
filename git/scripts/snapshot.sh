#!/bin/bash

[ -d .git ] || ( echo ".git folder not found" ; exit -1 )
[ -d .git/snapshot ] || ( mkdir .git/snapshot ; git init .git/snapshot )

for src in $(ls -A) ;do
	[ "$src" == ".git" ] && continue
	rm -fr .git/snapshot/$src
	cp -R $src .git/snapshot/$src
done

for src in $(ls -A .git) ;do
	[ "$src" == "snapshot" ] && continue
	rm -fr .git/snapshot/_git/$src
	cp -R $src .git/snapshot/_git/$src
done

git ls-files --cached -s > .git/snapshot/_git/index
find .git/objects/ -type f -path '[a-f0-9][a-f0-9]/[a-f0-9][a-f0-9]*[a-f0-9]'
(
	cd .git
	for i in $(find objects -type f -path 'objects/[a-f0-9][a-f0-9]/[a-f0-9][a-f0-9]*[a-f0-9]') ;do
		git cat-file -p ${i:8:2}${i:11:38} > .git/snapshot/_git/$i
	done
)
(
	cd .git/snapshot/_git
	git add .
	defname="snapshot taken at $(date)"
	git commit -m "${1-$defname}"
)
