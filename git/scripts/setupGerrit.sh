#!/bin/bash
# Set's up a gerrit with two users (admin,user) and a test-project
#
# Christian Halstrick, 2014

# get gerrit.war
gerrit_war=gerrit-2.11.1.war
[ -f gerrit.war ] || wget -O gerrit.war http://gerrit-releases.storage.googleapis.com/$gerrit_war || exit -1

# initialise & start a gerrit
[ -d gerrit ] || java -jar gerrit.war init --batch --no-auto-start -d gerrit || exit -2

# configure gerrit
git config -f gerrit/etc/gerrit.config auth.type DEVELOPMENT_BECOME_ANY_ACCOUNT 

# start gerrit, make sure known_hosts contains correct entry
gerrit/bin/gerrit.sh start
ssh-keygen -f ~/.ssh/known_hosts -R localhost:29418
ssh-keyscan -p 29418 localhost >>~/.ssh/known_hosts

# update properties of admin, create second user
read -p "Navigate to http://localhost:8080/#/register/ and create a user admin with known ssh key" resp
ssh -p 29418 admin@localhost gerrit set-account --full-name "Admin" --add-email "admin@example.com" --http-password admin admin
ssh -p 29418 admin@localhost gerrit create-account --full-name "User" --email "user@example.com" --ssh-key - --http-password user user <~/.ssh/id_rsa.pub

# create test-projects
for prj in testInitialNoCommit testInitialEmptyCommit testNoReviews testSomeReviews ;do
  ssh -p 29418 admin@localhost gerrit create-group --member user $prj-owners
  [ $prj == "testInitialNoCommit" ] && emptyCommit="" || emptyCommit="--empty-commit"
  ssh -p 29418 admin@localhost gerrit create-project $emptyCommit --owner $prj-owners $prj
  git clone http://user:user@localhost:8080/$prj
  scp -p -P 29418 user@localhost:hooks/commit-msg $prj/.git/hooks/ 
  if [ $prj == "testNoReviews" -o $prj == "testSomeReviews" ] ;then
    touch $prj/a
    git -C $prj add a
    git -C $prj commit -m "add a to $prj"
    [ $prj == "testNoReviews" ] && refSpec="HEAD:refs/heads/master" || refSpec="HEAD:refs/for/master"
    git -C $prj push origin $refSpec
  fi
done
