#!/bin/bash

# while in the intranet set the correct proxy
if ping -c 1 proxy.wdf.sap.corp ;then
	export http_proxy=http://proxy:8080 https_proxy=https://proxy:8080 no_proxy="wdf.sap.corp,nexus,jtrack,127.0.0.1,localhost,*.wdf.sap.corp"
else
	unset http_proxy https_proxy no_proxy
fi

date=$(date +%Y%m%d%H%M)

mkdir ~/bin
mkdir ~/log

[ -f ~/bin/"$1" ] && mv ~/bin/"$1" ~/bin/"$1.$date.bak"
(cd ~/bin; wget "https://raw.github.com/chalstrick/chrisFiles/master/scripts/Lubuntu/$1"; )
cmp -s ~/bin/"$1" ~/bin/"$1.$date.bak" && rm ~/bin/"$1.$date.bak"

chmod +x ~/bin/"$1"
bash -x ~/bin/"$1" 2>&1 | tee ~/log/"$1.$date.log"
