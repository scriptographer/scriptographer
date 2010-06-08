#!/bin/sh

# Create the root of the DMG, move the folder there

PACKAGES=$1
TARGET=$2
VERSION=$3

# Create root for package
mkdir root
mv Scriptographer root

# Create destination folder for version if it does not exist

if [ ! -d ${VERSION} ]
then
	mkdir ${VERSION}
fi


# CS has a different filename than the others
if [ $TARGET == "CS" ]
then
	PLUGIN=Scriptographer
else
	PLUGIN=Scriptographer.aip
fi

# Copy with all attributes, preserving packaged folders
ditto "$PACKAGES/$TARGET/Release/$PLUGIN" root/Scriptographer/$PLUGIN

# Create a DMG file containing the folder
DMG=${VERSION}/Scriptographer_Mac_${TARGET}_${VERSION}.dmg

# Remove it if it exists already
if [ -e $DMG ]
then
	rm $DMG
fi

hdiutil create -fs HFS+ -srcfolder root -volname Scriptographer $DMG

# To create zip files instead use: ditto -c -k root $ZIP

rm -r root/Scriptographer/$PLUGIN

# Remove root again
mv root/Scriptographer .
rmdir root
