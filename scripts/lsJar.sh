#!/bin/bash
# list the files inside all jar files found below the current directory
for f in $(find . -name '*.jar') ;do (jar -tf $f | sed -e s\&^\&$f\\\t\& ) ;done
