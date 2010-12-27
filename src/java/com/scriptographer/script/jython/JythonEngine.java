/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Apr 14, 2007.
 */

package com.scriptographer.script.jython;

import java.io.File;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.core.__builtin__;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 *
 */
public class JythonEngine extends com.scratchdisk.script.jython.JythonEngine {
	public JythonEngine() {
		// TODO: why is this needed? It is added in the classloader already!
		PySystemState.add_classdir(ScriptographerEngine.getPluginDirectory() + "/java/classes");
		globals.__setitem__(new PyString("test"), new PyInteger(100));
		PyTuple all = new PyTuple(new PyString[] { Py.newString('*') });
		__builtin__.__import__("com.scriptographer.ai", globals, globals, all);
		__builtin__.__import__("com.scriptographer.ui", globals, globals, all);
//		__builtin__.eval(new PyString("from com.scriptographer.ai import *"), globals);
//		__builtin__.eval(new PyString("from com.scriptographer.ui import *"), globals);
	}

	public String[] getScriptPath(File file) {
		return ScriptographerEngine.getScriptPath(file, true);
	}
}
