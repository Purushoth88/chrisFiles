#!/bin/bash
#
# Update a Debian based Linux

sudo -E apt-get -q=2 update
sudo -E apt-get -q=2 dist-upgrade
sudo -E apt-get -q=2 clean

