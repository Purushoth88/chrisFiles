#!/bin/bash
#
# Configure a Eclipse(Kepler) on a ubuntu system to my needs
#

# Install plugins to eclipse
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# install java
sudo -E apt-get -q=2 install openjdk-7-{jdk,doc,source} 

# install eclipse keploer
if [ ! -x /usr/bin/eclipse-kepler ] ;then
	keplerUrl='http://mirror.netcologne.de/eclipse//technology/epp/downloads/release/kepler/SR1/eclipse-jee-kepler-SR1-linux-gtk.tar.gz'
	if [ $(uname -m) == "x86_64" ] ;then
		keplerUrl='http://mirror.netcologne.de/eclipse//technology/epp/downloads/release/kepler/SR1/eclipse-jee-kepler-SR1-linux-gtk-x86_64.tar.gz'
	fi
	tmp=$(mktemp -d)
	wget -qO- "$keplerUrl" | tar -C $tmp -xz
	mv $tmp/eclipse ~/eclipse-kepler
	rm -fr $tmp
	sudo ln -s ~/eclipse-kepler/eclipse ~/bin/eclipse-kepler
	if [ ! -f ~/.local/share/applications/eclipse-kepler.desktop ] ;then
		mkdir -p ~/.local/share/applications
		cat <<'EOF' >~/.local/share/applications/eclipse-kepler.desktop
[Desktop Entry]
Type=Application
Name=Eclipse (Kepler)
Comment=Eclipse Integrated Development Environment
Icon=~/eclipse-kepler/eclipse
Exec=~/bin/eclipse-kepler
Terminal=false
Categories=Development;IDE;Java;
EOF
	fi
fi

# prepare API Baselines
mkdir -p ~/egit-releases
rel=org.eclipse.egit.repository-2.0.0.201206130900-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-2.0/$rel.zip && unzip -q $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi
