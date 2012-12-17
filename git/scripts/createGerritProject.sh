#!/bin/sh
# PLEASE CONFIGURE THE FOLLOWING LINE
user=d032780
if [ $# -ne 2 ]; then
	echo "Usage: `basename $0` <projectname> <administratorname>"
	echo "Example: `basename $0` test/NGP/abcd d032780"
	exit -1
fi
project=$1
admin=$2
groupname=${project//\//_}_committers
ssh -p 29418 $user@git.wdf.sap.corp gerrit create-group --member $admin $groupname
ssh -p 29418 $user@git.wdf.sap.corp gerrit create-project --name $project --owner $groupname
