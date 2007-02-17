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
 * File created on 25.12.2004.
 *
 * $Id: FunctionHelper.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scriptographer.script.rhino;

import org.mozilla.javascript.*;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class FunctionHelper {
	private FunctionHelper() {
		// Don't let anyone instantiate this class.
	}

	private static Object[] emptyArgs = new Object[0];

	/*
	 * Some static helper functions for getting and calling javascript
	 * functions.
	 * 
	 */
	public static Function getFunction(Scriptable scope, String name) {
		Object obj = scope.get(name, scope);
		return obj instanceof Function ? (Function) obj : null;
	}

	public static Object callFunction(Scriptable scope, Function func,
			Object args[]) {
		if (func != null) {
			ScriptographerEngine.beginExecution();
			Object ret = func.call(Context.getCurrentContext(), scope, scope, args);
			// commit all changed objects after a scripting function has been
			// called!
			ScriptographerEngine.endExecution();
			// unwrap if the return value is a native java object:
			if (ret != null && ret instanceof Wrapper) {
				ret = ((Wrapper) ret).unwrap();
			}
			return ret;
		}
		return null;
	}

	public static Object callFunction(Scriptable scope, Function func) {
		return callFunction(scope, func, emptyArgs);
	}

	public static Object callFunction(Scriptable scope, String name,
			Object args[]) {
		Function func = getFunction(scope, name);
		return func != null ? callFunction(scope, func, args) : null;
	}

	public static Object callFunction(Scriptable scope, String name) {
		return callFunction(scope, name, emptyArgs);
	}
}
