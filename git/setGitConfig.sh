#!/bin/bash
#
# Set my favourite config params for git 
#

git config --global user.name "Christian Halstrick"
git config --global user.email christian.halstrick@sap.com
# git config --global core.editor "'C:/notepad++/notepad++.exe' -multiInst -notabbar -nosession -noPlugin"
git config --global alias.lo 'log --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.los 'log --numstat --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.loa 'log --all --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.loas 'log --numstat --all --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.co checkout
git config --global alias.st 'status -s'
git config --global alias.cm commit
git config --global alias.br branch
git config --global alias.rl 'reflog --date=local'
git config --global alias.addchange '!bash -c '\''git config --add remote."$1".fetch refs/changes/"${2: -2}"/"$2"/*:refs/tags/"$1"/"$2"/*'\'' -'
git config --global alias.getchange '!bash -c '\''git fetch "$1" refs/changes/"${2: -2}"/"$2"/*:refs/tags/"$1"/"$2"/*'\'' -'
git config --global http.sslverify false
git config --global color.ui auto
git config --global alias.snap '!bash -c '\''~/git/chrisFiles/git/scripts/snapshot.sh $*'\'' -'
git config --global alias.lssnap '!bash -c '\''~/git/chrisFiles/git/scripts/lssnap.sh $*'\'' -'
