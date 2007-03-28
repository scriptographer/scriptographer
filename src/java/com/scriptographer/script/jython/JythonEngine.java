/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 * 
 * File created on Mar 7, 2007.
 *
 * $Id: $
 */

package com.scriptographer.script.jython;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PySystemState;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.script.Script;
import com.scriptographer.script.ScriptEngine;
import com.scriptographer.script.ScriptException;
import com.scriptographer.script.ScriptMethod;
import com.scriptographer.script.ScriptScope;

/**
 * @author lehni
 *
 */
public class JythonEngine extends ScriptEngine {

	public JythonEngine() {
		super("Python", new String[] { "py" });
		PySystemState.initialize();
		// TODO: why is this needed? It is added in the classloader already!
		PySystemState.add_classdir(ScriptographerEngine.getPluginDirectory() + "/java/classes");
		PySystemState state = new PySystemState();
        state.setClassLoader(Thread.currentThread().getContextClassLoader());
        Py.setSystemState(state);
	}

	protected Script compileScript(File file) throws ScriptException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return new JythonScript(Py.compile(in, file.getName(), "exec"), file);
		} catch (PyException e) {
			throw new JythonException(e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public ScriptScope createScope() {
		return new JythonScope();
	}

	public void evaluate(String string, ScriptScope scope) throws ScriptException {
	}

	public ScriptScope getScope(Object obj) {
		return new JythonScope(Py.java2py(obj));
	}

	private class JythonScript extends Script {
		PyCode code;

		public JythonScript(PyCode code, File file) {
			super(file);
			this.code = code;
		}

		public ScriptEngine getEngine() {
			return JythonEngine.this;
		}

		protected Object executeScript(ScriptScope scope) throws ScriptException {
			try {
				// TODO: typecast to JsContext can be wrong, e.g. when calling
				// from another language
				PyDictionary globals = new PyDictionary();
				return Py.tojava(Py.runCode(code, ((JythonScope) scope).getScope(), globals), Object.class);
			} catch (PyException re) {
				throw new JythonException(re);
			}
		}
	}
	
	private class JythonMethod extends ScriptMethod {
		PyObject function;
		
		JythonMethod(PyObject function) {
			this.function = function;
		}

		public Object call(Object obj, Object[] args) throws JythonException {
			// Retrieve wrapper object for the native java object, and call the
			// function on it.
			try {
				PyObject[] wrappedArgs = new PyObject[args == null ? 1 : args.length + 1];
				// self
				wrappedArgs[0] = Py.java2py(obj);
				// args
		        for (int i = 1; i < wrappedArgs.length; i++)
		        	wrappedArgs[i] = Py.java2py(args[i - 1]);

		        PyObject ret = function.__call__(wrappedArgs);
				// unwrap if the return value is a native java object:
				return Py.tojava(ret, Object.class);
			} catch(PyException e) {
				throw new JythonException(e);
			}
		}

		public PyObject getFunction() {
			return function;
		}

		public boolean toBoolean(Object object) {
			return Py.py2boolean((PyObject) object);
		}

		public int toInt(Object object) {
			try {
				return Py.py2int((PyObject) object);
			} catch (PyException e) {
				return 0;
			}
		}
	}

	private class JythonScope extends ScriptScope {
		private PyObject scope;

		/**
		 * Wrapps an existing scope
		 * 
		 * @param scope
		 */
		public JythonScope(PyObject scope) {
			this.scope = scope;
		}
		
		public JythonScope() {
			scope = new PyDictionary();
		}
		
		public PyObject getScope() {
			return scope;
		}

		public Object get(String name) {
			try {
				PyObject obj = scope.__getitem__(Py.java2py(name));
				if (obj.isCallable()) return new JythonMethod(obj);
				else return (obj == null) ? null : obj.__tojava__(Object.class);
			} catch (PyException e) {
				// catch not defined exception
				return null;
			}
		}

		public Object put(String name, Object value, boolean readOnly) {
			Object prev = this.get(name);
			scope.__setitem__(name, Py.java2py(value));
			return prev;
		}
	}

	private static class JythonException extends ScriptException {
		public JythonException(Throwable cause) {
			super(cause);
		}
	}
}
