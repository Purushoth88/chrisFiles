#!/bin/bash
#
# Configure a Lubuntu12.04 system to my needs
#

# System updates
sudo apt-get -q update
sudo apt-get -q --yes dist-upgrade

# install applications
sudo apt-get -q --yes install git gitk vim vim-gui-common maven openjdk-6-{jdk,doc,source} openjdk-7-{jdk,doc,source} gdb libssl-dev autoconf visualvm firefox
sudo apt-get -q --yes build-dep git

# clone linux&git
mkdir -p ~/git
git clone -q git://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux
git clone -q https://github.com/git/git.git ~/git/git 

# clone e/jgit & gerrit
git clone -q https://git.eclipse.org/r/p/jgit/jgit ~/git/jgit
git clone -q https://git.eclipse.org/r/p/egit/egit ~/git/egit
git clone -q https://git.eclipse.org/r/p/egit/egit-github ~/git/egit-github
git clone -q https://git.eclipse.org/r/p/egit/egit-pde ~/git/egit-pde
git clone -q https://gerrit.googlesource.com/gerrit ~/git/gerrit

# configure all gerrit repos to push to the review queue
for i in jgit egit egit-pde egit-github ;do git config -f ~/git/$i/.git/config remote.origin.push HEAD:refs/for/master ;done 

# build the projects
(cd ~/git/git && make configure && ./configure && make)
(cd ~/git/jgit && mvn install -DskipTests)
(cd ~/git/jgit/org.eclipse.jgit.packaging && mvn install)
(cd ~/git/egit && mvn -P skip-ui-tests install -DskipTests)
(cd ~/git/egit-github && mvn install -DskipTests)
(cd ~/git/egit-pde && mvn install -DskipTests)
(cd ~/git/gerrit && mvn package -DskipTests)

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
if ! grep "^no_proxy" /etc/environment ;then
	sudo sh -c "echo 'no_proxy=.wdf.sap.corp,nexus,jtrack' >> /etc/environment"
fi
export http_proxy=http://proxy:8080
export https_proxy=https://proxy:8080
export no_proxy=.wdf.sap.corp,nexus,jtrack
if ! grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-pac-url=http:\/\/proxy:8083\//' /usr/share/applications/chromium-browser.desktop
fi
mkdir -p ~/.m2
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
fi
if [ ! -f ~/.m2/settings.xml ] ;then
	cp ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml
fi
if [ ! -f /etc/apt/apt.conf.d/80proxy ] ;then
	cat <<END >80proxy
Acquire::http::proxy "http://proxy:8080/";
Acquire::https::proxy "https://proxy:8080/";
END
	sudo mkdir -p /etc/apt/apt.conf.d
	sudo mv 80proxy /etc/apt/apt.conf.d
fi

echo "Please logout/login to activate the proxy settings"
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
sudo sed -i '/^no_proxy/d' /etc/environment
unset http_proxy
unset https_proxy
unset no_proxy
if grep "proxy-pac-url" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/--proxy-pac-url=[^ \t]+[ \t]*//' /usr/share/applications/chromium-browser.desktop
fi
if [ -f ~/.m2/settings.xml ] ;then
	sudo sed -r -i 's/<proxy><active>true</<proxy><active>false</' ~/.m2/settings.xml
fi
sudo rm -f /etc/apt/apt.conf.d/80proxy
echo "Please logout/logon to activate the proxy settings"
EOF
	chmod +x ~/no_proxy.sh
fi
