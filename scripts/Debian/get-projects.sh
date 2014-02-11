#!/bin/bash
#
# Clone my favourite git repos

# get git
sudo -E apt-get -q=2 install git gitk 

# clone/fetch linux, git, e/jgit, gerrit, buck
( cd ~/git && git clone --recursive https://github.com/torvalds/linux.git ) &
( cd ~/git && git clone --recursive https://gerrit.googlesource.com/gerrit ) &
( cd ~/git && git clone --recursive https://github.com/git/git.git ) &
( cd ~/git && git clone --recursive https://git.eclipse.org/r/jgit/jgit ) &
( cd ~/git && git clone --recursive https://git.eclipse.org/r/egit/egit ) &
( cd ~/git && git clone --recursive https://gerrit.googlesource.com/buck ) &

wait

# configure gerrit repos: get commit-msg hook, configure push 
for i in { gerrit jgit egit } ;do 
  (
    cd $i && \
    git config remote.origin.push HEAD:refs/for/master && \
    wget -O ".git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg && \
    chmod +x ".git/hooks/commit-msg" && 
  )
done
