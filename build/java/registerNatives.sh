#!/bin/sh

java -cp build/lib/js.jar org.mozilla.javascript.tools.shell.Main registerNatives.js ../../src/cpp/jni/ ../../src/cpp/jni/registerNatives.cpp