#!/bin/bash

date=$(date +%Y%m%d%H%M)

mkdir ~/bin
mkdir ~/log

[ -f ~/bin/"$1" ] && mv ~/bin/"$1" ~/bin/"$1.$date.bak"
(cd ~/bin; wget "http://raw.github.com/chalstrick/chrisFiles/master/scripts/Lubuntu/$1"; )
cmp -s ~/bin/"$1" ~/bin/"$1.$date.bak" && rm ~/bin/"$1.$date.bak"

chmod +x ~/bin/"$1"
bash -x ~/bin/"$1" 2>&1 | tee ~/log/"$1.$date.log"
