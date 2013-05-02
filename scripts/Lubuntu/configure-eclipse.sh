#!/bin/bash
#
# Configure a Eclipse(Juno) on a ubuntu system to my needs
#

# Install plugins to eclipse
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# install eclipse juno
if [ ! -x /usr/bin/eclipse-juno ] ;then
	junoUrl='http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.2.2-201302041200/eclipse-SDK-4.2.2-linux-gtk.tar.gz&r=1'
	if [ $(uname -m) == "x86_64" ] ;then
		junoUrl='http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/R-4.2.2-201302041200/eclipse-SDK-4.2.2-linux-gtk-x86_64.tar.gz&r=1'
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
