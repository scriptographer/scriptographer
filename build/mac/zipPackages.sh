#!/bin/sh

#CS1
ditto "$1/CS1/JVM/Release/Scriptographer" Scriptographer/Scriptographer
ditto -c -k Scriptographer Scriptographer_Mac_CS_$2.zip
rm -r Scriptographer/Scriptographer

#CS2
ditto "$1/CS2/JVM/Release/Scriptographer.aip" Scriptographer/Scriptographer.aip
ditto -c -k Scriptographer Scriptographer_Mac_CS2_$2.zip
rm -r Scriptographer/Scriptographer.aip
