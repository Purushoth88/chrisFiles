#!/bin/sh
rm -fr * .git
git init
tmpdir=/c/temp/$RANDOM
mkdir -p src
mkdir -p otherFolder
mkdir -p $tmpdir

### step 0: preparation
for i in Add Change Delete Moveaway Moveto Unchanged
do
  for j in Add Change Delete Moveaway Moveto Unchanged
  do
    echo "...
Line 1 of $i$j
...
Last Line of $i$j
Inital Content" >src/$i$j
  done
done
# remove stupid things
rm src/{Change,Add,Moveto,Unchanged}{Add,Moveto}
rm src/{Delete,Moveaway}{Change,Delete,Moveaway,Unchanged}
mv src/Add* $tmpdir
mv src/Moveto* otherFolder
# commit the stuff
git add src otherFolder
git commit -m "Preparation Commit"

# step 1: first commit 
for i in $tmpdir/Add*
do
  mv $i src
done
git add src
for i in src/Change*
do
  echo "Changing content of $i" >> $i
  git add $i
done
for i in src/Delete*
do
  cp $i $tmpdir
  git rm $i
done
for i in src/Moveaway*
do
  git mv $i otherFolder
done
for i in otherFolder/Moveto*
do
  git mv $i src
done
git commit -m "First commit"

# step 2: second commit 
for i in $tmpdir/*Add
do
  mv $i src
done
git add src
for i in src/*Change
do
  echo "Changing in second commit content of $i" >> $i
  git add $i
done
for i in src/*Delete
do
  cp $i $tmpdir
  git rm $i
done
for i in src/*Moveaway
do
  git mv $i otherFolder
done
for i in otherFolder/*Moveto
do
  git mv $i src
done
git commit -m "Second commit"
