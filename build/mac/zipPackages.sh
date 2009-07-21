#!/bin/sh

# For creating zip files instead use: ditto -c -k root Scriptographer_Mac_CS_$2.zip

# Create the root of the DMG, move the folder there

PACKAGES=$1
VERSION=$2

# create root for package
mkdir root
mv Scriptographer root

zipPackage() {
	# CS has a different filename than the others
	if [ $1 == "CS" ]
	then
		PLUGIN=Scriptographer
	else
		PLUGIN=Scriptographer.aip
	fi

	# copy with all attributes, even as packages (folders)
	ditto "$PACKAGES/$1/Release/$PLUGIN" root/Scriptographer/$PLUGIN
	
	# Create a DMG file containing the folder
	DMG=Scriptographer_Mac_$1_$VERSION.dmg
	
	# Remove it if it exists already
	if [ -e $DMG ]
	then
		rm $DMG
	fi
	
	hdiutil create -fs HFS+ -srcfolder root -volname Scriptographer $DMG
	
	rm -r root/Scriptographer/$PLUGIN
}

zipPackage "CS4"
zipPackage "CS3"
zipPackage "CS2"
zipPackage "CS"

# Remove root again
mv root/Scriptographer .
rmdir root
