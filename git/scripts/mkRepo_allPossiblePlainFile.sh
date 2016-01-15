#!/bin/bash
#
# Create a git repo with test files to test git's merge/checkout
# behaviour. Must be called in an empty folder 

die() { echo "$@" 1>&2 ; exit 1; }
[ ! "$(ls -A)" ] || die "FATAL: the current folder is not empty"

# prepare initial commit
git init
for h in 0 a ;do
	for i in 0 a b ;do
		[[ $i == b && $h != a ]] && continue
		for w in 0 a b c ;do
			[[ $w == b && $i != a && $h != a ]] && continue
			[[ $w == c && $i != b ]] && continue
			for m in 0 a b c d ;do
				[[ $m == b && $w != a && $i != a && $h != a ]] && continue
				[[ $m == c && $w != b && $i != b ]] && continue
				[[ $m == d && $w != c ]] && continue
				echo "$h$i$w$m: a" >$h$i$w$m
				git add $h$i$w$m
				rm $h$i$w$m
			done
		done
	done
done
git commit -m initial

# prepare HEAD
git rm --cached '*'
for h in 0 a ;do
	for i in 0 a b ;do
		[[ $i == b && $h != a ]] && continue
		for w in 0 a b c ;do
			[[ $w == b && $i != a && $h != a ]] && continue
			[[ $w == c && $i != b ]] && continue
			for m in 0 a b c d ;do
				[[ $m == b && $w != a && $i != a && $h != a ]] && continue
				[[ $m == c && $w != b && $i != b ]] && continue
				[[ $m == d && $w != c ]] && continue
				if [[ $h != 0 ]] ;then
					echo "$h$i$w$m: $h" >$h$i$w$m
					git add $h$i$w$m
					rm $h$i$w$m
				fi
			done
		done
	done
done
git commit -m head

# prepare MERGE
git checkout -b merge HEAD~
git rm --cached '*'
for h in 0 a ;do
	for i in 0 a b ;do
		[[ $i == b && $h != a ]] && continue
		for w in 0 a b c ;do
			[[ $w == b && $i != a && $h != a ]] && continue
			[[ $w == c && $i != b ]] && continue
			for m in 0 a b c d ;do
				[[ $m == b && $w != a && $i != a && $h != a ]] && continue
				[[ $m == c && $w != b && $i != b ]] && continue
				[[ $m == d && $w != c ]] && continue
				if [[ $m != 0 ]] ;then
					echo "$h$i$w$m: $m" >$h$i$w$m
					git add $h$i$w$m
					rm $h$i$w$m
				fi
			done
		done
	done
done
git commit -m merge

# prepare index & working tree
git checkout master
git rm --cached '*'
for h in 0 a ;do
	for i in 0 a b ;do
		[[ $i == b && $h != a ]] && continue
		for w in 0 a b c ;do
			[[ $w == b && $i != a && $h != a ]] && continue
			[[ $w == c && $i != b ]] && continue
			for m in 0 a b c d ;do
				[[ $m == b && $w != a && $i != a && $h != a ]] && continue
				[[ $m == c && $w != b && $i != b ]] && continue
				[[ $m == d && $w != c ]] && continue
				if [[ $i != 0 ]] ;then
					echo "$h$i$w$m: $i" >$h$i$w$m
					git add $h$i$w$m
					rm $h$i$w$m
				fi
				if [[ $w != 0 ]] ;then
					echo "$h$i$w$m: $w" >$h$i$w$m
				fi
			done
		done
	done
done
git status

