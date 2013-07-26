#!/bin/bash
#
# Create a new gerrit account with the given accountName and publicKeyFile
#

port=29418
gerritHost=myGerritHost
adminAccount=myId

usage="\
$(basename $0) [-p <port>] [-g <gerritHost>] [-a <theIdOfTheAdmin>] -u <nameOfNewAccount> -f <publicKeyFileName>\n\
Defaults: port:$port, gerritHost:$gerritHost, adminAccount:$adminAccount\n\
Example: $(basename $0) -u userX -f pubKeyOfUserX\n\
Example: $(basename $0) -p 29418 -g yourgit.wdf.sap.corp -a administrator -u userX -f pKeyOfUserX.pub"

while getopts "p:g:a:u:f:h?" opt; do
	case "$opt" in
	p)	port=$OPTARG
		;;
	g)	gerritHost=$OPTARG
		;;
	a)	adminAccount=$OPTARG
		;;
	u)	account=$OPTARG
		;;
	f)	publicKeyFile=$OPTARG
		;;
	h|\?)	echo "usage $usage"
		exit 0
		;;
	esac
done
shift $((OPTIND-1))
[ "$1" = "--" ] && shift

if [ -z "$publicKeyFile" ] ;then echo -e "publicKeyFile not specified\nusage: $usage"; exit 1 ; fi
if [ -z "$account" ] ;then echo -e "account not specified\nusage: $usage"; exit 2 ; fi

cat "$publicKeyFile" | ssh -p $port ${adminAccount}@${gerritHost} gerrit create-account --ssh-key - $account

