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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Callable;

/**
 * @author lehni
 *
 */
public class RhinoCallable extends Callable {
	Function function;
	RhinoEngine engine;
	
	RhinoCallable(RhinoEngine engine, Function function) {
		this.engine = engine;
		this.function = function;
	}
	
	public Object call(Object obj, Object[] args) throws RhinoScriptException {
		// Retrieve wrapper object for the native java object, and call the
		// function on it.
		try {
			Scriptable scope = RhinoEngine.getWrapper(
					ScriptableObject.getTopLevelScope(function), obj);
			Object ret = function.call(Context.getCurrentContext(),
					scope.getParentScope(), scope, args);
			// unwrap if the return value is a native java object:
			if (ret instanceof Wrapper)
				ret = ((Wrapper) ret).unwrap();
			return ret;
		} catch (RhinoException re) {
			throw new RhinoScriptException(engine, re);
		}
	}

	public Function getCallable() {
		return function;
	}
}

