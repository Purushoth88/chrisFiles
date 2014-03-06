#!/bin/bash

rm -fr central client
git init central
(
	cd central
	touch a
	git add .
	git commit -m addA
	touch b
	git add .
	git commit -m addB
	touch c
	git add .
	git commit -m addC
)
git clone --no-hardlinks central client
(
	cd client
	git checkout -b feature HEAD~2
	touch 1
	git add .
	git commit -m add1
	touch 2
	git add .
	git commit -m add2
	git checkout master
	git merge feature
	touch 3
	git add .
	git commit -m add3
)	
(
	cd central
	touch d
	git add .
	git commit -m addD
)
