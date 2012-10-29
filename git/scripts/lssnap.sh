#!/bin/bash

git --git-dir "$(git rev-parse --git-dir)/snapshot/.git" log --format="*** Snapshot: %s (%cd) ***" $* | grep -v "^\(---\|+++\|new file mode\|deleted file mode\|index\|@@\) " | sed "s/^diff --git.* b\//File: /"
