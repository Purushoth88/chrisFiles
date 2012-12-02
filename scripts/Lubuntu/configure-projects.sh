#!/bin/bash
#
# Clone and build projects
#

# clone git e/jgit & gerrit
mkdir -p ~/git
[ -d ~/git/git ] || git clone -q https://github.com/git/git.git ~/git/git &
[ -d ~/git/jgit ] || git clone -q https://git.eclipse.org/r/p/jgit/jgit ~/git/jgit &
[ -d ~/git/egit ] || git clone -q https://git.eclipse.org/r/p/egit/egit ~/git/egit &
[ -d ~/git/egit-github ] || git clone -q https://git.eclipse.org/r/p/egit/egit-github ~/git/egit-github &
[ -d ~/git/egit-pde ] || git clone -q https://git.eclipse.org/r/p/egit/egit-pde ~/git/egit-pde &
[ -d ~/git/gerrit ] || git clone -q https://gerrit.googlesource.com/gerrit ~/git/gerrit &

wait

# clone/fetch linux
if [ -d ~/git/linux ] ;then
	(cd ~/git/linux; git fetch)
else
	git clone http://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git ~/git/linux &
fi


# configure all gerrit repos to push to the review queue and add commit msg hooks
curl -o /tmp/commit-msg https://git.eclipse.org/r/tools/hooks/commit-msg
chmod +x /tmp/commit-msg
for i in jgit egit egit-pde egit-github ;do cp /tmp/commit-msg ~/git/$i/.git/hooks/commit-msg ; git config -f ~/git/$i/.git/config remote.origin.push HEAD:refs/for/master ;done

# build the projects
(cd ~/git/gerrit && git fetch && git pull && mvn package -DskipTests) &
(cd ~/git/git && git fetch && git pull && make configure && ./configure && make) &
(cd ~/git/jgit && git fetch && git pull && mvn install -DskipTests)
(cd ~/git/jgit/org.eclipse.jgit.packaging && git fetch && git pull && mvn install)
(cd ~/git/egit && git fetch && git pull && mvn -P skip-ui-tests install -DskipTests)
(cd ~/git/egit-github && git fetch && git pull && mvn install -DskipTests) &
(cd ~/git/egit-pde && git fetch && git pull && mvn install -DskipTests) &

wait

# Create a gerrit test site
if [ -f ~/git/gerrit/gerrit-war/target/gerrit*.war ] ;then
	[ -d ~/gerrit ] || mkdir ~/gerrit
	java -jar ~/git/gerrit/gerrit-war/target/gerrit*.war init --batch -d ~/gerrit/gerrit-testsite
	~/gerrit/gerrit-testsite/bin/gerrit.sh stop
	sed -r -i 's/type.*=.*OPENID/type = DEVELOPMENT_BECOME_ANY_ACCOUNT/' ~/gerrit/gerrit-testsite/etc/gerrit.config
fi

if [ -d ~/bin -a ! -f ~/bin/jgit ] ;then
	cat <<EOF >~/bin/jgit
#!/bin/sh
java -jar ~/git/jgit/org.eclipse.jgit.pgm/target/jgit-cli.jar $*
EOF
	chmod +x ~/bin/jgit
fi

[ -d ~/lib ] || mkdir ~/lib
if [ ! -f ~/lib/git_bookmarks.html ] ;then
	cat <<EOF >~/lib/git_bookmarks.html
<!DOCTYPE NETSCAPE-Bookmark-file-1>
<!-- This is an automatically generated file.
     It will be read and overwritten.
     DO NOT EDIT! -->
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>Git Bookmarks</TITLE>
<DL><p>
    <DL><p>
        <DT><A HREF="http://git.eclipse.org/r/#mine" ADD_DATE="1301301306" LAST_MODIFIED="1327913113" ICON_URI="https://git.eclipse.org/r/favicon.ico" ICON="data:image/x-icon;base64,AAABAAEAEBAQAAEABAAoAQAAFgAAACgAAAAQAAAAIAAAAAEABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMIAAoAHAK+s/wC0tv8Ax8f/AKfazQDX1f8AmP+UAJ/9ogC58MYAp/6oAKv7sgDB/8MA7vH9AN7/3QAAAAAA///3h3eHd4///4dxF3cRd///dxERgREY//+HERFxERfyI4eBF4cReCIzd3h3eHh3MiN4eHeHd3giM3cAAHAAByMjigAAsAAIIiKLzt7Lh4ciIprMnLh3hzIzNbq6iId/IkRkQyIj//80bd1DMiL//yRGZkMyMv//8jMzMiIv///4AQAA8AAAAPAAAADwAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAADwAAAA8AAAAPAACAHwAA" SHORTCUTURL="g">Gerrit (Eclipse)</A>
        <DT><A HREF="https://bugs.eclipse.org/bugs/" ADD_DATE="1324282137" LAST_MODIFIED="1324282531" ICON_URI="https://bugs.eclipse.org/bugs/images/favicon.ico" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC9ElEQVQ4jaXRW0xTBwDG8c7EPWi3gUjHgdZiQREtXmpRsWAFPVJKIb2M3mhppSBMBVIQCvUAFRBR0WDipQpiRtAMzcIStyVb9MWYLFmm2R6WxeiSLYu6GI0ucyYayd/Hw7sP/9ffw/cpKk1jvE+KStMYefnNZC/by/LMDvKWHWSNLsHqDAmjph9TtoQxs4P1mfvQZzSQl15LbvpnrM/xyYBa00S2OkrGwjgZigSi6iSBjRPUbTxNxYojlAjdFAlR1qXWs3KxG53SSf6nNTKg0raiWtBFyDrBD+N3+H7wPt+dfUwiOotjxxiWrcOIBoltuhYKl4ZZ83ENBYJHBnK29vGBopVvR28D8Oox+EPTVDiTuJ0XqKk+h91yHGuJhDl/PxtSvRhU8wCz+Sjh0nGuNd/l7UuIds7gqZ8gEr7Mbu8Uda6L+KvP4LYco7I4RrEuRJEwbwOl0kbwkzj3Jt/SFr3BhhKJz5uu0hS+QsT3BSHXBAH7Ofy2k7h2JhANjZhzQjIgnXhD7+hrRs7P0TPyktjwCw4e/Z/4yH/Ej/xL9+HnxIaeERt6QmzwIe19D2jp/F0G9pqTTA3/yvTlV5TbpvD4Zmmou05DcJZI4Cvqa78k7J8i6E7is49QJfYgmmIysD/+J+29D0lemSM28JS27vt09v9DZ/8jDvT9TUfvX3RIfxCVfqO1+xZNbTcJN96SAX3eHty7hvhp5hGJlm/QLrbirThOwDZG0HaM2urDeKviOCwRxG2lFOqt6NVBGahwjaLROdGnBfjx6zl2O04hGiP4ygfxWw/htfbgLG/Gst3BFoOHVdoABbrw/Bc2o1CoEYslZpK/cKn3Z3pqJ3GYu7CbW6gui1Bm8mEs8JAjuMn6yM5qrV8GFi1ah7Gwi672cRSKTZQWxBgIXcexaYAtqxpZmxtgpcaLOtVFysJy0j8sI1eokoGi4n1srxxGqTSgVJaiVXnRpPkRUjxkpfgQUtwIqU6yltjIWiKiSStDv9wmA+/TO8GeHzSxVSTTAAAAAElFTkSuQmCC" SHORTCUTURL="b" LAST_CHARSET="UTF-8">Bugzilla (E/JGit)</A>
        <DT><A HREF="http://wiki.eclipse.org/EGit/Contributor_Guide" ADD_DATE="1301301307" LAST_MODIFIED="1324282554" ICON_URI="http://wiki.eclipse.org/favicon.ico" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC9ElEQVQ4jaXRW0xTBwDG8c7EPWi3gUjHgdZiQREtXmpRsWAFPVJKIb2M3mhppSBMBVIQCvUAFRBR0WDipQpiRtAMzcIStyVb9MWYLFmm2R6WxeiSLYu6GI0ucyYayd/Hw7sP/9ffw/cpKk1jvE+KStMYefnNZC/by/LMDvKWHWSNLsHqDAmjph9TtoQxs4P1mfvQZzSQl15LbvpnrM/xyYBa00S2OkrGwjgZigSi6iSBjRPUbTxNxYojlAjdFAlR1qXWs3KxG53SSf6nNTKg0raiWtBFyDrBD+N3+H7wPt+dfUwiOotjxxiWrcOIBoltuhYKl4ZZ83ENBYJHBnK29vGBopVvR28D8Oox+EPTVDiTuJ0XqKk+h91yHGuJhDl/PxtSvRhU8wCz+Sjh0nGuNd/l7UuIds7gqZ8gEr7Mbu8Uda6L+KvP4LYco7I4RrEuRJEwbwOl0kbwkzj3Jt/SFr3BhhKJz5uu0hS+QsT3BSHXBAH7Ofy2k7h2JhANjZhzQjIgnXhD7+hrRs7P0TPyktjwCw4e/Z/4yH/Ej/xL9+HnxIaeERt6QmzwIe19D2jp/F0G9pqTTA3/yvTlV5TbpvD4Zmmou05DcJZI4Cvqa78k7J8i6E7is49QJfYgmmIysD/+J+29D0lemSM28JS27vt09v9DZ/8jDvT9TUfvX3RIfxCVfqO1+xZNbTcJN96SAX3eHty7hvhp5hGJlm/QLrbirThOwDZG0HaM2urDeKviOCwRxG2lFOqt6NVBGahwjaLROdGnBfjx6zl2O04hGiP4ygfxWw/htfbgLG/Gst3BFoOHVdoABbrw/Bc2o1CoEYslZpK/cKn3Z3pqJ3GYu7CbW6gui1Bm8mEs8JAjuMn6yM5qrV8GFi1ah7Gwi672cRSKTZQWxBgIXcexaYAtqxpZmxtgpcaLOtVFysJy0j8sI1eokoGi4n1srxxGqTSgVJaiVXnRpPkRUjxkpfgQUtwIqU6yltjIWiKiSStDv9wmA+/TO8GeHzSxVSTTAAAAAElFTkSuQmCC" SHORTCUTURL="c">EGit/Contributor</A>
        <DT><A HREF="http://www.eclipse.org/forums/index.php?t=thread&frm_id=48" ADD_DATE="1301301307" LAST_MODIFIED="1324282470" ICON_URI="http://www.eclipse.org/favicon.ico" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC9ElEQVQ4jaXRW0xTBwDG8c7EPWi3gUjHgdZiQREtXmpRsWAFPVJKIb2M3mhppSBMBVIQCvUAFRBR0WDipQpiRtAMzcIStyVb9MWYLFmm2R6WxeiSLYu6GI0ucyYayd/Hw7sP/9ffw/cpKk1jvE+KStMYefnNZC/by/LMDvKWHWSNLsHqDAmjph9TtoQxs4P1mfvQZzSQl15LbvpnrM/xyYBa00S2OkrGwjgZigSi6iSBjRPUbTxNxYojlAjdFAlR1qXWs3KxG53SSf6nNTKg0raiWtBFyDrBD+N3+H7wPt+dfUwiOotjxxiWrcOIBoltuhYKl4ZZ83ENBYJHBnK29vGBopVvR28D8Oox+EPTVDiTuJ0XqKk+h91yHGuJhDl/PxtSvRhU8wCz+Sjh0nGuNd/l7UuIds7gqZ8gEr7Mbu8Uda6L+KvP4LYco7I4RrEuRJEwbwOl0kbwkzj3Jt/SFr3BhhKJz5uu0hS+QsT3BSHXBAH7Ofy2k7h2JhANjZhzQjIgnXhD7+hrRs7P0TPyktjwCw4e/Z/4yH/Ej/xL9+HnxIaeERt6QmzwIe19D2jp/F0G9pqTTA3/yvTlV5TbpvD4Zmmou05DcJZI4Cvqa78k7J8i6E7is49QJfYgmmIysD/+J+29D0lemSM28JS27vt09v9DZ/8jDvT9TUfvX3RIfxCVfqO1+xZNbTcJN96SAX3eHty7hvhp5hGJlm/QLrbirThOwDZG0HaM2urDeKviOCwRxG2lFOqt6NVBGahwjaLROdGnBfjx6zl2O04hGiP4ygfxWw/htfbgLG/Gst3BFoOHVdoABbrw/Bc2o1CoEYslZpK/cKn3Z3pqJ3GYu7CbW6gui1Bm8mEs8JAjuMn6yM5qrV8GFi1ah7Gwi672cRSKTZQWxBgIXcexaYAtqxpZmxtgpcaLOtVFysJy0j8sI1eokoGi4n1srxxGqTSgVJaiVXnRpPkRUjxkpfgQUtwIqU6yltjIWiKiSStDv9wmA+/TO8GeHzSxVSTTAAAAAElFTkSuQmCC" SHORTCUTURL="f">E/JGit Forum</A>
        <DT><A HREF="https://github.com/" ADD_DATE="1324284023" LAST_MODIFIED="1324284031" ICON_URI="https://github.com/favicon.ico" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABZUlEQVQ4jZVTLU8DURDcH0AQ9yvIveS2M5PUIQgCg6Ci6T8g/IgaJFjcSUIFAst/qGgdJKhLEAhCAwJ1hA+zbe4OGuAlz7y3OzO7O2vWOSmlPskSQCWpllQDqEiWKaV+N351sizblDSR9Clp7u5jkiOSI3cfS5rH3yTLss2fkqeSFu4+WEci6UDSQtK0BRLMC0lb39Db5W0AyCN2sqo5pB24OwB8ALgiedjr9U5Inrr7EYBzSW9FUeyGks+UUt9IlpLmHbC11933QvWcZGkAKncfm5m5+/FvAADOInYMoDJJNclRoN79BiDpycyM5EhSbTHrYQDc/0HBa8QOJdWtEgBc/0HBtFVCNHFmZlYUxXYoWsf+7u77oWBGslx1fmmgsPKFpIdG4iPJS0k7wT5YjbFhpCcAecNcswZAtXzP8zy1jNS0MoDnxkRemvKXnQfw/M3KDZBJMN50ewHgdu0ydfz+r3X+AiUf9nJ8euY0AAAAAElFTkSuQmCC" LAST_CHARSET="UTF-8">GitHub</A>
        <DT><A HREF="http://git-scm.com/docs" ADD_DATE="1339166134" LAST_MODIFIED="1339166134" ICON_URI="http://git-scm.com/favicon.png" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAABXFBMVEXnSCXnSCXnSCXmSCXwSyb4TijsSiXzTCf4Tij5Tij4Tij5Tij5TijySyb5TSfrSibvSyb4Tij5Tij6TyjzTCf4TijvSyb5TijwSyb5Tij5Tij4Tij4TijrSSX4TijxTCb7Tyj1TSfqSSXoSSX6Tyj6Tyj3TifqSSX4TijqSSX6Tyj1TSf2TSf7TyjmSCTmSCT0TSf2TSfzTCf0TSfsSiX0TSf1TSf1TSfwSybrSSXtSibzTCjyTCbvSifuSif5Tij0TSfxSybvSyb7TyjuSiX6Tyj7TyjyTCbzTCjlRyXnSCb0TSf2Tif4Tij4Tij3Tif1TSf2TSf8Tyj8Tyj1TSf1TSf1TSf5Tij5Tij5Tij0TSf2TSf5TijyTCbyTCb8TygAAADmSCTlRyTmSCTmSCTmSCT3TSf3Tij6Tij5Tij8TyjpSCX6Tyj4Tij3Tif1TSf2TSf0TSf7TyjzTChxYOXdAAAAYXRSTlP9/v7+yAHgtRMUA1NawaPd0V4aa7h7yVnHDAUOluthx2Go5vOCDWHcFdKIBg9T+O6Km7OEwg0Pj9HT0i4xrrOSRNHUROJCNjlN6NtCQ0JDQmBDSlBDQmEIV0NhYVRHRl0ApcTqLwAAAWZJREFUeNp10WdvwjAQBmBL3Xvvvffee+89gULbFz6UpmADyf+XanOKzEWtP9nOo9d3FwG2ZtYPnvgNB/2u6/58/gs6m9IuE0HQ+J3mIgjQxkQQtDQDMSY46JWyHQj3pKxgYFRJKVs7gJFhXzAQVSqpRbfZ1+tnSFgQzWaV0hkD5lDj12HBfVYvkzHUBSDMKjXgNZMhoWRdZQio0oJeIfCWy1hRUl4WYt0K/d0AI76UKgJkKRCr7fOFOPdyevkZcSAuiysATJJ4FBeelwckNBBSVgMATexYXHpeQUbCJMgGAkYcijPPCgJJOTgO7Dn5Sk8F3guFAbqX1OKm4xhxBAF8FAqAZiodI05gAAkCE8CY7jZJIgICNmNuCpie9TMiIFAg5s1hgf6LvIEFfqXLZr9BU7+FBTZjaWV1jWZ6Bw78jEScJnaNIODdPiAIeLcv+AvYmT6Dg2DGFTgIZuzjf4Dt3Z0tfvMLeqCGtL46hgQAAAAASUVORK5CYII=">Git - Reference</A>
        <DT><A HREF="http://dev.eclipse.org/mhonarc/lists/jgit-dev/" ADD_DATE="1300959368" LAST_MODIFIED="1342090229" ICON_URI="http://dev.eclipse.org/favicon.ico" ICON="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAC9ElEQVQ4jaXRW0xTBwDG8c7EPWi3gUjHgdZiQREtXmpRsWAFPVJKIb2M3mhppSBMBVIQCvUAFRBR0WDipQpiRtAMzcIStyVb9MWYLFmm2R6WxeiSLYu6GI0ucyYayd/Hw7sP/9ffw/cpKk1jvE+KStMYefnNZC/by/LMDvKWHWSNLsHqDAmjph9TtoQxs4P1mfvQZzSQl15LbvpnrM/xyYBa00S2OkrGwjgZigSi6iSBjRPUbTxNxYojlAjdFAlR1qXWs3KxG53SSf6nNTKg0raiWtBFyDrBD+N3+H7wPt+dfUwiOotjxxiWrcOIBoltuhYKl4ZZ83ENBYJHBnK29vGBopVvR28D8Oox+EPTVDiTuJ0XqKk+h91yHGuJhDl/PxtSvRhU8wCz+Sjh0nGuNd/l7UuIds7gqZ8gEr7Mbu8Uda6L+KvP4LYco7I4RrEuRJEwbwOl0kbwkzj3Jt/SFr3BhhKJz5uu0hS+QsT3BSHXBAH7Ofy2k7h2JhANjZhzQjIgnXhD7+hrRs7P0TPyktjwCw4e/Z/4yH/Ej/xL9+HnxIaeERt6QmzwIe19D2jp/F0G9pqTTA3/yvTlV5TbpvD4Zmmou05DcJZI4Cvqa78k7J8i6E7is49QJfYgmmIysD/+J+29D0lemSM28JS27vt09v9DZ/8jDvT9TUfvX3RIfxCVfqO1+xZNbTcJN96SAX3eHty7hvhp5hGJlm/QLrbirThOwDZG0HaM2urDeKviOCwRxG2lFOqt6NVBGahwjaLROdGnBfjx6zl2O04hGiP4ygfxWw/htfbgLG/Gst3BFoOHVdoABbrw/Bc2o1CoEYslZpK/cKn3Z3pqJ3GYu7CbW6gui1Bm8mEs8JAjuMn6yM5qrV8GFi1ah7Gwi672cRSKTZQWxBgIXcexaYAtqxpZmxtgpcaLOtVFysJy0j8sI1eokoGi4n1srxxGqTSgVJaiVXnRpPkRUjxkpfgQUtwIqU6yltjIWiKiSStDv9wmA+/TO8GeHzSxVSTTAAAAAElFTkSuQmCC">jgit ML</A>
    </DL><p>
</DL><p>
EOF
fi

wait
