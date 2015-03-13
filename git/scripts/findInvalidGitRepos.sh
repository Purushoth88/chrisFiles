#!/bin/bash
# Finds recursivly from current folder folders which end on .git but which don't contain 'config', 'refs' or 'objects'
#
# Christian Halstrick, SAP

find . -name '*.git' -type d | while read repo ;do
  [ -f "$repo/config" -a -d "$repo/refs" -a -d "$repo/objects" ] || echo $repo
done
