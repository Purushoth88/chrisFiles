#!/bin/bash
#
# Do some configurations for chris
#

git config --global user.name "Christian Halstrick"
git config --global user.email "christian.halstrick@gmail.com"
if [ -f ~/.netrc ] ;then
	echo "Don't write ~/.netrc because it already exists"
else
	read -s -p "Enter password for chalstrick at git.eclipse.org: " epass
	echo -e "machine git.eclipse.org\nlogin chalstrick\npassword $epass" >> ~/.netrc
	read -s -p "Enter password for chalstrick at github.com: " epass
	echo -e "machine github.com\nlogin chalstrick\npassword $epass" >> ~/.netrc
	chmod 600 ~/.netrc
fi
(cd git && git clone http://github.com/chalstrick/chrisFiles)
if [ -f ~/.gitconfig ] ;then
	echo "Don't write ~/.gitconfig because it already exists"
else
	cp git/chrisFiles/git/.gitconfig ~
fi
if [ -f ~/.vimrc ] ;then
	echo "Don't write ~/.vimrc because it already exists"
else
	cp git/chrisFiles/vim/_vimrc ~/.vimrc
fi

(cd git/jgit && git config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/jgit/jgit)
(cd git/jgit && git remote add github http://chalstrick@github.com/chalstrick/jgit && git fetch github) 
(cd git/egit && git config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit)
(cd git/egit && git remote add github http://chalstrick@github.com/chalstrick/egit && git fetch github) 
(cd git/egit-pde && git config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit-pde)
(cd git/egit-github && git config remote.origin.pushurl https://chalstrick@git.eclipse.org/r/p/egit/egit-github)
