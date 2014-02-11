#!/bin/bash
#
# Do Lubuntu specific configuration of the git dev-environment 

# fix errors regarding keyring e.g. during git https communication
if [ -f /etc/xdg/autostart/gnome-keyring-pkcs11.desktop ] ;then
	if ! grep '^OnlyShowIn=.*LXDE' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop ;then
		sudo sed -r -i 's/^OnlyShowIn=/OnlyShowIn=LXDE;/' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop
	fi
fi

read -p "Start Preferences/Customize Look and Feel/Color and switch \"Tooltip\" to yellow foreground/black background"
