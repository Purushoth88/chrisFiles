#!/bin/bash
# Creates multiple git repos each with a few eclipse projects. Each project contains a lot of compileable java files

nrOfRepos=2
projectsPerRepo=3
filesPerProject=20000

n2c="abcdefghij"
for (( r=1; r<=$nrOfRepos; r++ )) ;do
	repoPath=repo_$r
	mkdir $repoPath
	git -C $repoPath init
	for (( p=1; p<=$projectsPerRepo; p++ )) ;do
		projectPath=$repoPath/project_${r}_${p}
		mkdir $projectPath
		echo .
		cat >$projectPath/.project <<EOL
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
 <name>project_${r}_${p}</name>
 <comment></comment>
 <projects></projects>
 <buildSpec>
  <buildCommand>
   <name>org.eclipse.jdt.core.javabuilder</name>
   <arguments></arguments>
  </buildCommand>
 </buildSpec>
 <natures>
  <nature>org.eclipse.jdt.core.javanature</nature>
 </natures>i
</projectDescription>
EOL

		cat >$projectPath/.classpath <<EOL
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
 <classpathentry kind="src" path="src"/>
 <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"/>
 <classpathentry kind="output" path="bin"/>
</classpath>
EOL
			
		echo "bin/" >$projectPath/.gitignore
		mkdir $projectPath/src
		for (( f=10; f<10+$filesPerProject; f++ )) ;do
			if (( f%10 == 0 )) ;then
				d=""
				for (( i=0; 1+i<${#f}; i++ )) ;do c=${f:$i:1} ; d=$d/${n2c:$c:1} ;done
				d="${d:1}"
				mkdir $projectPath/src/$d
			fi
			cat >$projectPath/src/$d/Calc_${r}_${p}_${f}.java <<EOL
package ${d//\//.}; 
public class Calc_${r}_${p}_${f} {
 /** @return the sum of a and b */
 public int add(int a, int b) {
  return a+b;
 }
}
EOL
		done
	done
	git -C $repoPath add .
	git -C $repoPath commit -m initial
done
