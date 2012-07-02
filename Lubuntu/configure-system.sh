#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# System updates
sudo apt-get update
sudo apt-get --yes dist-upgrade
sudo /media/VBOXADDITIONS*/VBoxLinuxAdditions.run

# install applications
sudo apt-get install --yes git gitk vim vim-gui-common maven openjdk-6-jdk openjdk-7-jdk eclipse-platform

# clone linux & git, build git
[ -d git ] || mkdir git
(cd git && git clone http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git) &
sudo apt-get install --yes autoconf
sudo apt-get build-dep --yes git
(cd git && git clone https://github.com/git/git.git && cd git && make configure && ./configure && make)

# clone&build egit/jgit
(cd git && git clone https://git.eclipse.org/r/p/jgit/jgit && cd jgit && mvn clean install -DskipTests && mvn -f org.eclipse.jgit.packaging/pom.xml clean install)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit && cd egit && mvn -P skip-ui-tests clean install -DskipTests)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit-github && cd egit-github && mvn clean install)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit-pde && cd egit-pde && mvn clean install)

# install eclipse juno
(
	[ ! -d /usr/lib/eclipse-juno ] &&\
	[ ! -f ~/.local/share/applications/eclipse-juno.desktop ] &&\
	sudo mkdir -p /usr/lib/eclipse-juno/download &&\
	wget -qO- 'http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/juno/R/eclipse-jee-juno-linux-gtk-x86_64.tar.gz&r=1' | sudo tar -C /usr/lib/eclipse-juno/download -xz &&\
	sudo mv /usr/lib/eclipse-juno/download/eclipse/* /usr/lib/eclipse-juno &&\
	sudo ln -s /usr/lib/eclipse-juno/eclipse /usr/bin/eclipse-juno &&\
	mkdir -p ~/.local/share/applications &&\
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
)

# install egit/jgit
eclipse-juno -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/tools/orbit/downloads/drops/R20120526062928/repository/ -repository http://download.eclipse.org/egit/updates -repository http://download.eclipse.org/tools/orbit/downloads/drops/R20120526062928/repository/ -installIU org.hamcrest -installIU org.apache.log4j -installIU com.google.protobuf -installIU org.kohsuke.args4j -installIU org.mockito -installIU org.eclipse.egit.feature.group -installIU org.eclipse.egit.import.feature.group -installIU org.eclipse.egit.mylyn.feature.group -installIU org.eclipse.egit.psf.feature.group -installIU org.eclipse.jgit.feature.group -installIU org.eclipse.jgit.pgm.feature.group
eclipse -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/tools/orbit/downloads/drops/R20120526062928/repository/ -repository http://download.eclipse.org/egit/updates -repository http://download.eclipse.org/mylyn/releases/latest -installIU org.hamcrest -installIU org.apache.log4j -installIU com.google.protobuf -installIU org.kohsuke.args4j -installIU org.mockito -installIU org.eclipse.egit.feature.group -installIU org.eclipse.egit.mylyn.feature.group -installIU org.eclipse.egit.psf.feature.group -installIU org.eclipse.jgit.feature.group -installIU org.eclipse.jgit.pgm.feature.group

wait
