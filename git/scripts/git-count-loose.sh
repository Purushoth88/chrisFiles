#/!bin/sh

find $(git rev-parse --git-dir)/objects -type f | grep -E '/[0-9a-f]{2}/[0-9a-f]{38}$' | wc -l 
