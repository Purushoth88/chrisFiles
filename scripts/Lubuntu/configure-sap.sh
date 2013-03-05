#!/bin/bash
#
# Configure a Lubuntu system to work inside the SAP intranet.

# create a script which sets the proxy-related stuff
if [ ! -f ~/bin/setProxy.sh ] ;then
	mkdir -p ~/bin
	cat <<EOF >~/bin/setProxy.sh
#!/bin/bash
#
# Configure a Lubuntu12.04 to use SAP proxy
#

if [ ! -f ~/.m2/settings_sap_proxy.xml ] ;then
	mkdir -p ~/.m2
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

if [ ! -f ~/.80proxy ] ;then
	cat <<END >~/.80proxy
Acquire::http::proxy "http://proxy:8080/";
Acquire::https::proxy "https://proxy:8080/";
END
fi

if [ ! -f ~/.chromiom-browser-proxy.desktop ] ;then
	if [ -f /usr/share/applications/chromium-browser.desktop ] ;then
		cp /usr/share/applications/chromium-browser.desktop ~/.chromiom-browser-proxy.desktop
		cp /usr/share/applications/chromium-browser.desktop ~/.chromiom-browser-noproxy.desktop
		if ! grep -e "--proxy-" ~/.chromiom-browser-proxy.desktop ;then
			sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-server=proxy.wdf.sap.corp:8080 --proxy-bypass-list="*.wdf.sap.corp;nexus;jtrack;localhost;127.0.0.1"/' ~/.chromiom-browser-proxy.desktop
		else
			sed -r -i '/^Exec=/s/--proxy[^ \t]+[ \t]*//' ~/.chromiom-browser-noproxy.desktop
		fi
	fi
fi

# while in the intranet set the correct proxy
if ping -c 1 proxy.wdf.sap.corp >/dev/null 2>&1 ;then
	grep "^http_proxy" /etc/environment || sudo sh -c 'echo http_proxy=http://proxy:8080 >> /etc/environment'
	grep "^https_proxy" /etc/environment || sudo sh -c 'echo https_proxy=https://proxy:8080 >> /etc/environment'
	grep "^no_proxy" /etc/environment || sudo sh -c 'echo "no_proxy=wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp" >> /etc/environment'
	export http_proxy=http://proxy:8080
	export https_proxy=https://proxy:8080
	export no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp'
	[ -f /etc/apt/apt.conf.d/80proxy ] || sudo cp ~/.80proxy /etc/apt/apt.conf.d/80proxy
	[ -f ~/.m2/settings.xml ] || cp ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml
	cmp -s /usr/share/applications/chromium-browser.desktop ~/.chromiom-browser-noproxy.desktop && sudo cp ~/.chromiom-browser-proxy.desktop /usr/share/applications/chromium-browser.desktop
else
	sudo sed -i '/^http_proxy/d' /etc/environment
	sudo sed -i '/^https_proxy/d' /etc/environment
	sudo sed -i '/^no_proxy/d' /etc/environment
	unset http_proxy
	unset https_proxy
	unset no_proxy
	cmp -s ~/.80proxy /etc/apt/apt.conf.d/80proxy && sudo rm /etc/apt/apt.conf.d/80proxy
	cmp -s ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml && rm ~/.m2/settings.xml
	cmp -s /usr/share/applications/chromium-browser.desktop ~/.chromiom-browser-proxy.desktop && sudo cp ~/.chromium-browser-noproxy.desktop /usr/share/applications/chromium-browser.desktop
fi
EOF
	chmod +x ~/bin/setProxy.sh
fi

# set proxy related stuff
[ -x ~/bin/setProxy.sh ] && . ~/bin/setProxy.sh

sudo -E apt-get -q=2 install libnss3-tools
[ -f /media/sf_Shared/LubuntuConfig/SAPNetCA.crt ] && sudo cp /media/sf_Shared/LubuntuConfig/SAPNetCA.crt /usr/local/share/ca-certificates
[ -r /usr/local/share/ca-certificates/SAPNetCA.crt ] && read -P "please put of copy of the SAPNetCA.crt file to /usr/local/share/ca-certificates and press return" 
sudo chmod 444 /usr/local/share/ca-certificates/SAPNetCA.crt
sudo dpkg-reconfigure ca-certificates
sudo update-ca-certificates
[ -e $HOME/.pki/nssdb ] || ( mkdir -p $HOME/.pki/nssdb ; cd $HOME/.pki/nssdb ; certutil -N -d sql:. )
certutil -d sql:$HOME/.pki/nssdb -A -t TC -n "SAPNetCA" -i /usr/local/share/ca-certificates/SAPNetCA.crt

# add wdf.sap.corp as a default domain
if ! grep "^search" /etc/resolvconf/resolv.conf.d/base ;then
	sudo sh -c 'echo "search wdf.sap.corp" >> /etc/resolvconf/resolv.conf.d/base'
fi

