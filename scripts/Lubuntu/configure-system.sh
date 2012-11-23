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

# install java5 (can only be found on old repos)
dpkg -s sun-java5-jdk || {
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo apt-get update
	sudo apt-get install sun-java5-{jdk,doc,source}
	sudo update-alternatives --config java
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ hardy-updates multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty multiverse"
	sudo add-apt-repository -r "deb http://us.archive.ubuntu.com/ubuntu/ jaunty-updates multiverse"
	sudo apt-get update
}

# add user to group which is allowed to read shared folders
if id -G -n | grep vbox ;then 
	sudo adduser $USER vboxsf
fi

# write sap_proxy.sh to switch to sap proxies
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
	sudo sh -c "echo 'no_proxy=wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost' >> /etc/environment"
fi
if ! grep "^LOCALDOMAIN" /etc/environment ;then
	sudo sh -c "echo 'LOCALDOMAIN="dhcp.wdf.sap.corp wdf.sap.corp"' >> /etc/environment"
fi
export http_proxy=http://proxy:8080
export https_proxy=https://proxy:8080
export no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost'
export LOCALDOMAIN="dhcp.wdf.sap.corp wdf.sap.corp"
if ! grep -e "--proxy-" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-server=proxy.wdf.sap.corp:8080 --proxy-bypass-list="*.wdf.sap.corp;nexus;jtrack;localhost;127.0.0.1"/' /usr/share/applications/chromium-browser.desktop
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

# write no_proxy.sh to switch to use no proxies
cat <<EOF >~/no_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use no proxy
#

sudo sed -i '/^http_proxy/d' /etc/environment
sudo sed -i '/^https_proxy/d' /etc/environment
sudo sed -i '/^no_proxy/d' /etc/environment
sudo sed -i '/^LOCALDOMAIN/d' /etc/environment
unset http_proxy
unset https_proxy
unset no_proxy
unset LOCALDOMAIN
if grep -e "--proxy-" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/--proxy[^ \t]+[ \t]*//' /usr/share/applications/chromium-browser.desktop
fi
if [ -f ~/.m2/settings.xml ] ;then
	sudo sed -r -i 's/<proxy><active>true</<proxy><active>false</' ~/.m2/settings.xml
fi
sudo rm -f /etc/apt/apt.conf.d/80proxy
echo "Please logout/logon to activate the proxy settings"
EOF
chmod +x ~/no_proxy.sh

wait
