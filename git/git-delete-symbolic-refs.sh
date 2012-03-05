#!/bin/sh

gitDir=$(readlink -f $(git rev-parse --git-dir))
grep -R -l -e '^ref:' $gitDir/refs | xargs -r rm
