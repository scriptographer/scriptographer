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
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyObject;

import com.scratchdisk.script.Scope;

/**
 * @author lehni
 *
 */
public class JythonScope extends Scope {
	private PyObject scope;

	/**
	 * Wraps an existing scope
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
			if (obj == null)
				return null;
			if (obj.isCallable())
				return new JythonCallable(obj);
			return obj.__tojava__(Object.class);
		} catch (PyException e) {
			// catch not defined exception
			return null;
		}
	}

	public Object put(String name, Object value, boolean readOnly) {
		Object prev = get(name);
		// TODO: Implement readOnly
		scope.__setitem__(name, Py.java2py(value));
		return prev;
	}

	public Object[] getKeys() {
		// TODO: Implement
		return new Object[0];
	}
}
