#!/bin/bash
#
# Configure a Eclipse(Juno) on a Lubuntu12.04 system to my needs
#

# install egit/jgit in juno
eclipse-juno -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/juno,http://download.eclipse.org/eclipse/updates/4.2  \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.import.feature.group,org.eclipse.egit.mylyn.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group,org.eclipse.mylyn.gerrit.feature.feature.group,org.eclipse.mylyn.git.feature.group,org.eclipse.mylyn.github.feature.feature.group,org.eclipse.cdt.autotools.feature.group,org.eclipse.cdt.feature.group,org.eclipse.m2e.feature.feature.group,org.eclipse.pde.api.tools.ee.j2se15.group,org.eclipse.pde.api.tools.ee.javase16.group,org.eclipse.pde.api.tools.ee.javase17.group

# install egit/jgit in indigo (not all components)
eclipse -application org.eclipse.equinox.p2.director \
	-r http://download.eclipse.org/egit/updates,http://download.eclipse.org/releases/indigo \
	-i org.eclipse.egit.feature.group,org.eclipse.egit.psf.feature.group,org.eclipse.jgit.feature.group,org.eclipse.jgit.pgm.feature.group

