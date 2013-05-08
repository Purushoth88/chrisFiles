#!/bin/bash
#
# Clone and build projects
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

sudo -E apt-get -q=2 update
sudo -E apt-get -q=2 install gdb autoconf libssl-dev
sudo -E apt-get -q=2 build-dep git

# clone/fetch linux
cloneOrFetch http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &

# clone & build git e/jgit & gerrit
(cloneOrFetch https://gerrit.googlesource.com/gerrit ~/git/gerrit master && cd ~/git/gerrit && mvn -q package -DskipTests) &
(cloneOrFetch https://github.com/git/git.git ~/git/git && cd ~/git/git && make configure && ./configure && make)
(cloneOrFetch https://git.eclipse.org/r/jgit/jgit ~/git/jgit master && cd ~/git/jgit && mvn -q install && cd ~/git/jgit/org.eclipse.jgit.packaging && mvn -q install) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit ~/git/egit master && cd ~/git/egit && mvn -q -P skip-ui-tests install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-github ~/git/egit-github master && cd ~/git/egit-github && mvn -q install -DskipTests) 
(cloneOrFetch https://git.eclipse.org/r/egit/egit-pde ~/git/egit-pde master && cd ~/git/egit-pde && mvn -q install -DskipTests) 

wait

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

# get the egit developer tools
wget -q -O /tmp/egit-developer-tools.p2f http://git.eclipse.org/c/egit/egit.git/plain/tools/egit-developer-tools.p2f
read -p "Please import /tmp/egit-developer-tools.p2f in eclipse"

[ -d ~/lib ] || mkdir ~/lib
if [ ! -f ~/lib/git_bookmarks.html ] ;then
	cat <<EOF >~/lib/git_bookmarks.html
<!DOCTYPE NETSCAPE-Bookmark-file-1>
<!-- This is an automatically generated file.
     It will be read and overwritten.
     DO NOT EDIT! -->
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>Git Bookmarks</TITLE>
<DL><p>
    <DL><p>
        <DT><A HREF="http://git.eclipse.org/r/#mine">Gerrit (Eclipse)</A>
        <DT><A HREF="https://bugs.eclipse.org/bugs/">Bugzilla (E/JGit)</A>
        <DT><A HREF="http://wiki.eclipse.org/EGit/Contributor_Guide">EGit/Contributor</A>
        <DT><A HREF="http://www.eclipse.org/forums/index.php?t=thread&frm_id=48">E/JGit Forum</A>
        <DT><A HREF="https://github.com/">GitHub</A>
        <DT><A HREF="http://git-scm.com/docs">Git - Reference</A>
        <DT><A HREF="http://dev.eclipse.org/mhonarc/lists/jgit-dev/">jgit ML</A>
    </DL><p>
</DL><p>
EOF
	read -p "Please import bookmarks into your browser from ~/lib/git_bookmarks.html"
fi
