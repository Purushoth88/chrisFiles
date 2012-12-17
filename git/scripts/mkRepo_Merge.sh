#!/bin/sh
# Creates a new git repo for demonstrating git features.
projectName=DemoCalc

###### check that we are not running on an existing repo
if [ -d .git ] ;then
	if [ "$1" = "-f" ] ;then
		rm -fr .git 
	else
		echo "fatal: git repo already exists. Run in empty dir or use -f "
		exit 2
	fi
fi

###### initialize git repo and create an empty project
git init
mkdir $projectName
echo '<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>'$projectName'</name>
	<comment></comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
	</natures>
</projectDescription>' > $projectName/.project
echo '<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry kind="src" path="src"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"/>
	<classpathentry kind="output" path="bin"/>
</classpath>' > $projectName/.classpath
git add $projectName/.classpath $projectName/.project
git commit -m "adding empty eclipse project"
			
###### create initial version of Calc
file=$projectName/src/Calc.java
mkdir -p `dirname $file`
echo 'public class Calc {

	/** @return the sum of a and b */
	public int add(int a, int b) {
		return a+b;
	}

}
' > $file
git add $file
git commit -m "adding class Calc with add(int,int)"
git tag initalState

###### switch to branch 'Add_Pi' add a method pi
git checkout -b Add_Pi
sed '/^}/ i\
	/** @return pi */\
	public double pi() {\
		return java.lang.Math.PI;\
	}\
	' $file > a; mv a $file
git commit -a -m "Adding Calc.pi()"

###### switch to branch 'Add_E' add a method e
git checkout -b Add_E master
sed '/public class Calc/ a\
\
	/** @return e */\
	public double e() {\
		return java.lang.Math.E;\
	}' $file >a; mv a $file
git commit -a -m "Adding Calc.e()"

###### switch to branch 'Modify_Add_Double' and change add() to work with doubles
git checkout -b Modify_Add_Double master
sed 's/int add(int a, int b)/double add(double a, double b)/' $file >a; mv a $file
git commit -a -m "changing Calc.add() from working with ints to working with doubles"

###### switch to branch 'Modify_Add_Float' and change add() to work with float
git checkout -b Modify_Add_Float master
sed 's/int add(int a, int b)/float add(float a, float b)/' $file >a; mv a $file
git commit -a -m "changing Calc.add() from working with ints to working with floats"

####### switch to branch 'AddingLotOfTests' and add and modify lot of files
git checkout -b AddingTests master
for package in {1..5}
do
	mkdir -p $projectName/src/test/P$package
	for nr in {1..5}
	do
		newFile=$projectName/src/test/P$package/C$nr.java
		cp $file $newFile
		sed '1i\
package test.P'$package';
' $newFile >a; mv a $newFile
		sed "s/public class Calc/public class C$nr/" $newFile >a; mv a $newFile
	done
done
git add $projectName/src/test
git commit -a -m "adding lot of test files"
for package in {1..5}
do
	mkdir -p $projectName/src/test/P$package
	for nr in {1..5}
	do
		newFile=$projectName/src/test/P$package/C$nr.java
		sed '1i\
/** Added by Chris */
' $newFile >a; mv a $newFile
	done
done
git commit -a -m "modifiying lot of test files"

###### switch to branch 'Refactor' and move Calc into packackge xyz.com.test
git checkout -b RefactorCalc master
sed '1i\
package xyz.com.test;
' $file >a; mv a $file
mkdir -p $projectName/src/xyz/com/test
mv $file $projectName/src/xyz/com/test
file=$projectName/src/xyz/com/test/Calc.java
git add $file
git commit -a -m "moving Calc to xyz.com.test"

###### switch to branch AddingMult and do some commits on Calc
git checkout -b AddingSubMult master
file=$projectName/src/Calc.java
sed '/^}/ i\
	/** @return a-b */\
	public int sub(int a, int b) {\
		return a-b;\
	}\
	' $file >a; mv a $file
git commit -a -m "Adding sub()"
sed  '/^}/ i\
	/** @return a*b */\
	public double mult(int a, int b) {\
		return a*b;\
	}\
	' $file >a; mv a $file
git commit -a -m "Adding mult()"
