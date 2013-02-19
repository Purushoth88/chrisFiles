#!/bin/bash
#
# Returns the info to a show

set -x

root=`dirname $0`
otrUrl=http://www.onlinetvrecorder.com
chksum=$($root/getChecksum.sh)

/bin/curl -b "$root/cookies.txt" -s -G -d "email=$1" -d "pass=$2" -d "did=$3" -d "checksum=$chksum" "$otrUrl/downloader/api/request_file2.php"
