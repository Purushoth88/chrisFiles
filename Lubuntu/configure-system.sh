#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# install java5 (can only be found on old repos)
if ! dpkg -s sun-java5-jdk ;then
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo apt-get update
	sudo apt-get --yes install sun-java5-jdk
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
fi

# System updates
sudo apt-get update
sudo apt-get --yes dist-upgrade
sudo /media/VBOXADDITIONS*/VBoxLinuxAdditions.run

# install applications
sudo apt-get --yes install git gitk vim vim-gui-common maven openjdk-6-jdk openjdk-7-jdk eclipse-platform gdb libssl-dev autoconf
sudo apt-get --yes build-dep git

# clone linux&git, build git, all in background
mkdir ~/git
git clone http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &
git clone https://github.com/git/git.git ~/git/git && (cd ~/git/git && make configure && ./configure && make) &

# clone and build e/jgit & gerrit
(
	git clone https://git.eclipse.org/r/p/jgit/jgit ~/git/jgit && mvn -f ~/git/jgit/pom.xml install -DskipTests && mvn -f ~/git/jgit/org.eclipse.jgit.packaging/pom.xml install &
	git clone https://git.eclipse.org/r/p/egit/egit ~/git/egit &
	git clone https://git.eclipse.org/r/p/egit/egit-github ~/git/egit-github &
	git clone https://git.eclipse.org/r/p/egit/egit-pde ~/git/egit-pde &
	git clone https://gerrit.googlesource.com/gerrit ~/git/gerrit &

	# wait until everything is cloned and jgit is build
	wait

	# configure all gerrit repos to push to the review queue
	for i in jgit egit egit-pde egit-github ;do git config -f ~/git/$i/.git/config remote.origin.push HEAD:refs/for/master ;done 

	# build the remaining projects
	mvn -f ~/git/gerrit/pom.xml package &
	mvn -f ~/git/egit/pom.xml -P skip-ui-tests install -DskipTests
	mvn -f ~/git/egit-github/pom.xml install &
	mvn -f ~/git/egit-pde/pom.xml install
) &

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

# install egit/jgit in juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno,http://download.eclipse.org/eclipse/updates/4.2-I-builds  \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.pde.api.tools.ee.j2se15.group,org.eclipse.pde.api.tools.ee.javase16.group,org.eclipse.pde.api.tools.ee.javase17.group   

# install egit/jgit in indigo (not all components
eclipse -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/indigo \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group

# prepare API Baselines
mkdir ~/egit-releases 
rel=org.eclipse.egit.repository-2.0.0.201206130900-r
if [ ! -d ~/egit-releases/$rel ] ;then
	wget http://download.eclipse.org/egit/updates-2.0/$rel.zip
	unzip $rel.zip -d ~/egit-releases/$rel
	rm $rel.zip
fi

# add user to group which is allowed to read shared folders
if id -G -n | grep vbox ;then 
	sudo adduser $USER vboxsf
fi

# write sap_proxy.sh to switch to sap proxies
if [ ! -f ~/sap_proxy.sh ] ;then
	# write scripts which turn off/on sap proxy usage
	cat <<EOF >~/sap_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use SAP proxy
#

if ! grep "^http_proxy" /etc/environment ;then
	sudo sh -c "echo 'http_proxy=http://proxy:8080' >> /etc/environment"
fi
if ! grep "^https_proxy" /etc/environment ;then
	sudo sh -c "echo 'https_proxy=https://proxy:8080' >> /etc/environment"
fi
export http_proxy=http://proxy:8080
export https_proxy=https://proxy:8080
if ! grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-pac-url=http:\/\/proxy:8083\//' /usr/share/applications/chromium-browser.desktop
fi
mkdir ~/.m2
if [ ! -f ~/.m2/settings_sap_proxy.xml ] ;then
	cat <<END >~/.m2/settings_sap_proxy.xml
<settings>
  <proxies>
   <proxy><active>true</active>
      <protocol>http</protocol>
      <host>proxy</host>
      <port>8080</port>
      <nonProxyHosts>nexus|*.sap.corp</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
END
if [ -f ~/.m2/settings.xml ] ;then
	echo "Couldn't write new ~/.m2/settings.xml because it already existed"
else
	cp ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml
fi
echo "Please logoff/logon to activate the proxy settings"
EOF
	chmod +x ~/sap_proxy.sh
fi

# write no_proxy.sh to switch to use no proxies
if [ ! -f ~/no_proxy.sh ] ;then
	cat <<EOF >~/no_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use no proxy
#

sudo sed -i '/^http_proxy/d' /etc/environment
sudo sed -i '/^https_proxy/d' /etc/environment
unset http_proxy
unset https_proxy
if grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/--proxy-pac-url=[^ \t]+[ \t]*//' /usr/share/applications/chromium-browser.desktop
fi
sudo sed -i '/^https_proxy/d' /etc/environment
sudo sed -r -i 's/<proxy><active>true</<proxy><active>false</' ~/.m2/settings.xml
echo "Please logoff/logon to activate the proxy settings"
EOF
	chmod +x ~/no_proxy.sh
fi

wait
