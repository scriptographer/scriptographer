#!/bin/sh

# Create the root of the DMG, move the folder there
mkdir root
mv Scriptographer root

# CS
ditto "$1/CS/JVM/Release/Scriptographer" root/Scriptographer/Scriptographer
# For creating zip files: ditto -c -k root Scriptographer_Mac_CS_$2.zip

# Create a DMG file containing the folder
DMG=Scriptographer_Mac_CS_$2.dmg

# Remove it if it exists already
if [ -e $DMG ]
then
	rm $DMG
fi

hdiutil create -fs HFS+ -srcfolder root -volname Scriptographer $DMG

rm -r root/Scriptographer/Scriptographer

# CS2
ditto "$1/CS2/JVM/Release/Scriptographer.aip" root/Scriptographer/Scriptographer.aip
#ditto -c -k root Scriptographer_Mac_CS2_$2.zip

# Create a DMG file containing the folder
DMG=Scriptographer_Mac_CS2_$2.dmg

# Remove it if it exists already
if [ -e $DMG ]
then
	rm $DMG
fi

hdiutil create -fs HFS+ -srcfolder root -volname Scriptographer $DMG

rm -r root/Scriptographer/Scriptographer.aip

# Remove root again
mv root/Scriptographer .
rmdir root
