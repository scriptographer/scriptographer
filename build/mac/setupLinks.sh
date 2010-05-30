#!/bin/sh

BASE=../../../../..
JAVA=$BASE/java/build/
JAVASCRIPT=$BASE/../src/js/core/
EXAMPLES=$BASE/src/js/scripts/
REFERENCE=$BASE/docs/js/

makeLink() {
	echo "Linking $2 -> $1"
	if [ -h $2 ]
	then
		rm $2
	fi
	ln -s $1 $2
}

makeTargetLinks() {
	if [ ! -d build/$1/$2/Core ]
	then
		mkdir build/$1/$2/Core
	fi
	makeLink $JAVA build/$1/$2/Core/Java
	makeLink $JAVASCRIPT build/$1/$2/Core/JavaScript
	makeLink $EXAMPLES build/$1/$2/Examples
	makeLink $REFERENCE build/$1/$2/Reference
} 

makeLinks() {
	if [ ! -d build/$1 ]
	then
		mkdir build/$1
	fi
	makeTargetLinks $1 Debug
	makeTargetLinks $1 Release
}

makeLinks "CS"
makeLinks "CS2"
makeLinks "CS3"
makeLinks "CS4"
makeLinks "CS5"
