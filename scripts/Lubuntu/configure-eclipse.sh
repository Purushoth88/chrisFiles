#!/bin/bash
#
# Configure a Eclipse(Juno) on a ubuntu system to my needs
#

# Install plugins to eclipse
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# install java
sudo -E apt-get -q=2 install openjdk-7-{jdk,doc,source} 

# install eclipse juno
if [ ! -x /usr/bin/eclipse-juno ] ;then
	junoUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk.tar.gz&r=1'
	if [ $(uname -m) == "x86_64" ] ;then
		junoUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/juno/SR2/eclipse-jee-juno-SR2-linux-gtk-x86_64.tar.gz&r=1'
	fi
	tmp=$(mktemp -d)
	wget -qO- "$junoUrl" | tar -C $tmp -xz
	mv $tmp/eclipse ~/eclipse-juno
	rm -fr $tmp
	sudo ln -s ~/eclipse-juno/eclipse ~/bin/eclipse-juno
	if [ ! -f ~/.local/share/applications/eclipse-juno.desktop ] ;then
		mkdir -p ~/.local/share/applications
		cat <<'EOF' >~/.local/share/applications/eclipse-juno.desktop
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

# install CDT
installInEclipse eclipse-juno 
	http://download.eclipse.org/releases/juno
	org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.m2e.wtp.feature.feature.group

# prepare API Baselines
mkdir -p ~/egit-releases
rel=org.eclipse.egit.repository-2.0.0.201206130900-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-2.0/$rel.zip && unzip -q $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi

read -p "Please start eclipse and "Install software items from file" using ~/git/egit/tools/egit-developer-tools.p2f"
