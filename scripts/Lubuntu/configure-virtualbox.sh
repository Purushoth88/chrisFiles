#!/bin/bash
#
# Configure a Ubuntu based Linux to work properly as a VirtualBox guest

# update the package index database
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install dkms

# install guest additions (after dkms)
[ -x /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run ] && sudo -E /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run

# add user to group which is allowed to read shared folders
id -G -n | grep vbox || sudo adduser $USER vboxsf

# mount indep data automatically
if ! grep "LABEL=Indep" /etc/fstab ;then
        sudo sh -c 'echo "LABEL=Indep /media/'$USER'/Indep ext4 rw,nosuid,nodev,uhelper=udisks2 0 0" >> /etc/fstab'
fi
