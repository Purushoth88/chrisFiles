#!/bin/bash
#
# Configure a Lubuntu12.04 system for jpaas development
#

read -p "Enter root path where to find common files: " -e -i "/mnt/perm" common_files
read -p "Enter directory where to find git bundles: " -e -i "$common_files/git/bundles" bundleDir
read -p "Enter directory where to find sap installations: " -e -i "$common_files/sap/install" install_src

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
        if [ -d "$2" ] ;then
                gitDir="$2/.git"
                workTree="$2"
                [ ! -d "$2/.git" ] && gitDir="$2" && workTree=""
                if [ ! -z "$workTree" ] && [ $(git --git-dir "$gitDir" rev-parse --symbolic-full-name --abbrev-ref HEAD) == "master" ] ;then
                        git --git-dir "$gitDir" --work-tree "$2" pull
                else
                        git --git-dir "$gitDir" fetch --all
                fi
                [ -z "$workTree" ] || ( cd "$workTree" ; git submodule update --init --recursive )
        else
                if [ -f "$bundleDir"/$(basename "$2").bundle ] ;then
                        git clone "$bundleDir"/$(basename "$2").bundle "$2"
                        git --git-dir "$2/.git" config remote.origin.url $1
                        cloneOrFetch "$1" "$2" "$3"
                        return
                else
                        git clone --recursive $1 "$2"
                        gitDir="$2/.git"
                        [ ! -d "$2/.git" ] && gitDir="$2"
                fi
        fi
        if [ ! -z "$3" ] ;then
                git config -f "$gitDir/config" remote.origin.push HEAD:refs/for/$3
                if [ ! -f "$gitDir/hooks/commit-msg" ] ;then
                        wget -O "$gitDir/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg
                        chmod +x "$gitDir/hooks/commit-msg"
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
	export http_proxy=http://proxy.wdf.sap.corp:8080 https_proxy=https://proxy.wdf.sap.corp:8080 no_proxy='wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp'
else
	unset http_proxy https_proxy no_proxy
fi

# install chrome for appdesigner
sudo -E apt-get -q=2 install chromium-browser

# setup a jpaas sdk
find "$install_src" -maxdepth 1 -type f -name 'neo-sdk*.zip' -printf '%P\n' | sed -e 's/\.zip//' | while read sdk ;do
	if [ ! -d ~/jpaas/$sdk ] ;then
		mkdir -p ~/jpaas/$sdk
		(cd ~/jpaas/$sdk; unzip -q "$install_src"/$sdk.zip)
	fi
done

if [ ! -d /opt/sdb/MaxDB ] ;then
	tmp=$(mktemp -d)
	(
		cd $tmp
		tar -xzf "$install_src"/maxdb-all-linux-64bit-x86_64-7_8_02_28.tgz
		sudo maxdb-all-linux-64bit-x86_64-7_8_02_28/SDBINST
		rm -fr $tmp
	)
fi

if [ ! -d ~/jpaas/remoteAccess ] ;then
	mkdir ~/jpaas/remoteAccess
	cp  "$install_src"/com.sap.core.jdbc.remoteaccess.* ~/jpaas/remoteAccess
fi

if [ -d "$install_src"/SAP_HANA_STUDIO -a ! -d /usr/sap/hdbstudio ] ;then
        sudo "$install_src"/SAP_HANA_STUDIO/hdbinst
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

export GIT_SSL_NO_VERIFY=true

# clone metering
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/Services/metering ~/git/metering master

# clone orion
[ -d ~/git/orion ] || mkdir -p ~/git/orion
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/jpaas.orion.git ~/git/orion/jpaas.orion master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/org.eclipse.orion.client.git ~/git/orion/org.eclipse.orion.client master
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/org.eclipse.orion.server.git ~/git/orion/org.eclipse.orion.server master

# clone account page
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/JPaaS/com.sap.core.account.git ~/git/com.sap.core.account master

# clone appdesigner
cloneOrFetch https://git.wdf.sap.corp:8080/sapui5/sapui5.appdesigner.git ~/git/sapui5.appdesigner master

# clone hcproxy
cloneOrFetch https://git.wdf.sap.corp:8080/com.sap.core.hcproxy.git ~/git/com.sap.core.hcproxy master

# clone cloudrepo
cloneOrFetch https://git.wdf.sap.corp:8080/NGJP/LeanDI/Git/com.sap.core.cloudrepo.git ~/git/com.sap.core.cloudrepo master

# add local forks to projects
(cd ~/git/gerrit; git remote add sap https://git.wdf.sap.corp:8080/NGP/LDI/gerrit-internal; git fetch sap)
(cd ~/git/jgit; git remote add sap https://git.wdf.sap.corp:8080/NGP/LDI/jgit; git fetch sap)

# build the cloned repos
(cd ~/git/metering/com.sap.core.metering.parent; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true)
(cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f org.eclipse.orion.client/pom.xml clean install)
(cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f org.eclipse.orion.server/pom.xml clean install)
(cd ~/git/orion; mvn -s ~/.m2/settings.xml.jpaas -f jpaas.orion/pom.xml clean install)
(cd ~/git/com.sap.core.account; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true)
(cd ~/git/sapui5.appdesigner; mvn -f reactor/pom.xml -Poptimized.build clean install -DskipTests=true)
(cd ~/git/com.sap.core.hcproxy; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true)
(cd ~/git/com.sap.core.cloudrepo/com.sap.jgit.extensions; mvn -s ~/.m2/settings.xml.jpaas -q install -DskipTests=true)

if [ ! -f ~/lib/git_jpaas_bookmarks.html ] ;then
	[ -d ~/lib ] || mkdir ~/lib
	cat <<'EOF' >~/lib/git_jpaas_bookmarks.html
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

# install SAP Tools in kepler
installInEclipse eclipse \
	http://download.eclipse.org/releases/kepler,https://tools.neo.ondemand.com/kepler,http://download.eclipse.org/m2e-wtp/releases \
	com.sap.core.tools.eclipse.help.feature.feature.group,com.sap.core.tools.eclipse.server.feature.feature.group,com.sap.jvm.profiling.feature.group,com.sap.ide.support.feature.feature.group,com.sap.ide.ui5.cloud.feature.feature.group 
exit
