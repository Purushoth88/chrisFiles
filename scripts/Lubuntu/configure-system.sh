#!/bin/bash
#
# Configure a ubuntu system to contain my favourite packages

# update the package index database
sudo -E apt-get -q=2 update

# update installed packages, remove orphans
sudo -E apt-get -q=2 dist-upgrade

# install applications
sudo -E apt-get -q=2 install git gitk vim vim-gui-common maven openjdk-7-{jdk,doc,source} visualvm curl

# fix errors in jvisualvm
[ -f /usr/bin/jvisualvm ] && sudo sed -r -i 's&/usr/share/netbeans/platform12/&/usr/share/netbeans/platform13/&' /usr/bin/jvisualvm

# clean apt-get cache
sudo -E apt-get -q=2 clean
