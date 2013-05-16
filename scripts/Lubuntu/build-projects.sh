#!/bin/bash
#
# Clone and build projects
#

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
		if [ -f /media/sf_Shared/$(basename "$2").zip ] ;then
			unzip -q /media/sf_Shared/$(basename "$2").zip -d "$(dirname "$2")"
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
sudo -E apt-get -q=2 install gdb autoconf libssl-dev maven openjdk-7-{jdk,doc,source}
sudo -E apt-get -q=2 build-dep git

# clone & build git e/jgit & gerrit
(cloneOrFetch https://gerrit.googlesource.com/gerrit ~/git/gerrit master && cd ~/git/gerrit && mvn package -DskipTests)
(cloneOrFetch https://github.com/git/git.git ~/git/git && cd ~/git/git && make configure && ./configure && make)
(cloneOrFetch https://git.eclipse.org/r/jgit/jgit ~/git/jgit master && cd ~/git/jgit && mvn install && cd ~/git/jgit/org.eclipse.jgit.packaging && mvn install) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit ~/git/egit master && cd ~/git/egit && mvn -P skip-ui-tests install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-github ~/git/egit-github master && cd ~/git/egit-github && mvn install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-pde ~/git/egit-pde master && cd ~/git/egit-pde && mvn install -DskipTests) 

# Create a gerrit test site
if [ -f ~/git/gerrit/gerrit-war/target/gerrit*.war ] ;then
	site=~/git/test-site
	[ -d $site ] || mkdir $site
	java -jar ~/git/gerrit/gerrit-war/target/gerrit*.war init --batch -d $site
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

