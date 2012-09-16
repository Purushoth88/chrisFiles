#!/bin/bash
#
# Configure a Lubuntu12.04 system for jpaas development 
#

# setup mavens settings.xml
if [ ! -f ~/.m2/settings.xml ] ;then
	mkdir -p ~/.m2
	wget -O ~/.m2/settings.xml http://nexus.wdf.sap.corp:8081/nexus/service/local/templates/settings/LeanDI/content
fi

# clone & build metering
mkdir -p ~/git
git clone -q https://git.wdf.sap.corp:8080/NGJP/Services/metering
cd ~/git/metering/com.sap.core.metering.parent && mvn install -DskipTests

# setup a jpaas sdk
if [ ! -d ~/jpaas/sdk1.9 ] ;then
	mkdir -p ~/jpaas
	echo "Please download a juno SDK from https://tools.prod.jpaas.sapbydesign.com/index.html to ~/jpaas"
fi

# install plugins to eclipse-juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno,http://download.eclipse.org/eclipse/updates/4.2  \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.pde.api.tools.ee.j2se15.group,org.eclipse.pde.api.tools.ee.javase16.group,org.eclipse.pde.api.tools.ee.javase17.group
