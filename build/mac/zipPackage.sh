#!/bin/sh

# For creating zip files instead use: ditto -c -k root Scriptographer_Mac_CS_$2.zip

# Create the root of the DMG, move the folder there

PACKAGES=$1
TARGET=$2
VERSION=$3

# create root for package
mkdir root
mv Scriptographer root

# CS has a different filename than the others
if [ $TARGET == "CS" ]
then
	PLUGIN=Scriptographer
else
	PLUGIN=Scriptographer.aip
fi

# copy with all attributes, even as packages (folders)
ditto "$PACKAGES/$TARGET/Release/$PLUGIN" root/Scriptographer/$PLUGIN

# Create a DMG file containing the folder
DMG=Scriptographer_Mac_$TARGET_$VERSION.dmg

# Remove it if it exists already
if [ -e $DMG ]
then
	rm $DMG
fi

hdiutil create -fs HFS+ -srcfolder root -volname Scriptographer $DMG

rm -r root/Scriptographer/$PLUGIN

# Remove root again
mv root/Scriptographer .
rmdir root
