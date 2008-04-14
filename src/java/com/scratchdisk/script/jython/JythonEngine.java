/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

package com.scratchdisk.script.jython;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PySystemState;

import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Scope;

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

	protected Script compileScript(File file) throws ScriptException, IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return new JythonScript(this,
					(PyCode) Py.compile(in, file.getName(), "exec"), file);
		} catch (PyException e) {
			throw new JythonException(e);
		} finally {
			if (in != null)
				in.close();
		}
	}

	public Scope createScope() {
		return new JythonScope();
	}

	public void evaluate(String string, Scope scope) throws ScriptException {
	}

	public Scope getScope(Object obj) {
		return new JythonScope(Py.java2py(obj));
	}

	public Scope getGlobalScope() {
		return getScope(globals);
	}
}
