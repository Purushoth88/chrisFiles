#!/bin/bash
#
# Append to each sha-1 (words consisting of exactly 40 hex chars) the string
# ' (ID_<n>)'. Each unique sha-1 gets it's own <n>. Makes it easier to read
# texts with a lot of sha-1's. You see that two sha-1's in the text are equal
# by comparing their small running id (which is easier than to compare 40
# characters).
# 
# Christian Halstrick, 2014

awk --posix '{
  for(i=1;i<=NF;i++)
    if ($i ~ /^[0-9a-fA-F]{40}$/)
      if (map[$i]=="")
	map[$i]=$i" (ID_"nr++")"
  out=$0
  for (key in map)
    gsub(key, map[key], out)
  print out
}'
