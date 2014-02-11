#!/bin/bash
#
# Build my favourite projects on a ubuntu based system

# install software to build: java, g++
sudo -E apt-get -q=2 install gdb autoconf libssl-dev maven openjdk-7-{jdk,doc,source} ant curl
sudo update-alternatives --set java /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
sudo -E apt-get -q=2 install sudo -E apt-get -q=2 build-dep git

# clone & build git e/jgit & gerrit
(cd ~/git/git && make configure && ./configure && make -s) &
(cd ~/git/jgit && mvn -q install -DskipTests && cd ~/git/jgit/org.eclipse.jgit.packaging && mvn install -DskipTests) && (cd ~/git/egit && mvn -q -P skip-ui-tests install -DskipTests) &
(cd ~/git/buck && ant -S && ln -s $(pwd)/bin/buck ~/bin/ ) && (cd ~/git/gerrit && buck build release && tools/eclipse/project.py --src) &
wait

# Create a gerrit test site
if [ -f ~/git/gerrit/buck-out/gen/release.war ] ;then
	site=~/gerrit/test-site/bin
	[ -d $site ] || mkdir -p $site
	java -jar ~/git/gerrit/buck-out/gen/release.war init --batch -d $site
	$site/bin/gerrit.sh stop
	sed -r -i 's/type.*=.*OPENID/type = DEVELOPMENT_BECOME_ANY_ACCOUNT/' $site/etc/gerrit.config
fi

if [ ! -f ~/bin/jgit ] ;then
	[ -d ~/bin ] || mkdir ~/bin
	cat <<'EOF' >~/bin/jgit
#!/bin/sh
java -jar ~/git/jgit/org.eclipse.jgit.pgm/target/jgit-cli.jar $*
EOF
	chmod +x ~/bin/jgit
fi

