# get jgit.sh script
[ ! -x jgit.sh ] && echo "I don't see jgit.sh. Download to this folder from http://www.eclipse.org/jgit/download/" && exit 1

# create a repo containing all different line endings
git init test_remote
cd test_remote
git config core.autocrlf false 
echo -e -n 'line1\r\nline2\r\nline3' > text.dos
echo -e -n 'line1\nline2\nline3' > text.unix
echo -e -n 'line1\r\nline2\nline3' > text.mixed
sleep 2
git add text.dos text.unix text.mixed
git commit -m "adding text files with different line endings"
sleep 2
cd ..

# clone the repo once with autcrlf and once without
git clone --config core.autocrlf=true test_remote test_autocrlftrue
git clone --config core.autocrlf=false test_remote test_autocrlffalse
git clone --config core.autocrlf=input test_remote test_autocrlfinput

# diff the working trees and check they only differ by whitspaces
diff    -q -s -x .git test_autocrlf{true,false}
diff -w -q -s -x .git test_autocrlf{true,false}

# check the status with native git
( cd test_autocrlftrue ; git status )
( cd test_autocrlffalse ; git status )

# modify the file text.dos in both repos and check the status with native git
sed -b -i -e 's/line/row/g' test_autocrlf{true,false}/text.dos
( cd test_autocrlftrue ; git status )
( cd test_autocrlffalse ; git status )

# revert the modification and check the status with native git
sed -b -i -e 's/row/line/g' test_autocrlf{true,false}/text.dos
( cd test_autocrlftrue ; git status )
( cd test_autocrlffalse ; git status )

# do the same thing with jgit
# make sure that jgit has checked out the files and created the index
( cd test_autocrlftrue ; rm  -f * .git/index ; jgit.sh reset --hard HEAD )
( cd test_autocrlffalse ; rm -f * .git/index ; jgit.sh reset --hard HEAD )

# diff the working trees and check they only differ by whitspaces
# BUG: the jgit checkout did the conversion also on a mixed line-endings file
diff -q -s -x .git test_autocrlf{true,false}
diff -w -q -s -x .git test_autocrlf{true,false}

# check the status with jgit
( cd test_autocrlftrue ; jgit.sh status )
( cd test_autocrlffalse ; jgit.sh status )

# modify the file text.dos in both repos and check the status with jgit
sed -b -i -e 's/line/row/g' test_autocrlf{true,false}/text.dos
( cd test_autocrlftrue ; jgit.sh status )
( cd test_autocrlffalse ; jgit.sh status )

# revert the modification and check the status with jgit
sed -b -i -e 's/row/line/g' test_autocrlf{true,false}/text.dos
( cd test_autocrlftrue ; jgit.sh status )
( cd test_autocrlffalse ; jgit.sh status )
