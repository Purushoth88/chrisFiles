#!/bin/bash
#
# Configure a Eclipse(Juno) on a Lubuntu12.04 system to my needs
#

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

# install egit/jgit, cdt,...  in juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno,http://download.eclipse.org/eclipse/updates/4.2,http://update.eclemma.org/ \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.pde.api.tools.ee.j2se15.group,org.eclipse.pde.api.tools.ee.javase16.group,org.eclipse.pde.api.tools.ee.javase17.group,com.mountainminds.eclemma.feature.feature.group
