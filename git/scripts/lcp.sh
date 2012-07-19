#!/bin/sh
#******************************************************************************
# Copyright (c) 2006, 2007 Wind River Systems, Inc.
# All rights reserved. This program and the accompanying materials 
# are made available under the terms of the Eclipse Public License v1.0 
# which accompanies this distribution, and is available at 
# http://www.eclipse.org/legal/epl-v10.html 
# 
# Contributors: 
# Martin Oberhuber - initial API and implementation 
#******************************************************************************
#:#
#:# lcp - Count lines of code, data and documentation in a file submitted
#:# as a patch
#:#
#:# Usage:
#:#    lcp [-eh] {patchfile}
#:# Options:
#:#    -e  ..  include empty lines in count (like wc -l)
#:#    -h  ..  show help
#:# Examples:
#:#    lcp bug12345.patch.diff

curdir=`pwd`
case x$1 in
  x-e*) INCLUDE_EMPTY=1 ;;
  x-*) HELP=1 ;;
esac
if [ "$HELP" = "1" ]; then
  grep '^#:#' $0 | grep -v grep | sed -e 's,^#:#,,'
  exit 0
fi

if [ ! -f "$1" ]; then 
  echo "Error: Argument is not a file: $1"
  echo "Type `basename $0` -help for help"
  exit 1
fi

################################
# Here's our contribution line counting algorithm for patches:
# 1. Cat the patch (necessary to support patches embedding binary data)
# 2. Extract all lines that were added in the patch (starting with '+').
#    We use -a in order to work around embedded icons and other binary data
# 3. Suppress filenames (starting with '+++').
# 4. Suppress empty lines and lines only containing /*#{}
#
# Known limitations:
# a. "Moving around" type modications count too many + lines.

if [ x${INCLUDE_EMPTY} = x1 ]; then
  cat "$1" | grep -a '^[+]' | grep -a -v '^[+][+][+]' | wc -l
else
  cat "$1" | grep -a '^[+]' | grep -a -v '^[+][+][+]' \
      | egrep -a -v '^\+[^a-zA-Z0-9_!?"|@~`$%&()+;,.:<>=+-]*$' | wc -l
fi
