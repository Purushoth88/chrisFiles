#!/bin/bash
# Set's up a gerrit with two users (admin,user) and a test-project
#
# Christian Halstrick, 2014

# get gerrit.war
gerrit_war=gerrit-2.9.1.war
[ -f $gerrit_war ] || wget http://gerrit-releases.storage.googleapis.com/$gerrit_war

# initialise & start a gerrit
[ -d gerrit ] && { echo "ERROR: gerrit folder already exists" ; exit 1 ; }
java -jar $gerrit_war init --batch -d gerrit

# configure gerrit
gerrit/bin/gerrit.sh stop
cd gerrit
git config -f etc/gerrit.config auth.type DEVELOPMENT_BECOME_ANY_ACCOUNT 
java -jar bin/gerrit.war gsql -c "insert into ACCOUNTS (ACCOUNT_ID, REGISTERED_ON) VALUES (1000000, NOW());"
java -jar bin/gerrit.war gsql -c "insert into ACCOUNT_EXTERNAL_IDS (ACCOUNT_ID, EXTERNAL_ID) VALUES (1000000, 'username:admin');"
java -jar bin/gerrit.war gsql -c "insert into ACCOUNT_EXTERNAL_IDS (ACCOUNT_ID, EXTERNAL_ID) VALUES (1000000, 'uuid:37ded9ac-e81e-4417-85d6-e4205e9fa7a3');"
java -jar bin/gerrit.war gsql -c "insert into ACCOUNT_SSH_KEYS (ACCOUNT_ID, SSH_PUBLIC_KEY, VALID) VALUES (1000000, '$(cat ~/.ssh/id_rsa.pub)', 'Y');"
java -jar bin/gerrit.war gsql -c "insert into ACCOUNT_GROUP_MEMBERS (ACCOUNT_ID, GROUP_ID) VALUES (1000000, 1);"
cd -

# start gerrit, make sure known_hosts contains correct entry
gerrit/bin/gerrit.sh start
ssh-keygen -f ~/.ssh/known_hosts -R localhost:29418
ssh-keyscan -p 29418 localhost >>~/.ssh/known_hosts

# update properties of admin, create second user
ssh -p 29418 admin@localhost gerrit set-account --full-name "Admin" --add-email "admin@admin.com" --http-password adminpwd admin
cat ~/.ssh/id_rsa.pub | ssh -p 29418 admin@localhost gerrit create-account --full-name "User" --email "user@user.com" --ssh-key - --http-password userpwd user

# create test-project
ssh -p 29418 admin@localhost gerrit create-group --member user test-project-owners
ssh -p 29418 admin@localhost gerrit create-project --owner test-project-owners --empty-commit test-project

# clone from both users with ssh and https
git clone http://user:userPwd@localhost:8080/test-project
