#!/bin/sh
echo "start updating git repos at " `date`
for i in `find $* \( -name '*.git' -o -name '.git' \) -type d -maxdepth 3`
do
	echo "inspecting repo <$i>"
        if [ -d $i/svn ]
	then
		echo ".. updating by git svn rebase"
		(cd $i/.. ; git svn rebase)
	fi
	for j in `git --git-dir $i remote`
	do
		echo ".. get updates from remote $j by git fetch"
		git --git-dir $i fetch $j
	done
done
echo "finished updating git repos at " `date`
