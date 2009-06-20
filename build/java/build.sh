#!/bin/sh

TARGET=$1
shift

OPTIONS=
if [ $# -gt 0 ]
then
	OPTIONS=-Dclassmatch=\"$*\"
fi

ant -lib build/classes:build/lib:lib $OPTIONS $TARGET
