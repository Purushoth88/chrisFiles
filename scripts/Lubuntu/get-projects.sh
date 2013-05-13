#!/bin/bash
#
# Clone and build projects
#

# Clones a non-bare git repo or (if it already exists) fetches updates
# usage: getOrFetch <url> <localDir> [<gerritBranchToPush>]
cloneOrFetch() {
	if [ -f /media/sf_Shared/$(basename "$2").zip ] && [ ! -d "$2" ] ;then
		unzip /media/sf_Shared/$(basename "$2").zip -d "$(dirname "$2")"
	fi
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
