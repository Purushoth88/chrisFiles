#!/bin/bash

# create a repo with three files with different line ending styles
git init
git config core.autocrlf false
echo -ne 'line1\r\nline2\r\n' > dos.txt
echo -ne 'line1\nline2\n' > unix.txt
echo -ne 'line1\r\nline2\n' > mixed.txt
git add *
git commit -m "add unix,dos and mixed style file"

# How do the working tree files and the index look like and what does git think about the status?
file *
git ls-files -s -t --debug

# set repo to autocrlf=input 
git config core.autocrlf input

# get the status again. Be aware that git tells dos.txt is clean although if you would say
# "git rm dos.txt && git add dos.txt" you would get new content 
git ls-files -s -t --debug -- dos.txt

# delete the index content and add the same files again but now with autocrlf=input 
git rm --cached *
git add *
# Compare len and content-sha1 to previous status --- len fields have not changed (they reflect
# the length of the files in the working tree) but sha1's are now all the same (because of 
# eol-canonicalization)
git ls-files -s -t --debug

