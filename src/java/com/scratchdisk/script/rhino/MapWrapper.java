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
 * File created on 19.02.2005.
 * 
 * $Id: MapObject.java 230 2007-01-16 20:36:33Z lehni $
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

import java.util.Map;

/**
 * Wrapper class for java.util.Map objects It adds js object-like properties, so
 * it is possible to access lists like this: map["value"], or map[10] It also
 * defines getIds(), so enumeration is possible too: for (var i in list) ... But
 * it does not allow to access by anything else than Integer or String, so not
 * the whole functionality Of maps is provided. For scriptographer it is enough,
 * though. And the real functions can still be accessed, as the class extends a
 * NativeJavaObject
 * 
 * @author lehni
 */
public class MapWrapper extends NativeJavaObject {
	public MapWrapper(Scriptable scope, Map map, Class staticType) {
		super(scope, map, staticType);
	}

	public Object[] getIds() {
		if (javaObject != null) {
			return ((Map) javaObject).keySet().toArray();
		} else {
			return new Object[0];
		}
	}

	public boolean has(int index, Scriptable start) {
		return javaObject != null
			&& ((Map) javaObject).get(Integer.toString(index)) != null;
	}

	public Object get(int index, Scriptable scriptable) {
		if (javaObject != null) {
			Object obj = ((Map) javaObject).get(Integer.toString(index));
			if (obj != null)
				return Context.toObject(obj, scriptable);
		}
		return Scriptable.NOT_FOUND;
	}

	public void put(int index, Scriptable start, Object value) {
		if (javaObject != null) {
			if (value instanceof Wrapper)
				value = ((Wrapper) value).unwrap();
			((Map) javaObject).put(Integer.toString(index), value);
		}
	}

	public boolean has(String name, Scriptable start) {
		return super.has(name, start) || javaObject != null
			&& ((Map) javaObject).get(name) != null;
	}

	public Object get(String name, Scriptable scriptable) {
		Object obj = super.get(name, scriptable);
		if (obj == Scriptable.NOT_FOUND && javaObject != null) {
			obj = ((Map) javaObject).get(name);
			if (obj != null)
				return Context.toObject(obj, scriptable);
			else
				obj = Scriptable.NOT_FOUND;
		}
		return obj;
	}

	public void put(String name, Scriptable start, Object value) {
		if (javaObject != null) {
			if (value instanceof Wrapper)
				value = ((Wrapper) value).unwrap();
			((Map) javaObject).put(name, value);
		}
	}

	public void delete(String name) {
		if (javaObject != null) {
			((Map) javaObject).remove(name);
		}
	}

	public void delete(int index) {
		if (javaObject != null) {
			((Map) javaObject).remove(Integer.toString(index));
		}
	}
}
