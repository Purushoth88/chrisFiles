#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# install guest additions if available
[ -x /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run ] && sudo /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run

# add user to group which is allowed to read shared folders
id -G -n | grep vbox || sudo adduser $USER vboxsf
