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

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ReadOnlyList;
import com.scratchdisk.util.WeakIdentityHashMap;

/**
 * @author lehni
 */
public class RhinoWrapFactory extends WrapFactory {
	private WeakIdentityHashMap wrappers = new WeakIdentityHashMap();
	
	public RhinoWrapFactory() {
		this.setJavaPrimitiveWrap(false);
	}

	public Scriptable wrapCustom(Context cx, Scriptable scope,
			Object javaObj, Class staticType) {
		return null;
	}

	public Object wrap(Context cx, Scriptable scope, Object obj, Class staticType) {
        if (obj == null || obj == Undefined.instance || obj instanceof Scriptable)
            return obj;
        // Allways override staticType and set itto the native type of
		// the class. Sometimes the interface used to acces an object of
        // a certain class is passed.
		// But why should it be wrapped that way?
        if (staticType == null || !staticType.isPrimitive())
			staticType = obj.getClass();
		Object result = staticType != null && staticType.isArray() ?
				new ExtendedJavaArray(scope, obj, staticType, true) :
				super.wrap(cx, scope, obj, staticType);
        return result;
	}

	public Scriptable wrapNewObject(Context cx, Scriptable scope, Object obj) {
		return (Scriptable) (obj instanceof Scriptable ? obj :
				wrapAsJavaObject(cx, scope, obj, null));
	}

	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObj, Class staticType) {
		// Keep track of wrappers so that if a given object needs to be
		// wrapped again, take the wrapper from the pool...
		Scriptable obj = (Scriptable) wrappers.get(javaObj);
		if (obj == null) {
	        // Allways override staticType and set itto the native type
			// of the class. Sometimes the interface used to acces an
			// object of a certain class is passed. But why should it
			// be wrapped that way?
			staticType = javaObj.getClass();
			if (staticType != null && staticType.isArray())
				obj = new ExtendedJavaArray(scope, javaObj, staticType, true);
			else {
				if (javaObj instanceof ReadOnlyList)
					obj = new ListWrapper(scope, (ReadOnlyList) javaObj, staticType);
				else if (javaObj instanceof Map)
					obj = new MapWrapper(scope, (Map) javaObj, staticType);
				else {
					obj = wrapCustom(cx, scope, javaObj, staticType);
					if (obj == null)
						obj = new ExtendedJavaObject(scope, javaObj, staticType, true);
				}
			}
			wrappers.put(javaObj, obj);
		}
		return obj;
	}

	public Object coerceType(Class type, Object value) {
		// coerce native objects to maps when needed
		if (value instanceof NativeObject && Map.class.isAssignableFrom(type)) {
			// return convertToMap((NativeObject) value);
			return new MapAdapter((NativeObject) value);
		} else if (value instanceof Function && type == Callable.class) {
			return new RhinoCallable((Function) value);
		}
		return null;
	}
}

