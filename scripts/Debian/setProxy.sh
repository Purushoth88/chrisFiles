#!/bin/bash
#
# Configure a ubuntu system to use SAP proxy
#

if [ "$1" = "-h" ] ;then
	echo "usage: setProxy [-on] [-off] [-update]"
	echo "       -on:  turn on proxy usage"
	echo "       -off: turn off proxy usage"
	echo "       -update: force rewriting of the config files"
	echo "       by default it is determined automatically whether a proxy is needed or not"
	exit 0
fi

host=$(hostname)
if [ "$1" = "-update" ] || [ "$2" = "-update" ] ;then
	rm ~/.m2/settings_sap_proxy.xml ~/.chromium-browser-proxy.desktop ~/.chromium-browser-noproxy.desktop
	shift 2
fi

if [ ! -f ~/.m2/settings_sap_proxy.xml ] ;then
	mkdir -p ~/.m2
	cat <<EOF >~/.m2/settings_sap_proxy.xml
<settings>
  <proxies>
    <proxy><active>true</active>
      <protocol>http</protocol>
      <host>proxy.wdf.sap.corp</host>
      <port>8080</port>
      <nonProxyHosts>nexus|*.sap.corp|localhost|$host</nonProxyHosts>
    </proxy>
    <proxy><active>true</active>
      <protocol>https</protocol>
      <host>proxy.wdf.sap.corp</host>
      <port>8080</port>
      <nonProxyHosts>nexus|*.sap.corp|localhost|$host</nonProxyHosts>
    </proxy>
  </proxies>
</settings>
EOF
fi

if [ ! -f ~/.80proxy ] ;then
	cat <<'EOF' >~/.80proxy
Acquire::http::proxy "http://proxy.wdf.sap.corp:8080/";
Acquire::https::proxy "https://proxy.wdf.sap.corp:8080/";
EOF
fi

if [ -f /usr/share/applications/chromium-browser.desktop ] ;then
	[ -d ~/.local/share/applications ] || mkdir -p ~/.local/share/applications
	if [ ! -f ~/.chromium-browser-proxy.desktop ] ;then
		cp /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-proxy.desktop
		cp /usr/share/applications/chromium-browser.desktop ~/.chromium-browser-noproxy.desktop
		if ! grep -e "--proxy-" ~/.chromium-browser-proxy.desktop ;then
			sed -r -i '/^Exec=/s/\/usr\/bin\/chromium-browser/\/usr\/bin\/chromium-browser --proxy-server=proxy.wdf.sap.corp:8080 --proxy-bypass-list="*.wdf.sap.corp;nexus;jtrack;localhost;127.0.0.1;'$host'"/' ~/.chromium-browser-proxy.desktop
		else
			sed -r -i '/^Exec=/s/--proxy[^ \t]+[ \t]*//' ~/.chromium-browser-noproxy.desktop
		fi
	fi
fi

if [ "$1" == "" ] ;then
	if ping -c 1 proxy.wdf.sap.corp >/dev/null 2>&1 ;then
		proxy="-on"	
	else
		proxy="-off"
	fi
else
	proxy=$1
fi

# while in the intranet set the correct proxy
if [ $proxy = "-on" ] ;then
	grep "^http_proxy" /etc/environment || sudo sh -c 'echo http_proxy=http://proxy.wdf.sap.corp:8080 >> /etc/environment'
	grep "^https_proxy" /etc/environment || sudo sh -c 'echo https_proxy=https://proxy.wdf.sap.corp:8080 >> /etc/environment'
	grep "^no_proxy" /etc/environment || sudo sh -c 'echo "no_proxy=wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp,'$host'" >> /etc/environment'
	export http_proxy=http://proxy.wdf.sap.corp:8080
	export https_proxy=https://proxy.wdf.sap.corp:8080
	export no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp,'$host
	[ -f /etc/apt/apt.conf.d/80proxy ] || sudo cp ~/.80proxy /etc/apt/apt.conf.d/80proxy
	[ -f ~/.m2/settings.xml ] || cp ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml
	if [ -f /usr/share/applications/chromium-browser.desktop ] ;then
		if cmp -s ~/.local/share/applications/chromium-browser.desktop ~/.chromium-browser-noproxy.desktop || [ ! -f  ~/.local/share/applications/chromium-browser.desktop ] ;then
			cp ~/.chromium-browser-proxy.desktop ~/.local/share/applications/chromium-browser.desktop
		fi
	fi
	echo "turned proxy usage ON"
else
	sudo sed -i '/^http_proxy/d' /etc/environment
	sudo sed -i '/^https_proxy/d' /etc/environment
	sudo sed -i '/^no_proxy/d' /etc/environment
	unset http_proxy
	unset https_proxy
	unset no_proxy
	cmp -s ~/.80proxy /etc/apt/apt.conf.d/80proxy && sudo rm /etc/apt/apt.conf.d/80proxy
	cmp -s ~/.m2/settings_sap_proxy.xml ~/.m2/settings.xml && rm ~/.m2/settings.xml
	if [ -f /usr/share/applications/chromium-browser.desktop ] ;then
		if cmp -s ~/.local/share/applications/chromium-browser.desktop ~/.chromium-browser-proxy.desktop || [ ! -f  ~/.local/share/applications/chromium-browser.desktop ] ;then
			cp ~/.chromium-browser-noproxy.desktop ~/.local/share/applications/chromium-browser.desktop
		fi
	fi
	echo "turned proxy usage OFF"
fi
