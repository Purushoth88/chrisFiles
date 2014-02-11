#!/bin/bash
#
# Install & configure Eclipse on a ubuntu based system

# install java
sudo -E apt-get -q=2 install openjdk-7-{jdk,doc,source} 

# install eclipse
if [ ! -x /usr/bin/eclipse ] ;then
	eclipseUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/kepler/SR1/eclipse-jee-kepler-SR1-linux-gtk.tar.gz&r=1'
	if [ $(uname -m) == "x86_64" ] ;then
		eclipseUrl='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/kepler/SR1/eclipse-jee-kepler-SR1-linux-gtk-x86_64.tar.gz&r=1'
	fi
	tmp=$(mktemp -d)
	wget -qO- "$eclipseUrl" | tar -C $tmp -xz
	sudo mv $tmp/eclipse /opt/eclipse
	rm -fr $tmp
	sudo chown -R $USER:$USER /opt/eclipse
	sudo ln -s /opt/eclipse/eclipse ~/bin/
	if [ ! -f ~/.local/share/applications/eclipse.desktop ] ;then
		mkdir -p ~/.local/share/applications
		cat <<EOF >~/.local/share/applications/eclipse.desktop
[Desktop Entry]
Version=4.3.0
Name=Eclipse
Comment=Eclipse EE 4.3.1 (Kepler)
Exec=env UBUNTU_MENUPROXY=0 /opt/eclipse/eclipse
Icon=/opt/eclipse/icon.xpm
Terminal=false
Type=Application
Categories=Utility;Development;Application
EOF
	fi
fi

# download an EGit release to be prepared for API Baselines
mkdir -p ~/egit-releases
rel=org.eclipse.egit.repository-3.2.0.201312181205-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget -q http://download.eclipse.org/egit/updates-3.2/$rel.zip && unzip -q $rel.zip -d ~/egit-releases/$rel && rm $rel.zip
fi

# get plugins needed by e/jgit development
eclipse \
	-r "http://download.eclipse.org/tools/orbit/downloads/drops/R20140114142710/repository/,http://archive.eclipse.org/jetty/updates/jetty-bundles-7.x/7.6.0.v20120127,http://download.eclipse.org/eclipse/updates/4.3,http://download.eclipse.org/egit/updates-nightly,http://update.eclemma.org/,http://findbugs.cs.umd.edu/eclipse,http://download.eclipse.org/mylyn/snapshots/weekly,http://download.eclipse.org/reviews/nightly,http://download.eclipse.org/technology/swtbot/snapshots" \
	-i org.apache.ant,org.apache.commons.compress,org.apache.log4j,org.kohsuke.args4j,org.hamcrest,javaewah,org.objenesis,org.mockito,com.jcraft.jsch,org.junit,javax.servlet,org.tukaani.xz,org.eclipse.jetty.bundles.f.feature.group,org.eclipse.egit.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.java7.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.pde.api.tools.ee.feature.feature.group,com.mountainminds.eclemma.feature.feature.group,edu.umd.cs.findbugs.plugin.eclipse.feature.group,org.eclipse.mylyn_feature.feature.group,org.eclipse.mylyn.bugzilla_feature.feature.group,org.eclipse.mylyn.reviews.feature.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.swtbot.forms.feature.group,org.eclipse.swtbot.eclipse.feature.group,org.eclipse.swtbot.feature.group,org.eclipse.swtbot.ide.feature.group,org.eclipse.swtbot.eclipse.test.junit.feature.group

read -p "Please add source and javadoc attachments to the installed JRE's in eclipse"
