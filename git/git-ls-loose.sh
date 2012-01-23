#/!bin/sh

find $(git rev-parse --git-dir) | grep -E '[0-9a-f]{38}$' | sed -r 's,^.*([0-9a-f][0-9a-f])/([0-9a-f]{38}),\1\2,' | sort | uniq 

