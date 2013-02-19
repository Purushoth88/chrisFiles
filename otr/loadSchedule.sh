#!/bin/bash
#
# Loads the schedule for a specific day from onlinetvrecorder.com. Supports caching
#

root=`dirname $0`
otrUrl=http://www.onlinetvrecorder.com

day=$1

if [ ! -f "$root/$day" ] ;then
	curl "$otrUrl/epg/csv/epg_$day.csv" -o "$root/$day"	
fi

if [ -f "$root/$day" ] ;then
	cat "$root/$day"
	exit 0
else 
	exit 1
fi
