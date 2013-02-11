#!/bin/bash
#
# Do some configurations for chris
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	[ -d "$2" ] || mkdir -p "$2"	
	if git rev-parse --resolve-git-dir "$1/.git" >/dev/null 2>&1 ;then
		git fetch --git-dir "$1/.git" -q --all	
		if [ `git rev-parse --symbolic-full-name --abbrev-ref HEAD` == "master" ] ;then
			git pull -q	
		fi
	else
		git clone -q "$1" "$2"
	fi
	if [ ! -z "$3" ] ;then
		git config -f "$2/.git/config" remote.origin.push HEAD:refs/for/$3 
		if [ ! -f "$2/.git/hooks/commit-msg" ] ;then 
			echo "Won't download a commit-msg hook for repo $2 because such a hook already exists"
		else
			curl -o "$2/.git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg
			chmod +x "$2/.git/hooks/commit-msg"
		fi
	fi
}

sudo apt-get install -q --yes terminator

if [ -f ~/.netrc ] ;then
	echo "Don't write ~/.netrc because it already exists"
else
	read -s -p "Enter password for d032780 at meteringd032780bsap.prod.jpaas.sapbydesign.com: " epass
	echo -e "machine meteringd032780bsap.prod.jpaas.sapbydesign.com\nlogin d032780\npassword $epass" >> ~/.netrc
	echo
	read -s -p "Enter password for P1323864547 at servermetermeter-meter.prod.jpaas.sapbydesign.com: " epass
	echo -e "machine servermetermeter-meter.prod.jpaas.sapbydesign.com\nlogin P1323864547\npassword $epass" >> ~/.netrc
	echo
	read -s -p "Enter password for d032780 at git.wdf.sap.corp: " epass
	echo -e "machine git.wdf.sap.corp\nlogin d032780\npassword $epass" >> ~/.netrc
	echo
	read -s -p "Enter password for chalstrick at git.eclipse.org: " epass
	echo -e "machine git.eclipse.org\nlogin chalstrick\npassword $epass" >> ~/.netrc
	echo
	read -s -p "Enter password for chalstrick at github.com: " epass
	echo -e "machine github.com\nlogin chalstrick\npassword $epass" >> ~/.netrc
	chmod 600 ~/.netrc
fi

cloneOrFetch http://github.com/chalstrick/chrisFiles ~/git/chrisFiles && . ~/git/chrisFiles/git/setGitConfig.sh

if [ -f ~/.vimrc ] ;then
	echo "Don't write ~/.vimrc because it already exists"
else
	cp ~/git/chrisFiles/vim/_vimrc ~/.vimrc
fi

git config -f ~/git/jgit/.git/config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/jgit/jgit.git
GIT_DIR=~/git/jgit && git remote add github https://chalstrick@github.com/chalstrick/jgit.git && git fetch github 
git config -f ~/git/egit/.git/config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit.git
GIT_DIR=~/git/egit && git remote add github https://chalstrick@github.com/chalstrick/egit.git && git fetch github 
git config -f ~/git/egit-pde/.git/config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit-pde.git
git config -f ~/git/egit-github/.git/config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit-github.git
