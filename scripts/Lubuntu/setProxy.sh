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

if [ ! -f ~/.chromium-browser-proxy.desktop ] ;then
	if [ -f /usr/share/applications/chromium-browser.desktop ] ;then
		cp /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-proxy.desktop
		cp /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-noproxy.desktop
		if ! grep -e "--proxy-" ~/.chromium-browser-proxy.desktop ;then
			sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-server=proxy.wdf.sap.corp:8080 --proxy-bypass-list="*.wdf.sap.corp;nexus;jtrack;localhost;127.0.0.1"/' ~/.chromium-browser-proxy.desktop
		else
			sed -r -i '/^Exec=/s/--proxy[^ \t]+[ \t]*//' ~/.chromium-browser-noproxy.desktop
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
	cmp -s /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-noproxy.desktop && sudo cp ~/.chromium-browser-proxy.desktop /usr/share/applications/chromium-browser.desktop
else
	sudo sed -i '/^http_proxy/d' /etc/environment
	sudo sed -i '/^https_proxy/d' /etc/environment
	sudo sed -i '/^no_proxy/d' /etc/environment
	unset http_proxy
	unset https_proxy
	unset no_proxy
	cmp -s ~/.80proxy /etc/apt/apt.conf.d/80proxy && sudo rm /etc/apt/apt.conf.d/80proxy
	cmp -s ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml && rm ~/.m2/settings.xml
	cmp -s /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-proxy.desktop && sudo cp ~/.chromium-browser-noproxy.desktop /usr/share/applications/chromium-browser.desktop
fi
