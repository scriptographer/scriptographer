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
 * File created on Apr 10, 2007.
 *
 * $Id: $
 */

package com.scratchdisk.script.rhino;

import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Scope;

/**
 * @author lehni
 *
 */
public class RhinoScript extends Script {
	private org.mozilla.javascript.Script script;
	private RhinoEngine engine;

	public RhinoScript(RhinoEngine engine,
			org.mozilla.javascript.Script script, File file) {
		super(file);
		this.engine = engine;
		this.script = script;
	}
	
	public ScriptEngine getEngine() {
		return engine;
	}

	public Object execute(Scope scope) throws ScriptException {
		try {
			Context cx = Context.getCurrentContext();
			// TODO: typecast to JsContext can be wrong, e.g. when calling
			// from another language
			Object ret = script.exec(cx, ((RhinoScope) scope).getScope());
			if (ret instanceof Wrapper)
				ret = ((Wrapper) ret).unwrap();
			return ret;
		} catch (RhinoException re) {
			throw new RhinoScriptException(re);
		}
	}
}

