#!/bin/bash
#
# Logs in to onlinetvrecorder. Returns the sessionId as a cookie in cookies.txt

otrUrl=http://www.onlinetvrecorder.com
dataDir="$(dirname $0)/data"

[ -d "$dataDir" ] || mkdir -p "$dataDir"
[ -f "$dataDir/code" ] && rm -f "$dataDir/code"
[ ! -f "$dataDir/code" ] && curl -s "$otrUrl/downloader/api/getcode.php" -o "$dataDir/code"
[ ! -f "$dataDir/code" ] && exit 1
cat "$dataDir/code"
email=
did=
password=
chksum=$($root/getChecksum.sh)

rm -f "$root/cookies.txt"
/bin/curl -c "$root/cookies.txt" -s -G -d "email=$email" -d "pass=$password" -d "did=$did" -d "checksum=$chksum" "$otrUrl/downloader/api/login.php"
