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

import java.io.File;

import org.python.core.Py;
import org.python.core.PyCode;

import com.scratchdisk.script.Scope;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
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
