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

package com.scratchdisk.script.jython;

import java.io.File;

import org.python.core.Py;
import org.python.core.PyCode;

import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptException;

/**
 * @author lehni
 *
 */
public class JythonScript extends Script {
	PyCode code;
	JythonEngine engine;

	public JythonScript(JythonEngine engine, PyCode code, File file) {
		super(file);
		this.engine = engine;
		this.code = code;
	}

	public JythonEngine getEngine() {
		return engine;
	}

	public Object execute(Scope scope) throws ScriptException {
		try {
			// TODO: typecast to JythonScope can be wrong, e.g. when calling
			// from another language
			return Py.tojava(Py.runCode(code, ((JythonScope) scope).getScope(),
					engine.globals), Object.class);
		} catch (Throwable t) {
			throw new JythonException(t);
		}
	}
}
