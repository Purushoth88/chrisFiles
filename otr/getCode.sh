#!/bin/bash
#
# Gets the "code" to use the OnlineTvRecorder API

otrUrl=http://www.onlinetvrecorder.com
dataDir="$(dirname $0)/data"

[ -d "$dataDir" ] || mkdir -p "$dataDir"
[ -f "$dataDir/code" ] && rm -f "$dataDir/code"
[ ! -f "$dataDir/code" ] && curl -s "$otrUrl/downloader/api/getcode.php" -o "$dataDir/code"
[ ! -f "$dataDir/code" ] && exit 1
cat "$dataDir/code"