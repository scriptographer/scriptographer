#!/bin/sh

TARGET=$1
OPTIONS=

if [ $# -eq 2 ]
then
	OPTIONS=-Dsingleclass=$2
fi

ant -lib build/classes:build/lib:lib $OPTIONS $TARGET
