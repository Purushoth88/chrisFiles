#!/bin/bash
#
# Configure a Lubuntu12.04 system for jpaas development
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	if [ -d "$2/.git/refs" ] ;then
		if [ `git --git-dir "$2/.git" rev-parse --symbolic-full-name --abbrev-ref HEAD` == "master" ] ;then
			git --git-dir "$2/.git" --work-tree "$2" pull -q
		else
			git --git-dir "$2/.git" fetch -q --all
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
if ping -c 1 proxy.wdf.sap.corp >/dev/null 2>&1 ;then
	export http_proxy=http://proxy:8080 https_proxy=https://proxy:8080 no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp'
else
	unset http_proxy https_proxy no_proxy
fi

# setup a jpaas sdk
find /media/sf_Shared -maxdepth 1 -type f -name 'neo-sdk*.zip' -printf '%P\n' | sed -e 's/\.zip//' | while read sdk ;do
	if [ ! -d ~/jpaas/$sdk ] ;then
		mkdir -p ~/jpaas/$sdk
		(cd ~/jpaas/$sdk; unzip /media/sf_Shared/$sdk.zip)
	fi
done

if [ ! -d /opt/sdb/MaxDB ] ;then
	tmp=$(mktemp -d)
	(
		cd $tmp
		tar -xzf /media/sf_Shared/maxdb-all-linux-64bit-x86_64-7_8_02_28.tgz
		sudo maxdb-all-linux-64bit-x86_64-7_8_02_28/SDBINST
		rm -fr $tmp
	)
fi

if [ ! -d ~/jpaas/remoteAccess ] ;then
	mkdir ~/jpaas/remoteAccess
	cp  /media/sf_Shared/com.sap.core.jdbc.remoteaccess.* ~/jpaas/remoteAccess
fi

if [ -d /media/sf_Shared/SAP_HANA_STUDIO -a ! -d /user/sap/hdbstudio ] ;then
        sudo /media/sf_Shared/SAP_HANA_STUDIO/hdbinst
fi

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

GIT_SSL_NO_VERIFY=true

# clone metering
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/Services/metering ~/git/metering master

# clone orion
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.org.eclipse.orion.server ~/git/org.eclipse.orion.server master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.org.eclipse.orion.client ~/git/org.eclipse.orion.client master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.orion ~/git/jpaas.orion master

# clone account page
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/JPaaS/com.sap.core.account.git ~/git/com.sap.core.account master

# clone appdesigner
cloneOrFetch https://git.wdf.sap.corp:8080/sapui5/sapui5.appdesigner.git ~/git/sapui5.appdesigner master

# build the cloned repos
(cd ~/git/metering/com.sap.core.metering.parent; mvn -q install -DskipTests=true)
(cd ~/git/jpaas.orion; mvn -q install -DskipTests=true)
(cd ~/git/com.sap.core.account; mvn -q install -DskipTests=true)
(cd ~/git/sapui5.appdesigner; mvn -q install -DskipTests=true)

if [ ! -f ~/lib/git_jpaas_bookmarks.html ] ;then
	[ -d ~/lib ] || mkdir ~/lib
	cat <<EOF >~/lib/git_jpaas_bookmarks.html
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
    </DL><p>
</DL><p>
EOF
fi
read -p "Please import bookmarks from ~/lib/git_jpaas_bookmarks.html into chrome. Hit <Return when done"

exit
