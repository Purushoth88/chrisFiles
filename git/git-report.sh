#!/bin/sh

scriptDir=$(dirname $0)
gitDir=$(readlink -f $(git rev-parse --git-dir))
echo "================================================================================"
echo "Report on repo $gitDir on $(date)"
echo
echo $(git count-objects -v)
echo
echo "Loose objects:"
$scriptDir/git-ls-loose.sh
echo
echo "Packed objects:"
$scriptDir/git-ls-packed.sh
echo
echo "all known refs:"
git show-ref
echo
echo "all packed refs:"
[ -f $gitDir/packed-refs ] && cat $gitDir/packed-refs
echo "================================================================================"

