#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# System updates
sudo apt-get update
sudo apt-get --yes dist-upgrade
sudo /media/VBOXADDITIONS*/VBoxLinuxAdditions.run

# install applications
sudo apt-get install --yes git gitk vim vim-gui-common maven openjdk-6-jdk openjdk-7-jdk eclipse-platform gdb libssl-dev

# install java5 (can only be found on old repos)
if [ ! dpkg -s sun-java5-jdk ] ;then
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo apt-get update
	sudo apt-get install sun-java5-jdk
	sudo update-alternatives --config java
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo apt-get update
fi

# clone linux & git & gerrit, build git & gerrit
[ -d git ] || mkdir git
(cd git && git clone http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git) &
(cd git && git clone https://gerrit.googlesource.com/gerrit && cd gerrit && mvn package) &
sudo apt-get install --yes autoconf
sudo apt-get build-dep --yes git
(cd git && git clone https://github.com/git/git.git && cd git && make configure && ./configure && make) &

# clone&build egit/jgit
(cd git && git clone https://git.eclipse.org/r/p/jgit/jgit && cd jgit && git config remote.origin.push HEAD:refs/for/master && mvn clean install -DskipTests && mvn -f org.eclipse.jgit.packaging/pom.xml clean install)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit && cd egit && git config remote.origin.push HEAD:refs/for/master && mvn -P skip-ui-tests clean install -DskipTests)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit-github && cd egit-github && git config remote.origin.push HEAD:refs/for/master && mvn clean install)
(cd git && git clone https://git.eclipse.org/r/p/egit/egit-pde && cd egit-pde && git config remote.origin.push HEAD:refs/for/master && mvn clean install)

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

# install egit/jgit in juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group

# install egit/jgit in indigo (not all components
eclipse -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/indigo \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group

# prepare API Baselines
[ -d ~/egit-releases ] || mkdir ~/egit-releases 
(
	rel=org.eclipse.egit.repository-2.0.0.201206130900-r
	cd ~/egit-releases
	wget http://download.eclipse.org/egit/updates-2.0/$rel.zip
	unzip $rel.zip -d $rel
	rm $rel.zip
)

# add user to group which is allowed to read shared folders
id -G -n | grep vbox || sudo adduser $USER vboxsf

# write scripts which turn off/on sap proxy usage
cat <<EOF >~/sap_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use SAP proxy
#

grep "^http_proxy" /etc/environment || sudo sh -c "echo 'http_proxy=http://proxy:8080' >> /etc/environment"
grep "^https_proxy" /etc/environment || sudo sh -c "echo 'https_proxy=https://proxy:8080' >> /etc/environment"
export http_proxy=http://proxy:8080
export https_proxy=https://proxy:8080
grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop || sudo sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-pac-url=http:\/\/proxy:8083\//' /usr/share/applications/chromium-browser.desktop
[ -d ~/.m2 ] || mkdir ~/.m2
[ -f ~/.m2/settings_sap_proxy.xml ] || cat <<END >~/.m2/settings_sap_proxy.xml
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
[ -f ~/.m2/settings.xml ] && echo "Couldn't write new ~/.m2/settings.xml because it already existed"
[ -f ~/.m2/settings.xml ] || cp ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml
echo "Please logoff/logon to activate the proxy settings"
EOF
chmod +x ~/sap_proxy.sh

cat <<EOF >~/no_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use no proxy
#

sudo sed -i '/^http_proxy/d' /etc/environment
sudo sed -i '/^https_proxy/d' /etc/environment
unset http_proxy
unset https_proxy
grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop && sudo sed -r -i '/^Exec=/s/--proxy-pac-url=[^ \t]+[ \t]*//' /usr/share/applications/chromium-browser.desktop
sudo sed -i '/^https_proxy/d' /etc/environment
sudo sed -r -i 's/<proxy><active>true</<proxy><active>false</' ~/.m2/settings.xml
echo "Please logoff/logon to activate the proxy settings"
EOF
chmod +x ~/no_proxy.sh

# fix wrong colors in default scheme of Lubuntu 12.04
path=/usr/share/themes
theme=Lubuntu-default
sudo sed -i 's/tooltip_bg_color:#000000/tooltip_bg_color:#f5f5b5/g' $path/$theme/gtk-3.0/settings.ini
sudo sed -i 's/tooltip_fg_color:#ffffff/tooltip_fg_color:#1/g' $path/$theme/gtk-3.0/settings.ini

wait
