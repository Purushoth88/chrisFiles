#!/bin/sh
# Creates multiple git repos each with a lot of eclipse projects

nrOfRepos=5
projectsPerRepo=100
filesPerProject=20

for (( r=1; r<=$nrOfRepos; r++ )) ;do
	repoPath=repo_$r
	mkdir $repoPath
	git -C $repoPath init
	for (( p=1; p<=$projectsPerRepo; p++ )) ;do
		projectPath=$repoPath/project_${r}_${p}
		mkdir $projectPath
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
		for (( f=1; f<=$filesPerProject; f++ )) ;do

			cat >$projectPath/src/Calc_${r}_${p}_${f}.java <<EOL
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
	git -C $repoPath commit -m "adding classes Calc_${r}_<x>_<y> with add(int,int) to project project_${r}_<x>"
done
