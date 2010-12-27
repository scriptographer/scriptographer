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

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyObject;

import com.scratchdisk.script.Callable;

/**
 * @author lehni
 *
 */
public class JythonCallable extends Callable {
	PyObject function;
	
	JythonCallable(PyObject function) {
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
			Object res = ret.__tojava__(Object.class);
			return res != Py.NoConversion ? res : ret;
		} catch(PyException e) {
			throw new JythonException(e);
		}
	}

	public PyObject getCallable() {
		return function;
	}

	public JythonScope getScope() {
		// TODO: Implement
		return null;
	}
}
