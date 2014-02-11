#!/bin/bash
#
# Clone my favourite git repos

# get git
sudo -E apt-get -q=2 install git gitk 

# clone/fetch linux, git, e/jgit, gerrit, buck
git clone --recursive https://github.com/torvalds/linux.git
git clone --recursive https://gerrit.googlesource.com/gerrit
git clone --recursive https://github.com/git/git.git
git clone --recursive https://git.eclipse.org/r/jgit/jgit
git clone --recursive https://git.eclipse.org/r/egit/egit
git clone --recursive https://gerrit.googlesource.com/buck

# configure gerrit repos: get commit-msg hook, configure push 
for i in { gerrit jgit egit } ;do 
  (
    cd $i && \
    git config remote.origin.push HEAD:refs/for/master && \
    wget -O ".git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg && \
    chmod +x ".git/hooks/commit-msg" && 
  )
done
