#!/bin/bash
#
# Configure a ubuntu system to contain optional software packages (e.g. scripted,...)
#

# add latest nodejs repo (otherwise scripted doesn't install)
sudo add-apt-repository ppa:chris-lea/node.js

# update the package index database
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install python-software-properties python g++ make

# install nodejs and scripted
sudo -E apt-get -q=2 install nodejs
npm install -g scripted
