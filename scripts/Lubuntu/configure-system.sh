#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# while in the intranet set the correct proxy
if ping -c 1 proxy.wdf.sap.corp ;then
	export http_proxy=http://proxy:8080 https_proxy=https://proxy:8080 no_proxy="wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp"
else
	unset http_proxy https_proxy no_proxy
fi

# install java5 (can only be found on old repos)
dpkg -s sun-java5-jdk || {
	sudo -E add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo -E add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo -E add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo -E add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo -E apt-get -q=2 update
	sudo -E apt-get -q=2 install sun-java5-jdk
	sudo -E add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo -E add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo -E add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo -E add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
}

# System updates
sudo -E apt-get -q=2 update

# install applications
sudo -E apt-get -q=2 install git gitk vim vim-gui-common maven openjdk-6-{jdk,doc,source} openjdk-7-{jdk,doc,source} visualvm curl dkms

# install guest additions (after dkms)
[ -x /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run ] && sudo -E /media/user/VBOXADDITIONS*/VBoxLinuxAdditions.run

# do an upgrade of the installation
sudo -E apt-get -q=2 dist-upgrade

# add user to group which is allowed to read shared folders
id -G -n | grep vbox || sudo adduser $USER vboxsf

# mount indep data automatically
if ! grep "LABEL=Indep" /etc/fstab ;then
        sudo sh -c 'echo "LABEL=Indep /media/'$USER'/Indep ext4 rw,nosuid,nodev,uhelper=udisks2 0 0" >> /etc/fstab'
fi

# add ~/bin to PATH
if [ ! -d ~/bin ] ;then
	mkdir ~/bin
	if ! grep "^PATH=" /etc/environment ;then
		echo "FATAL: no PATH in /etc/environment. Stopping!"
		exit 1
	else
		sudo sed -r -i '/^PATH=/s@"$@:'$HOME'/bin"@' /etc/environment
	fi
fi

# fix errors regarding keyring e.g. during git https communication
if ! grep '^OnlyShowIn=.*LXDE' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop ;then
	sudo sed -r -i 's/^OnlyShowIn=/OnlyShowIn=LXDE;/' /etc/xdg/autostart/gnome-keyring-pkcs11.desktop
fi

# fix errors in jvisualvm
sudo sed -r -i 's&/usr/share/netbeans/platform12/&/usr/share/netbeans/platform13/&' /usr/bin/jvisualvm

# set the default colorscheme of vim to desert
[ -f ~/.vimrc ] || echo "colorscheme desert" > ~/.vimrc
