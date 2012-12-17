rm -fr * .git
git init
git config core.filemode true
echo 'text1' > a
echo 'text2' > b.exe
echo '#!/bin/sh' > c
ls -la
git add *
git ls-files -s
git commit -m 'added 3 files, msys-ls thinks 1 text, 2 executables'
git update-index --chmod=+x b.exe c
git ls-files -s
git commit -m 'explicitly made two files executable in git'
git push -f http://github.com/chalstrick/testRepo HEAD:refs/heads/master
