#!/bin/bash
#
# Configure a Ubuntu based Linux to work properly as a VirtualBox guest

# install applications
sudo apt-get update
sudo apt-get install dkms

# install guest additions (after dkms)
[ -x /media/VBOXADDITIONS*/VBoxLinuxAdditions.run ] && sudo -E /media/VBOXADDITIONS*/VBoxLinuxAdditions.run

# add user to group which is allowed to read shared folders
id -G -n | grep vbox || sudo adduser $USER vboxsf
