#!/bin/bash
#
# Clone and build projects
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	if [ -d "$2/.git/refs" ] ;then
		if [ `git --git-dir "$2/.git" rev-parse --symbolic-full-name --abbrev-ref HEAD` == "master" ] ;then
			git --git-dir "$2/.git" --work-tree "$2" pull
		else
			git --git-dir "$2/.git" fetch --all
		fi
	else
		git clone "$1" "$2"
	fi
	if [ ! -z "$3" ] ;then
		git config -f "$2/.git/config" remote.origin.push HEAD:refs/for/$3
		if [ ! -f "$2/.git/hooks/commit-msg" ] ;then
			curl -o "$2/.git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg
			chmod +x "$2/.git/hooks/commit-msg"
		fi
	fi
}

sudo -E apt-get -q=2 update
sudo -E apt-get -q=2 install gdb autoconf libssl-dev
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
	site=~/git/gerrit/test-site/bin
	[ -d $site ] || mkdir $site
	java -jar ~/git/gerrit/gerrit-war/target/gerrit*.war init --batch -d $site
	$site/bin/gerrit.sh stop
	sed -r -i 's/type.*=.*OPENID/type = DEVELOPMENT_BECOME_ANY_ACCOUNT/' $site/etc/gerrit.config
fi

if [ -d ~/bin -a ! -f ~/bin/jgit ] ;then
	cat <<EOF >~/bin/jgit
#!/bin/sh
java -jar ~/git/jgit/org.eclipse.jgit.pgm/target/jgit-cli.jar $*
EOF
	chmod +x ~/bin/jgit
fi

