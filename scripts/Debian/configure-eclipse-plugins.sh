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
	http://download.eclipse.org/tools/orbit/downloads/drops/R20140114142710/repository/,http://archive.eclipse.org/jetty/updates/jetty-bundles-7.x/7.6.0.v20120127,http://download.eclipse.org/eclipse/updates/4.3,http://download.eclipse.org/egit/updates-nightly,http://update.eclemma.org/,http://findbugs.cs.umd.edu/eclipse,http://download.eclipse.org/mylyn/snapshots/weekly,http://download.eclipse.org/reviews/nightly,http://download.eclipse.org/technology/swtbot/snapshots \
	org.apache.ant,org.apache.commons.compress,org.apache.log4j,org.kohsuke.args4j,org.hamcrest,javaewah,org.objenesis,org.mockito,com.jcraft.jsch,org.junit,javax.servlet,org.tukaani.xz,org.eclipse.jetty.bundles.f.feature.group,org.eclipse.egit.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.java7.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.pde.api.tools.ee.feature.feature.group,com.mountainminds.eclemma.feature.feature.group,edu.umd.cs.findbugs.plugin.eclipse.feature.group,org.eclipse.mylyn_feature.feature.group,org.eclipse.mylyn.bugzilla_feature.feature.group,org.eclipse.mylyn.reviews.feature.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.swtbot.forms.feature.group,org.eclipse.swtbot.eclipse.feature.group,org.eclipse.swtbot.feature.group,org.eclipse.swtbot.ide.feature.group,org.eclipse.swtbot.eclipse.test.junit.feature.group
