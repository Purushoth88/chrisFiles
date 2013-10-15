#!/bin/bash
#
# Configure a ubuntu system to contain optional software packages (e.g. scripted,...)
#

# update the package index database
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install python-software-properties python g++ make vim vim-gui-common visualvm curl terminator openjdk-6-jdk
