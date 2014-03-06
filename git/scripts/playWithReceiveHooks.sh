#!/bin/bash

set -x
rm -fr central.git developer

# create central repo
git init central
cd central
git commit --allow-empty -m initial
git branch side
cd ..
mv central/.git central.git
rm -fr central
git --git-dir central.git config core.bare true
echo -e '#!/bin/bash \n echo -e "pre-receive:\nrefs:\n$(git show-ref)\nInput:" && while read line ;do echo $line ;done' > central.git/hooks/pre-receive
echo -e '#!/bin/bash \n echo -e "post-receive:\nrefs:\n$(git show-ref)\nInput:" && while read line ;do echo $line ;done' > central.git/hooks/post-receive
chmod +x central.git/hooks/*-receive

# start a daeomon
git daemon --verbose --export-all --enable=receive-pack --base-path=. &

# create developers repo, modify and push
git clone git://localhost/central.git developer
cd developer
git commit --allow-empty -m modifiedByDev
git checkout -b side origin/side 
git merge master
git checkout master
git commit --allow-empty -m modifiedByDev2
git push origin master:refs/heads/master
git checkout side
git push origin side:refs/heads/side

# stop the daemon
kill %%
