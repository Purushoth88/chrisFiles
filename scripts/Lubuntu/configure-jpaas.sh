#!/bin/bash
#
# Configure a Lubuntu12.04 system for jpaas development 
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	if [ -d "$2/.git/refs" ] ;then
		git --git-dir "$2/.git" fetch -q --all
		if [ `git --git-dir "$2/.git" rev-parse --symbolic-full-name --abbrev-ref HEAD` == "master" ] ;then
			git --git-dir "$2/.git" --work-tree "$2" pull -q
		fi
	else
		git clone -q "$1" "$2"
	fi
	if [ ! -z "$3" ] ;then
		git config -f "$2/.git/config" remote.origin.push HEAD:refs/for/$3 
		if [ ! -f "$2/.git/hooks/commit-msg" ] ;then
			curl -o "$2/.git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg
			chmod +x "$2/.git/hooks/commit-msg"
		fi
	fi
}

# Install plugins to eclipse 
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

# while in the intranet set the correct proxy
[ -x ~/bin/setProxy.sh ] && . ~/bin/setProxy.sh

# setup a jpaas sdk
if [ ! -d ~/jpaas/sdk1.9 ] ;then
	mkdir -p ~/jpaas
	read -p "Please download and extract a juno SDK from https://tools.prod.jpaas.sapbydesign.com/index.html to ~/jpaas . Hit <Return when done"
	read -p "Please download and extract a maxdb from https://downloads.sdn.sap.com/maxdb/7_8/maxdb-all-linux-64bit-x86_64-7_8_02_28.tgz . Hit <Return when done" 
	read -p "Please download and extract a RemoteJDBC driver and war from https://wiki.wdf.sap.corp/wiki/display/JavaPersistence/Remote+Access+to+Databases+in+the+NetWeaver+Cloud . Hit <Return when done"
	read -p "Please download a hana studio installer from \\\\production.wdf.sap.corp\\newdb\NewDB100\\rel . Hit <Return when done"
fi

GIT_SSL_NO_VERIFY=true
https_proxy=""

# clone metering
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/Services/metering ~/git/metering master

# clone orion
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.org.eclipse.orion.server ~/git/jpaas.org.eclipse.orion.server master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.org.eclipse.orion.client ~/git/jpaas.org.eclipse.orion.client master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.orion ~/git/jpaas.orion master

# setup mavens settings.xml
if [ ! -f ~/.m2/settings.xml.jpaas ] ;then
        mkdir -p ~/.m2
        wget -O ~/.m2/settings.xml.jpaas http://nexus.wdf.sap.corp:8081/nexus/service/local/templates/settings/LeanDI/content
        if [ ! -f ~/.m2/settings.xml ] ;then
                cp ~/.m2/settings.xml.jpaas ~/.m2/settings.xml
		(cd ~/git/metering/com.sap.core.metering.parent && mvn install -DskipTests)
        else
                echo "Couldn't set ~/.m2/settings.xml because there is already an settings.xml. Copied the jpaas settings.xml to ~/.m2/settings.xml.jpaas"
        fi
fi

# install sap tools in juno
installInEclipse eclipse-juno \
	https://tools.prod.jpaas.sapbydesign.com/juno \
	com.sap.ide.ui5.cloud.feature.feature.group,com.sap.core.tools.eclipse.server.feature.feature.group,com.sap.core.tools.eclipse.help.feature.feature.group

[ -d ~/lib ] || mkdir ~/lib
if [ -f ~/lib/git_bookmarks.html ] ;then
	cat <<EOF >~/lib/git_bookmarks.html
<!DOCTYPE NETSCAPE-Bookmark-file-1>
<!-- This is an automatically generated file.
     It will be read and overwritten.
     DO NOT EDIT! -->
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>Git Bookmarks</TITLE>
<DL><p>
    <DL><p>
        <DT><A HREF="http://git.eclipse.org/r/#mine" >Gerrit (Eclipse)</A>
        <DT><A HREF="https://bugs.eclipse.org/bugs/" >Bugzilla (E/JGit)</A>
        <DT><A HREF="http://wiki.eclipse.org/EGit/Contributor_Guide" >EGit/Contributor</A>
        <DT><A HREF="http://www.eclipse.org/forums/index.php?t=thread&frm_id=48" >E/JGit Forum</A>
        <DT><A HREF="https://github.com/" >GitHub</A>
        <DT><A HREF="http://git-scm.com/docs" >Git - Reference</A>
        <DT><A HREF="http://dev.eclipse.org/mhonarc/lists/jgit-dev/" >jgit ML</A>
        <DT><A HREF="https://projectportal.wdf.sap.corp/" >Project Portal @ SAP</A>
        <DT><A HREF="https://monitoring.prod.jpaas.sapbydesign.com/cockpit/" >Cockpit (Prod)</A>
        <DT><A HREF="https://git.wdf.sap.corp:8080/#q,status:open+project:NGJP/Services/metering,n,z" >Gerrit Metering</A>
        <DT><A HREF="chrome://ietab2/content/reloaded.html?url=https://portal.wdf.sap.corp/" >SAP Corporate Portal</A>
        <DT><A HREF="https://git.wdf.sap.corp:50000/git/?p=NGJP/Services/metering.git;a=tree" >gitweb (SAP)</A>
    </DL><p>
</DL><p>
EOF
fi
exit
