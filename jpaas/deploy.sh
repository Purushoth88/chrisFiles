#!/bin/bash
baseDir=.
space=sap
appliance=d032780
component=chris120911a
password=<secret>
user=<user>
./neo.sh stop -password $password -space $space -appliance $appliance -component $component -user $user $baseDir/deploy.properties -sync
./neo.sh undeploy -password $password -space $space -appliance $appliance -component $component -user $user $baseDir/deploy.properties
./neo.sh deploy -password $password -space $space -appliance $appliance -component $component -user $user $baseDir/deploy.properties
./neo.sh put-destination -password $password -host prod.jpaas.sapbydesign.com -space $space -appliance $appliance -component $component -service connectivity -user $user -localpath $baseDir/domain
./neo.sh put-destination -password $password -host prod.jpaas.sapbydesign.com -space $space -appliance $appliance -component $component -service connectivity -user $user -localpath $baseDir/ecm
./neo.sh put-destination -password $password -host prod.jpaas.sapbydesign.com -space $space -appliance $appliance -component $component -service connectivity -user $user -localpath $baseDir/persistence
./neo.sh start -password $password  -space $space -appliance $appliance -component $component -user $user $baseDir/deploy.properties

