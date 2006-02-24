#!/bin/sh

#CS1
ditto "$1/cs1/release/Scriptographer" Scriptographer/
ditto -c -k Scriptographer Scriptographer_Mac_CS1_$2.zip
rm Scriptographer/Scriptographer

#CS2
ditto "$1/cs2/release/Scriptographer.aip" Scriptographer/Scriptographer.aip
ditto -c -k Scriptographer Scriptographer_Mac_CS2_$2.zip
rm -r Scriptographer/Scriptographer.aip
