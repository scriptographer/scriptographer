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
 * File created on Apr 14, 2007.
 *
 * $Id: $
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

	public PyObject getFunction() {
		return function;
	}
}
