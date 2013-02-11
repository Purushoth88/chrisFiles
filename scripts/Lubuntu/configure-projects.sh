#!/bin/bash
#
# Clone and build projects
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	[ -d "$2" ] || mkdir -p "$2"	
	if git rev-parse --resolve-git-dir "$1/.git" >/dev/null 2>&1 ;then
		git fetch --git-dir "$1/.git" -q --all	
		if [ `git rev-parse --symbolic-full-name --abbrev-ref HEAD` == "master" ] ;then
			git pull -q	
		fi
	else
		git clone -q "$1" "$2"
	fi
	if [ ! -z "$3" ] ;then
		git config -f "$2/.git/config" remote.origin.push HEAD:refs/for/$3 
		if [ ! -f "$2/.git/hooks/commit-msg" ] ;then 
			echo "Won't download a commit-msg hook for repo $2 because such a hook already exists"
		else
			curl -o "$2/.git/hooks/commit-msg" https://git.eclipse.org/r/tools/hooks/commit-msg
			chmod +x "$2/.git/hooks/commit-msg"
		fi
	fi
}

# clone git e/jgit & gerrit
cloneOrFetch https://github.com/git/git.git ~/git/git &
cloneOrFetch https://git.eclipse.org/r/p/jgit/jgit ~/git/jgit master &
cloneOrFetch https://git.eclipse.org/r/p/egit/egit ~/git/egit master &
cloneOrFetch https://git.eclipse.org/r/p/egit/egit-github ~/git/egit-github master &
cloneOrFetch https://git.eclipse.org/r/p/egit/egit-pde ~/git/egit-pde master &
cloneOrFetch https://gerrit.googlesource.com/gerrit ~/git/gerrit master &
wait

# clone/fetch linux
cloneOrFetch http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &

# build the projects
(cd ~/git/gerrit && mvn package -DskipTests) &
(cd ~/git/git && mvn install -DskipTests)
(cd ~/git/jgit/org.eclipse.jgit.packaging && mvn install)
(cd ~/git/egit && mvn -P skip-ui-tests install -DskipTests)
(cd ~/git/egit-github && mvn install -DskipTests) &
(cd ~/git/egit-pde && mvn install -DskipTests) &

wait

# Create a gerrit test site
if [ -f ~/git/gerrit/gerrit-war/target/gerrit*.war ] ;then
	[ -d ~/gerrit ] || mkdir ~/gerrit
	java -jar ~/git/gerrit/gerrit-war/target/gerrit*.war init --batch -d ~/gerrit/gerrit-testsite
	~/gerrit/gerrit-testsite/bin/gerrit.sh stop
	sed -r -i 's/type.*=.*OPENID/type = DEVELOPMENT_BECOME_ANY_ACCOUNT/' ~/gerrit/gerrit-testsite/etc/gerrit.config
fi

if [ -d ~/bin -a ! -f ~/bin/jgit ] ;then
	cat <<EOF >~/bin/jgit
#!/bin/sh
java -jar ~/git/jgit/org.eclipse.jgit.pgm/target/jgit-cli.jar $*
EOF
	chmod +x ~/bin/jgit
fi

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
fi

wait
