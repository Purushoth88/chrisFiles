#!/bin/bash
# test git/jgit filter calling
# usage: testFilter.sh <gitClient> <sleepAfterEdit>
# e.g. "testFilter.sh jgit.sh 2"

GITCLIENT="$1"
SLEEPTIME="$2"

mkdir test
cd test
$GITCLIENT init
git config filter.aCap.clean 'sed s/a/aX/g && echo $(date): '$GITCLIENT' triggered clean %f >>/tmp/filterLog'
git config filter.aCap.smudge 'sed s/b/bY/g && echo $(date): '$GITCLIENT' triggered smudge %f >>/tmp/filterLog'
echo "*.txt filter=aCap" >.gitattributes
$GITCLIENT add .gitattributes
$GITCLIENT commit -m addAttributes
$GITCLIENT branch init
echo "abcABC" >bob.txt
[ $SLEEPTIME -gt 0 ] && sleep $SLEEPTIME
$GITCLIENT add bob.txt
$GITCLIENT commit -m addBob
$GITCLIENT checkout init
$GITCLIENT checkout master
echo -n File: && cat bob.txt
echo -n Index: && git cat-file -p :bob.txt
echo "FilterLog: "
cat /tmp/filterLog
