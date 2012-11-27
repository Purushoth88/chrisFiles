#!/bin/bash
#
# Configure a Lubuntu12.04 system for jpaas development 
#

no_proxy='wdf.sap.corp,nexus,wiki,git.wdf.sap.corp'
# setup mavens settings.xml
if [ ! -f ~/.m2/settings.xml.jpaas ] ;then
        mkdir -p ~/.m2
        wget -O ~/.m2/settings.xml.jpaas http://nexus.wdf.sap.corp:8081/nexus/service/local/templates/settings/LeanDI/content
        if [ ! -f ~/.m2/settings.xml ] ;then
                cp ~/.m2/settings.xml.jpaas ~/.m2/settings.xml
        else
                echo "Couldn't set ~/.m2/settings.xml because there is already an settings.xml. Copied the jpaas settings.xml to ~/.m2/settings.xml.jpaas"
        fi
fi

# clone & build metering
mkdir -p ~/git
https_proxy="" GIT_SSL_NO_VERIFY=true git clone https://git.wdf.sap.corp:8080/NGJP/Services/metering ~/git/metering
(cd ~/git/metering/com.sap.core.metering.parent && git config remote.origin.pushurl https://d032780@git.wdf.sap.corp:8080/NGJP/Services/metering.git && mvn install -DskipTests)

# install sap tools in juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r https://tools.prod.jpaas.sapbydesign.com/juno \
	-i com.sap.ide.ui5.cloud.feature.feature.group,com.sap.core.tools.eclipse.server.feature.feature.group,com.sap.core.tools.eclipse.help.feature.feature.group


# setup a jpaas sdk
if [ ! -d ~/jpaas/sdk1.9 ] ;then
	mkdir -p ~/jpaas
	echo "Please download and extract a juno SDK from https://tools.prod.jpaas.sapbydesign.com/index.html to ~/jpaas"
	echo "Please download and extract a maxdb from https://downloads.sdn.sap.com/maxdb/7_8/maxdb-all-linux-64bit-x86_64-7_8_02_28.tgz" }
	echo "Please download and extract a RemoteJDBC driver and war from https://wiki.wdf.sap.corp/wiki/display/JavaPersistence/Remote+Access+to+Databases+in+the+NetWeaver+Cloud"
	echo "Please download a hana studio installer from \\production.wdf.sap.corp\newdb\NewDB100\rel"
fi

exit
