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

# get git
sudo -E apt-get -q=2 install git gitk 

# clone/fetch linux
cloneOrFetch https://github.com/torvalds/linux.git ~/git/linux

# clone & build git e/jgit & gerrit
cloneOrFetch https://gerrit.googlesource.com/gerrit ~/git/gerrit master
cloneOrFetch https://github.com/git/git.git ~/git/git && cd ~/git/git
cloneOrFetch https://git.eclipse.org/r/jgit/jgit ~/git/jgit master
cloneOrFetch https://git.eclipse.org/r/egit/egit ~/git/egit master
cloneOrFetch https://git.eclipse.org/r/egit/egit-github ~/git/egit-github master
cloneOrFetch https://git.eclipse.org/r/egit/egit-pde ~/git/egit-pde master

# write bookmarks file
[ -d ~/lib ] || mkdir ~/lib
if [ ! -f ~/lib/git_bookmarks.html ] ;then
	cat <<'EOF' >~/lib/git_bookmarks.html
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
