#!/bin/sh
for i in `find /home/git/repositories -name '*.git' -type d`
do
	if [ -f $i/git-daemon-export-ok ]
	then
		reponame=`basename $i .git`
		if [ "$reponame" == ".git" ] 
		then
			reponame=`basename \`dirname $i\``
		fi
		echo "repo.url="$reponame
		echo "repo.path="$i
	fi
done
