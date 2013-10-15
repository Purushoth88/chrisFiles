#!/bin/bash
#
# Configure a Lubuntu system to work inside the SAP intranet.

sudo -E apt-get -q=2 install libnss3-tools
read -p "Folder containing SAPNetCA.crt: " crtFolder

[ -f $crtFolder/SAPNetCA.crt ] && sudo cp $crtFolder/SAPNetCA.crt /usr/local/share/ca-certificates
if [ -r /usr/local/share/ca-certificates/SAPNetCA.crt ] ;then
	sudo chmod 444 /usr/local/share/ca-certificates/SAPNetCA.crt
	sudo dpkg-reconfigure ca-certificates
	sudo update-ca-certificates
	[ -e $HOME/.pki/nssdb ] || ( mkdir -p $HOME/.pki/nssdb ; cd $HOME/.pki/nssdb ; certutil -N -d sql:. )
	certutil -d sql:$HOME/.pki/nssdb -A -t TC -n "SAPNetCA" -i /usr/local/share/ca-certificates/SAPNetCA.crt
else
	echo "No valid SAPNetCA.crt found. Will not update the certificates databases"
fi

# add wdf.sap.corp as a default domain
if ! grep "^search" /etc/resolvconf/resolv.conf.d/base ;then
	sudo sh -c 'echo "search wdf.sap.corp" >> /etc/resolvconf/resolv.conf.d/base'
fi

