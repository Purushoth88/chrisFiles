#!/bin/bash
#
# Configure a Eclipse(Indigo, Juno) on a Lubuntu12.04 system to my needs
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

# install eclipse juno
if [ ! -x /usr/bin/eclipse-juno ] ;then
	junoUrl=http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk.tar.gz
	if [ $(uname -m) == "x86_64" ] ;then
		junoUrl=http://mirror.selfnet.de/eclipse/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk-x86_64.tar.gz
	fi
	tmp=$(mktemp -d)
	wget -qO- "$junoUrl" | tar -C $tmp -xz
	mv $tmp/eclipse ~/eclipse-juno
	rm -fr $tmp
	sudo ln -s ~/eclipse-juno/eclipse ~/bin/eclipse-juno
	if [ ! -f ~/.local/share/applications/eclipse-juno.desktop ] ;then
		mkdir -p ~/.local/share/applications
		cat <<EOF >~/.local/share/applications/eclipse-juno.desktop
[Desktop Entry]
Type=Application
Name=Eclipse (Juno)
Comment=Eclipse Integrated Development Environment
Icon=eclipse-juno
Exec=eclipse-juno
Terminal=false
Categories=Development;IDE;Java;
EOF
	fi
fi

# prepare API Baselines
mkdir -p ~/egit-releases
rel=org.eclipse.egit.repository-2.0.0.201206130900-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-2.0/$rel.zip && unzip $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi

# install egit/jgit in juno
installInEclipse eclipse-juno \
	http://download.eclipse.org/releases/juno,http://download.eclipse.org/egit/updates \
	org.eclipse.egit.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group

# install egit/jgit in indigo
installInEclipse eclipse \
	http://download.eclipse.org/releases/indigo,http://download.eclipse.org/egit/updates \
	org.eclipse.egit.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group org.eclipse.cdt.autotools.feature.group org.eclipse.cdt.feature.group 
