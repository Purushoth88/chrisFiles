#!/bin/bash
#
# Gets the checksum to use the OnlineTvRecorder API 

dataDir="$(dirname $0)/data"

[ -d "$dataDir" ] || mkdir -p "$dataDir"
code=$($dataDir/../getCode.sh)
echo -ne "8512374682481164${code}rrzuj5jngrjh85" | md5sum | cut -d' ' -f 1 > "$dataDir/chksum"
[ ! -f "$dataDir/chksum" ] && exit 1
cat "$dataDir/chksum"
