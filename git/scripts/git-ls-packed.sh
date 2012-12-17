#/!bin/sh

for p in $(find $(git rev-parse --git-dir)/objects/pack -name '*.idx')
do
  git show-index < $p | cut -f 2 -d ' '
done | sort | uniq
