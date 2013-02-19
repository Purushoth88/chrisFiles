#!/bin/bash
#
# Loads the schedule for a specific day from onlinetvrecorder.com. Supports caching
#

day=$1

loadSchedule.sh $1 | cut -d \; -f 1,9
