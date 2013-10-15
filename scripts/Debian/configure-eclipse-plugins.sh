#!/bin/bash
#
# Configure a Eclipse on a ubuntu system to my needs
#

# Install plugins to eclipse
# usage: installInEclipse <eclipse> <url> <commaSeperatedFeatures>
installInEclipse() {
	"$1" -application org.eclipse.equinox.p2.director -r "$2" -i $3
}

installInEclipse eclipse \
	http://download.eclipse.org/tools/orbit/downloads/drops/R20130827064939/repository/ \
	org.apache.ant,org.apache.commons.compress,org.apache.log4j,org.kohsuke.args4j,org.hamcrest,javaewah,org.objenesis,org.mockito,com.jcraft.jsch,org.junit,javax.servlet,org.tukaani.xz

installInEclipse eclipse \
	http://archive.eclipse.org/jetty/updates/jetty-bundles-7.x/7.6.0.v20120127/,http://download.eclipse.org/tools/orbit/downloads/drops/R20130827064939/repository/,http://download.eclipse.org/eclipse/updates/4.3 \
	org.eclipse.jetty.bundles.f.feature.group

installInEclipse eclipse \
	http://download.eclipse.org/egit/updates-nightly \
	org.eclipse.egit.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.java7.feature.group,org.eclipse.jgit.pgm.feature.group

installInEclipse eclipse \
	http://download.eclipse.org/eclipse/updates/4.3 \
	org.eclipse.pde.api.tools.ee.feature.feature.group

installInEclipse eclipse \
	http://update.eclemma.org/ \
	com.mountainminds.eclemma.feature.feature.group

installInEclipse eclipse \
	http://findbugs.cs.umd.edu/eclipse \
	edu.umd.cs.findbugs.plugin.eclipse.feature.group

installInEclipse eclipse \
	http://download.eclipse.org/mylyn/snapshots/weekly \
	org.eclipse.mylyn_feature.feature.group,org.eclipse.mylyn.bugzilla_feature.feature.group

installInEclipse eclipse \
	http://download.eclipse.org/reviews/nightly \
	org.eclipse.mylyn.reviews.feature.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group

installInEclipse eclipse \
	http://download.eclipse.org/technology/swtbot/snapshots \
	org.eclipse.swtbot.forms.feature.group,org.eclipse.swtbot.eclipse.feature.group,org.eclipse.swtbot.feature.group,org.eclipse.swtbot.ide.feature.group,org.eclipse.swtbot.eclipse.test.junit.feature.group
