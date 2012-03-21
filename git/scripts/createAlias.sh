#!/bin/sh

git config --global alias.lo 'log --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.los 'log --numstat --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.loa 'log --all --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.loas 'log --numstat --all --date=short --graph --pretty="%h %cd %s (%an%Cgreen%d%Creset)"'
git config --global alias.co 'checkout'
git config --global alias.st 'status -s'
git config --global alias.cm 'commit'
git config --global alias.br 'branch'
git config --global alias.addchange '!sh -c '\''git config --add remote."$1".fetch refs/changes/"${2: -2}"/"$2"/*:refs/tags/"$1"/"$2"/*'\'' -'
git config --global alias.getchange '!sh -c '\''git fetch "$1" refs/changes/"${2: -2}"/"$2"/*:refs/tags/"$1"/"$2"/*'\'' -'
git config --global alias.rl 'reflog --date=local'
