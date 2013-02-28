#!/bin/bash
#
# Configure a Lubuntu system to work inside the SAP intranet.

# while in the intranet set the correct proxy
if ping -c 1 proxy.wdf.sap.corp ;then
	export http_proxy=http://proxy:8080 https_proxy=https://proxy:8080 no_proxy="wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp"
else
	unset http_proxy https_proxy no_proxy
fi

sudo -E apt-get -q=2 install libnss3-tools
[ -f /media/sf_Shared/LubuntuConfig/SAPNetCA.crt ] && sudo cp /media/sf_Shared/LubuntuConfig/SAPNetCA.crt /usr/local/share/ca-certificates
[ -f /media/sf_Shared/LubuntuConfig/SSO_CA.crt ] && sudo cp /media/sf_Shared/LubuntuConfig/SSO_CA.crt /usr/local/share/ca-certificates
[ -r /usr/local/share/ca-certificates/SAPNetCA.crt ] && read -P "please put of copy of the SAPNetCA.crt file to /usr/local/share/ca-certificates and press return" 
[ -r /usr/local/share/ca-certificates/SSO_CA.crt ] && read -P "please put of copy of the SSO_CA.crt file to /usr/local/share/ca-certificates and press return" 
sudo dpkg-reconfigure ca-certificates
sudo update-ca-certificates
certutil -d sql:$HOME/.pki/nssdb -A -t TC -n "SSO_CA" -i /usr/local/share/ca-certificates/SSO_CA.crt
certutil -d sql:$HOME/.pki/nssdb -A -t TC -n "SAPNetCA" -i /usr/local/share/ca-certificates/SAPNetCA.crt

# add wdf.sap.corp as a default domain
if ! grep "^search" /etc/resolvconf/resolv.conf.d/base ;then
	sudo sh -c 'echo "search wdf.sap.corp" >> /etc/resolvconf/resolv.conf.d/base'
fi

# write sap_proxy.sh to switch to sap proxies
cat <<EOF >~/bin/sap_proxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use SAP proxy
#

if ! grep "^http_proxy" /etc/environment ;then
	sudo sh -c 'echo http_proxy=http://proxy:8080 >> /etc/environment'
fi
if ! grep "^https_proxy" /etc/environment ;then
	sudo sh -c 'echo https_proxy=https://proxy:8080 >> /etc/environment'
fi
if ! grep "^no_proxy" /etc/environment ;then
	sudo sh -c 'echo "no_proxy=wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp" >> /etc/environment'
fi
export http_proxy=http://proxy:8080
export https_proxy=https://proxy:8080
export no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp'
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
    <proxy><active>true</active>
      <protocol>https</protocol>
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
if [ -f ~/.m2/settings.xml ] ;then
	sudo sed -r -i 's/<proxy><active>false</<proxy><active>true</' ~/.m2/settings.xml
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
chmod +x ~/bin/sap_proxy.sh

# write no_proxy.sh to switch to use no proxies
cat <<EOF >~/bin/no_proxy.sh
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
if grep -e "--proxy-" /usr/share/applications/chromium-browser.desktop ;then
	sudo sed -r -i '/^Exec=/s/--proxy[^ \t]+[ \t]*//' /usr/share/applications/chromium-browser.desktop
fi
if [ -f ~/.m2/settings.xml ] ;then
	sudo sed -r -i 's/<proxy><active>true</<proxy><active>false</' ~/.m2/settings.xml
fi
sudo rm -f /etc/apt/apt.conf.d/80proxy
echo "Please logout/logon to activate the proxy settings"
EOF
chmod +x ~/bin/no_proxy.sh

