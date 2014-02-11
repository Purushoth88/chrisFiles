#!/bin/bash
#
# Configure a ubuntu based system for jpaas development

read -p "Enter directory where to find sap installations: " -e -i "/mnt/perm/sap/install" install_src

# setup a jpaas sdk
find "$install_src" -maxdepth 1 -type f -name 'neo-sdk*.zip' -printf '%P\n' | sed -e 's/\.zip//' | while read sdk ;do
	if [ ! -d ~/jpaas/$sdk ] ;then
		mkdir -p ~/jpaas/$sdk
		(cd ~/jpaas/$sdk; unzip -q "$install_src"/$sdk.zip)
	fi
	[ -f ~/bin/neo ] || ln -s ~/jpaas/$sdk/tools/neo.sh ~/bin/neo
done

# setup mavens settings.xml
if [ ! -f ~/.m2/settings.xml.jpaas ] ;then
	[ -d ~/.m2 ] || mkdir ~/.m2
	wget -O ~/.m2/settings.xml.jpaas http://nexus.wdf.sap.corp:8081/nexus/service/local/templates/settings/LeanDI/content
	sed -r -i 's/\r//' ~/.m2/settings.xml.jpaas
fi
if [ -f ~/.m2/settings.xml.jpaas -a -f ~/.m2/settings_sap_proxy.xml ] ;then
	cat ~/.m2/settings.xml.jpaas ~/.m2/settings_sap_proxy.xml >~/.m2/settings_sap_proxy.xml.jpaas
	sed -r -i '/<\/settings>/{ N; s/<\/settings>\n<settings>// }' ~/.m2/settings_sap_proxy.xml.jpaas
fi
cmp ~/.m2/settings.xml ~/.m2/settings.xml.jpaas && rm ~/.m2/settings.xml
cmp ~/.m2/settings.xml ~/.m2/settings_sap_proxy.xml && rm ~/.m2/settings.xml
[ -f ~/.m2/settings.xml ] || cp ~/.m2/settings_sap_proxy.xml.jpaas ~/.m2/settings.xml

export GIT_SSL_NO_VERIFY=true

[ -d ~/git/orion ] || mkdir -p ~/git/orion

# clone metering, orion, accountPage, appdesigner, hcproxy, cloudrepo
(cd ~/git && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/Services/metering ) &
(cd ~/git/orion && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.orion.git ) &
(cd ~/git/orion && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/LeanDI/org.eclipse.orion.client.git ) &
(cd ~/git/orion && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/LeanDI/org.eclipse.orion.server.git ) &
(cd ~/git && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/JPaaS/com.sap.core.account.git ) &
(cd ~/git && git clone -o sap https://git.wdf.sap.corp:8080/sapui5/sapui5.appdesigner.git ) &
(cd ~/git && git clone -o sap https://git.wdf.sap.corp:8080/com.sap.core.hcproxy.git ) &
(cd ~/git && git clone -o sap https://git.wdf.sap.corp:8080/NGJP/LeanDI/Git/com.sap.core.cloudrepo.git ) &
(cd ~/git/gerrit && git remote add sap https://git.wdf.sap.corp:8080/NGP/LDI/gerrit-internal && git fetch sap) &
(cd ~/git/jgit && git remote add sap https://git.wdf.sap.corp:8080/NGP/LDI/jgit && git fetch sap) &
wait

# configure gerrit repos: get commit-msg hook, configure push 
for i in { metering orion/jpaas.orion.git orion/org.eclipse.orion.client.git orion/org.eclipse.orion.server.git com.sap.core.account.git sapui5.appdesigner.git com.sap.core.hcproxy.git com.sap.core.cloudrepo.git } ;do 
  (
    cd ~/git/$i && \
    git config remote.sap.push HEAD:refs/for/master && \
    wget -O ".git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg && \
    chmod +x ".git/hooks/commit-msg" && 
  )
done

# build the cloned repos
(cd ~/git/metering/com.sap.core.metering.parent; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true) &
(cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f org.eclipse.orion.client/pom.xml clean install) && (cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f org.eclipse.orion.server/pom.xml clean install) && (cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f jpaas.orion/pom.xml clean install) &
(cd ~/git/com.sap.core.account; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true) &
(cd ~/git/sapui5.appdesigner; mvn -s -f reactor/pom.xml -Poptimized.build clean install -DskipTests=true) &
(cd ~/git/com.sap.core.hcproxy; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true) &
(cd ~/git/com.sap.core.cloudrepo/com.sap.jgit.extensions; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true) &
wait

# install SAP Tools in kepler
eclipse -application org.eclipse.equinox.p2.director \
  -r "http://download.eclipse.org/releases/kepler,https://tools.neo.ondemand.com/kepler,http://download.eclipse.org/m2e-wtp/releases" \
  -i com.sap.core.tools.eclipse.help.feature.feature.group,com.sap.core.tools.eclipse.server.feature.feature.group,com.sap.jvm.profiling.feature.group,com.sap.ide.support.feature.feature.group,com.sap.ide.ui5.cloud.feature.feature.group 
exit
