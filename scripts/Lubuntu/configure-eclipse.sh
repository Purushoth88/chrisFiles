#!/bin/bash
#
# Configure a Eclipse on a ubuntu system to my needs
#

# Install plugins to eclipse
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# install java
sudo -E apt-get -q=2 install openjdk-7-{jdk,doc,source} 

# install eclipse
if [ ! -x /usr/bin/eclipse ] ;then
	eclipseUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/luna/M2/eclipse-jee-luna-M2-linux-gtk.tar.gz'
	if [ $(uname -m) == "x86_64" ] ;then
		eclipseUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/luna/M2/eclipse-jee-luna-M2-linux-gtk-x86_64.tar.gz'
	fi
	tmp=$(mktemp -d)
	wget -qO- "$eclipseUrl" | tar -C $tmp -xz
	sudo mv $tmp/eclipse /opt/eclipse
	rm -fr $tmp
	sudo chown -R $USER:$USER /opt/eclipse
	sudo ln -s /opt/eclipse/eclipse ~/bin/
	if [ ! -f ~/.local/share/applications/eclipse.desktop ] ;then
		mkdir -p ~/.local/share/applications
		cat <<'EOF' >~/.local/share/applications/eclipse.desktop
[Desktop Entry]
Type=Application
Name=Eclipse
Comment=Eclipse Integrated Development Environment
Icon=/opt/eclipse/eclipse
Exec=/opt/eclipse/eclipse
Terminal=false
Categories=Development;IDE;Java;
EOF
	fi
fi

# prepare API Baselines
mkdir -p ~/egit-releases
rel=org.eclipse.egit.repository-3.1.0.201310021548-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-3.1/$rel.zip && unzip -q $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi
