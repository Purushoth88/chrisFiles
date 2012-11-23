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

# clone linux
git clone -q http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &

# configure all gerrit repos to push to the review queue and add commit msg hooks
curl -o /tmp/commit-msg https://git.eclipse.org/r/tools/hooks/commit-msg
chmod +x /tmp/commit-msg
for i in jgit egit egit-pde egit-github ;do cp ~/tmp/commit-msg ~/git/$i/.git/hooks/commit-msg ; git config -f ~/git/$i/.git/config remote.origin.push HEAD:refs/for/master ;done

# build the projects
(cd ~/git/gerrit && mvn package -DskipTests) &
(cd ~/git/git && make configure && ./configure && make) &
(cd ~/git/jgit && mvn install -DskipTests)
(cd ~/git/jgit/org.eclipse.jgit.packaging && mvn install)
(cd ~/git/egit && mvn -P skip-ui-tests install -DskipTests)
(cd ~/git/egit-github && mvn install -DskipTests) &
(cd ~/git/egit-pde && mvn install -DskipTests) &

wait

