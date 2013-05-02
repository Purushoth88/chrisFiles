#!/bin/bash
#
# Configure a ubuntu system to contain my favourite packages

# update the package index database
sudo -E apt-get -q=2 update

# update installed packages, remove orphans
sudo -E apt-get -q=2 dist-upgrade

# install applications
sudo -E apt-get -q=2 install git gitk vim vim-gui-common maven openjdk-7-{jdk,doc,source} visualvm curl firefox

# fix errors regarding keyring e.g. during git https communication
if [ -f /etc/xdg/autostart/gnome-keyring-pkcs11.desktop ] ;then
	if ! grep '^OnlyShowIn=.*LXDE' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop ;then
		sudo sed -r -i 's/^OnlyShowIn=/OnlyShowIn=LXDE;/' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop
	fi
fi

# fix errors in jvisualvm
[ -f /usr/bin/jvisualvm ] && sudo sed -r -i 's&/usr/share/netbeans/platform12/&/usr/share/netbeans/platform13/&' /usr/bin/jvisualvm

# clean apt-get cache
sudo -E apt-get -q=2 clean
