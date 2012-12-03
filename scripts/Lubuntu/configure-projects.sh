#!/bin/bash
#
# Clone and build projects
#

# clone git e/jgit & gerrit
mkdir -p ~/git
[ -d ~/git/git ] || git clone -q https://github.com/git/git.git ~/git/git &
[ -d ~/git/jgit ] || git clone -q https://git.eclipse.org/r/p/jgit/jgit ~/git/jgit &
[ -d ~/git/egit ] || git clone -q https://git.eclipse.org/r/p/egit/egit ~/git/egit &
[ -d ~/git/egit-github ] || git clone -q https://git.eclipse.org/r/p/egit/egit-github ~/git/egit-github &
[ -d ~/git/egit-pde ] || git clone -q https://git.eclipse.org/r/p/egit/egit-pde ~/git/egit-pde &
[ -d ~/git/gerrit ] || git clone -q https://gerrit.googlesource.com/gerrit ~/git/gerrit &

wait

# clone/fetch linux
if [ -d ~/git/linux ] ;then
	(cd ~/git/linux; git fetch)
else
	git clone http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &
fi


# configure all gerrit repos to push to the review queue and add commit msg hooks
curl -o /tmp/commit-msg https://git.eclipse.org/r/tools/hooks/commit-msg
chmod +x /tmp/commit-msg
for i in jgit egit egit-pde egit-github ;do cp /tmp/commit-msg ~/git/$i/.git/hooks/commit-msg ; git config -f ~/git/$i/.git/config remote.origin.push HEAD:refs/for/master ;done

# build the projects
(cd ~/git/gerrit && git fetch && git pull && mvn package -DskipTests) &
(cd ~/git/git && git fetch && git pull && make configure && ./configure && make) &
(cd ~/git/jgit && git fetch && git pull && mvn install -DskipTests)
(cd ~/git/jgit/org.eclipse.jgit.packaging && git fetch && git pull && mvn install)
(cd ~/git/egit && git fetch && git pull && mvn -P skip-ui-tests install -DskipTests)
(cd ~/git/egit-github && git fetch && git pull && mvn install -DskipTests) &
(cd ~/git/egit-pde && git fetch && git pull && mvn install -DskipTests) &

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
