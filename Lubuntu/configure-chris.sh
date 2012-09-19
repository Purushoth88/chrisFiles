#!/bin/bash
#
# Do some configurations for chris
#

sudo apt-get install -q --yes terminator

if [ -f ~/.netrc ] ;then
	echo "Don't write ~/.netrc because it already exists"
else
machine meteringd032780bsap.prop.jpaas.sapbydesign.com
login d032780
p	read -s -p "Enter password for d032780 at meteringd032780bsap.prop.jpaas.sapbydesign.com: " epass
	echo -e "machine meteringd032780bsap.prop.jpaas.sapbydesign.com\nlogin d032780\npassword $epass" >> ~/.netrc
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
(cd git && git clone http://github.com/chalstrick/chrisFiles && . chrisFiles/git/setGitConfig.sh)
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
(cd git/metering && git config remote.origin.pushurl https://d032780@git.wdf.sap.corp:8080/NGJP/Services/metering)
