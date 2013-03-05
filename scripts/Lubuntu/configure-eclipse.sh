#!/bin/bash
#
# Configure a Eclipse(Juno,Kepler) on a Lubuntu12.04 system to my needs
#

# Install plugins to eclipse 
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# while in the intranet set the correct proxy
if ping -c 1 proxy.wdf.sap.corp >/dev/null 2>&1 ;then
	export http_proxy=http://proxy:8080 https_proxy=https://proxy:8080 no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp'
else
	unset http_proxy https_proxy no_proxy
fi

# install eclipse indigo
sudo -E apt-get -q=2 install eclipse-platform

# install eclipse kepler
if [ ! -d /usr/lib/eclipse-kepler ] ;then
	tmp=$(mktemp -d)
	wget -qO- 'http://download.eclipse.org/technology/epp/downloads/release/kepler/M5/eclipse-jee-kepler-M5-linux-gtk-x86_64.tar.gz' | tar -C $tmp -xz
	sudo mv $tmp/eclipse /usr/lib/eclipse-kepler
fi
sudo ln -s /usr/lib/eclipse-kepler/eclipse /usr/bin/eclipse-kepler
if [ ! -f ~/.local/share/applications/eclipse-kepler.desktop ] ;then
	mkdir -p ~/.local/share/applications
	cat <<EOF >~/.local/share/applications/eclipse-kepler.desktop
[Desktop Entry]
Type=Application
Name=Eclipse (Kepler)
Comment=Eclipse Integrated Development Environment
Icon=eclipse-kepler
Exec=eclipse-kepler
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

# install egit/jgit in kepler
installInEclipse eclipse-kepler \
	http://download.eclipse.org/releases/kepler,http://download.eclipse.org/egit/updates \
	org.eclipse.egit.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group

# install egit/jgit in indigo
installInEclipse eclipse \
	http://download.eclipse.org/releases/indigo,http://download.eclipse.org/egit/updates \
	org.eclipse.egit.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group
