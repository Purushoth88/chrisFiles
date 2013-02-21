#!/bin/bash
#
# Configure a Eclipse(Juno,Kepler) on a Lubuntu12.04 system to my needs
#

# Install plugins to eclipse 
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# install eclipse indigo
sudo apt-get -q=2 install eclipse-platform

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

# install eclipse kepler
if [ ! -d /usr/lib/eclipse-kepler ] ;then
	tmp=$(mktemp -d)
	wget -qO- 'http://www.eclipse.org/downloads/download.php?file=/eclipse/downloads/drops4/S-4.3M5a-201302041400/eclipse-SDK-4.3M5a-linux-gtk-x86_64.tar.gz&mirror_id=17' | tar -C $tmp -xz
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
Icon=eclipse
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

# install egit/jgit, cdt,...  in juno
installInEclipse eclipse-juno \
	http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno,http://download.eclipse.org/eclipse/updates/4.2,http://update.eclemma.org/ \
	org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.pde.api.tools.ee.j2se15.group,org.eclipse.pde.api.tools.ee.javase16.group,org.eclipse.pde.api.tools.ee.javase17.group,com.mountainminds.eclemma.feature.feature.group

# install egit/jgit in indigo
installInEclipse eclipse \
	http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/indigo \
	org.eclipse.egit.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group
