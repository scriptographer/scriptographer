#!/bin/sh

BASE=../../../..
JAVA=$BASE/java/build/
DOCS=$BASE/../docs/js/
CORE=$BASE/../src/js/core/

makeLink() {
	echo "Linking $2 -> $1"
	if [ -h $2 ]
	then
		rm $2
	fi
	ln -s $1 $2
}

makeTargetLinks() {
	makeLink $JAVA build/$1/$2/java
	makeLink $DOCS build/$1/$2/doc
	makeLink $CORE build/$1/$2/core
} 

makeLinks() {
	if [ ! -d build/$1 ]
	then
		mkdir build/$1
	fi
	makeTargetLinks $1 Debug
	makeTargetLinks $1 Release
}

makeLinks "CS4"
makeLinks "CS3"
makeLinks "CS2"
makeLinks "CS"
