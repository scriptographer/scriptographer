/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: FunctionHelper.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.js;

import org.mozilla.javascript.*;
import com.scriptographer.CommitManager;

import java.util.HashMap;
import java.util.Map;

public class FunctionHelper {
	private FunctionHelper() {
	}

	private static Object[] emptyArgs = new Object[0];
	/*
	 * Some static helper functions for getting and calling javascript functions.
	 * 
	 */
	public static Function getFunction(Scriptable scope, String name) {
		Object obj = scope.get(name, scope);
		return obj instanceof Function ? (Function)obj : null;
	}
	
	public static Object callFunction(Scriptable scope, Function func, Object args[]) throws Exception {
		Object ret = func.call(Context.getCurrentContext(), scope, scope, args);
		// commit all changed objects after a scripting function has been called!
		CommitManager.commit();
		// unwrap if the return value is a native java object:
		if (ret != null && ret instanceof NativeJavaObject) {
			ret = ((NativeJavaObject) ret).unwrap();
		}
		return ret;
	}

	public static Object callFunction(Scriptable scope, Function func) throws Exception {
		return callFunction(scope, func, emptyArgs);
	}

	public static Object callFunction(Scriptable scope, String name, Object args[]) throws Exception {
		Function func = getFunction(scope, name);
		if (func != null)
			return callFunction(scope, func, args);
		return null;
	}

	public static Object callFunction(Scriptable scope, String name) throws Exception {
		return callFunction(scope, name, emptyArgs);
	}

	public static Map convertToMap(NativeObject object) {
		HashMap map = new HashMap();
		Object[] ids = object.getIds();
		for (int i = 0; i < ids.length; i++) {
			Object id = ids[i];
			Object val = id instanceof String ? object.get((String) id, object) : object.get(((Number) id).intValue(), object);
			map.put(id, val);
		}
		return map;
	}
	
	public static Object[] convertToArray(NativeArray array) {
		Object[] objects = new Object[(int) array.getLength()];
		for (int i = 0; i < objects.length; i++) {
			Object obj = array.get(i, array);
			if (obj instanceof NativeObject)
				obj = convertToMap((NativeObject) obj);
			objects[i] = obj;
		}
		return objects;
	}
}
