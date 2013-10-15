#!/bin/bash
#
# Configure a ubuntu system to contain optional software packages (e.g. scripted,...)
#

# update the package index database
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install python-software-properties python g++ make vim vim-gui-common visualvm curl terminator

# fix errors in jvisualvm
[ -f /usr/bin/jvisualvm ] && sudo sed -r -i 's&/usr/share/netbeans/platform12/&/usr/share/netbeans/platform13/&' /usr/bin/jvisualvm
