#!/bin/bash
#
# Configure a Lubuntu system to work inside the SAP intranet.

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

