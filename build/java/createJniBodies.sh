#!/bin/sh

java -cp build/lib/js.jar org.mozilla.javascript.tools.shell.Main createJniBodies.js ../../src/cpp/jni/
