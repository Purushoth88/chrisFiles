#!/bin/bash
#
# Configure a ubuntu based system to contain my optional software packages

# update the package index database
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install python-software-properties python g++ make vim vim-gui-common visualvm curl terminator openjdk-6-jdk chromium-browser
