#!/bin/bash
#
# Clone and build projects
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	bundleDir=/mnt/perm/git/bundles
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
		if [ -f $bundleDir/$(basename "$2").bundle ] ;then
			git clone $bundleDir/$(basename "$2").bundle "$2"
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

# install software to build: java, g++
sudo -E apt-get -q=2 install openjdk-7-jdk
sudo update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
sudo -E apt-get -q=2 install gdb autoconf libssl-dev maven openjdk-7-{doc,source} ant curl
sudo -E apt-get -q=2 build-dep git

# clone & build git e/jgit & gerrit
(cloneOrFetch https://github.com/git/git.git ~/git/git && cd ~/git/git && make configure && ./configure && make)
(cloneOrFetch https://git.eclipse.org/r/jgit/jgit ~/git/jgit master && cd ~/git/jgit && mvn -s ~/.m2/settings_sap_proxy.xml install && cd ~/git/jgit/org.eclipse.jgit.packaging && mvn -s ~/.m2/settings_sap_proxy.xml install) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit ~/git/egit master && cd ~/git/egit && mvn -s ~/.m2/settings_sap_proxy.xml -P skip-ui-tests install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-github ~/git/egit-github master && cd ~/git/egit-github && mvn -s ~/.m2/settings_sap_proxy.xml install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-pde ~/git/egit-pde master && cd ~/git/egit-pde && mvn -s ~/.m2/settings_sap_proxy.xml install -DskipTests) 
(cloneOrFetch https://gerrit.googlesource.com/buck ~/git/buck master && cd ~/git/buck && ant && ln -s $(pwd)/bin/buck ~/bin/ ) 
(cloneOrFetch https://gerrit.googlesource.com/gerrit ~/git/gerrit master && cd ~/git/gerrit && buck build release && tools/eclipse/project.py --src)

# Create a gerrit test site
if [ -f ~/git/gerrit/buck-out/gen/release.war ] ;then
	site=~/git/gerrit/test-site/bin
	[ -d $site ] || mkdir -p $site
	java -jar ~/git/gerrit/buck-out/gen/release.war init --batch -d $site
	$site/bin/gerrit.sh stop
	sed -r -i 's/type.*=.*OPENID/type = DEVELOPMENT_BECOME_ANY_ACCOUNT/' $site/etc/gerrit.config
fi

if [ -d ~/bin -a ! -f ~/bin/jgit ] ;then
	cat <<'EOF' >~/bin/jgit
#!/bin/sh
java -jar ~/git/jgit/org.eclipse.jgit.pgm/target/jgit-cli.jar $*
EOF
	chmod +x ~/bin/jgit
fi

