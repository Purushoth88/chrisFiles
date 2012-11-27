#!/bin/bash
#
# Configure a Eclipse(Juno) on a Lubuntu12.04 system to my needs
#

# install eclipse indigo
sudo apt-get -q --yes install eclipse-platform

# install eclipse juno
if [ ! -d /usr/lib/eclipse-juno ] ;then
	tmp=$(mktemp -d)
	wget -qO- 'http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/juno/R/eclipse-jee-juno-linux-gtk-x86_64.tar.gz&r=1' | tar -C $tmp -xz
	sudo mv $tmp/eclipse /usr/lib/eclipse-juno
fi
sudo ln -s /usr/lib/eclipse-juno/eclipse /usr/bin/eclipse-juno
if [ ! -f ~/.local/share/applications/eclipse-juno.desktop ] ;then
	mkdir -p ~/.local/share/applications
	cat <<EOF >~/.local/share/applications/eclipse-juno.desktop
[Desktop Entry]
Type=Application
Name=Eclipse (Juno)
Comment=Eclipse Integrated Development Environment
Icon=eclipse
Exec=eclipse-juno
Terminal=false
Categories=Development;IDE;Java;
EOF
fi

# prepare API Baselines
mkdir -p ~/egit-releases 
rel=org.eclipse.egit.repository-2.0.0.201206130900-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-2.0/$rel.zip && unzip $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi

