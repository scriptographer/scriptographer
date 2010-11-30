/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
