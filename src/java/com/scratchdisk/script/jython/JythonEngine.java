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
 * File created on Mar 7, 2007.
 */

package com.scratchdisk.script.jython;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.python.core.CompileMode;
import org.python.core.Py;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PySystemState;

import com.scratchdisk.script.ArgumentReader;
import com.scratchdisk.script.PropertyObserver;
import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;

/**
 * @author lehni
 *
 */
public class JythonEngine extends ScriptEngine {

	protected PyDictionary globals = new PyDictionary();

	public JythonEngine() {
		super("Python", "py");
		PySystemState.initialize();
		PySystemState state = new PySystemState();
		state.setClassLoader(Thread.currentThread().getContextClassLoader());
		Py.setSystemState(state);
	}

	protected Script compileScript(File file) throws ScriptException,
			IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return new JythonScript(this,
					Py.compile(in, file.getName(), CompileMode.exec), file);
		} catch (PyException e) {
			throw new JythonException(e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public Script compile(String code, String name) {
		return null;
	}

	public Scope createScope() {
		return new JythonScope();
	}

	public Scope getScope(Object object) {
		return new JythonScope(Py.java2py(object));
	}

	public Scope getGlobalScope() {
		return getScope(globals);
	}

	public <T> T toJava(Object object, Class<T> type) {
		if (object instanceof PyObject)
			return Py.tojava((PyObject) object, type);
		return null;
	}

	@Override
	public ArgumentReader getArgumentReader(Object object) {
		return null;
	}

	@Override
	public boolean observe(Map object, Object key, PropertyObserver observer) {
		return false;
	}
}
