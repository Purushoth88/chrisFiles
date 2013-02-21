#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
# Install Guest addtions

# install guest additions if available
sudo apt-get -q update
sudo apt-get -q --yes install dkms && [ -x /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run ] && sudo /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run
