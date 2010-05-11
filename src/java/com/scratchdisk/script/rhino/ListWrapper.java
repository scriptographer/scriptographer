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
 * File created on 11.02.2005.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.list.List;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;
import com.scratchdisk.list.StringIndexList;
import com.scratchdisk.script.ChangeReceiver;

/**
 * Wrapper class for com.scriptographer.util.List objects to make them behave
 * like NativeArray. The prototype is set to the js Array prototype, so all
 * Array functions can be used on list objects.
 * 
 * Unlike MapWrapper, this is still an ExtendedJavaObject at the same time, so
 * native methods get passed through too. The reasoning behind this is that in
 * Map<->Object wrapping, such methods could interfere with the expected
 * behavior of a native Object (e.g. script.preferences.values returning the
 * values() method from Map), whereas on lists, such clashes are not going to
 * happen, and right now list classes still often define other functionality in
 * Scriptographer, e.g. HierarchyList in the UI framework.
 * 
 * TODO: In the future, this should be handled in the same way as MapAdapter,
 * making the list behave like a 100% native array.
 * 
 * It adds array-like properties, so it is possible to access lists like this:
 * list[i] It also defines getIds(), so enumeration is possible too: for (var i
 * in list) ...
 * 
 * @author lehni
 */
public class ListWrapper extends ExtendedJavaObject {
	public ListWrapper(Scriptable scope, ReadOnlyList list,
			Class staticType, boolean unsealed) {
		super(scope, list, staticType, unsealed);
		// Make ListWrappers behave exactly like arrays and inherit all features
		// from them.
		// Thanks to the great functionality of our ExtendedJavaObject, there is
		// no difference between a java List and a JavaScript Array after this,
		// all features work as long as the list is not read-only!
		setPrototype(ScriptableObject.getArrayPrototype(scope));
	}

	public Object[] getIds() {
		if (javaObject != null) {
			// act like a JS javaObject:
			Integer[] ids = new Integer[((ReadOnlyList) javaObject).size()];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = new Integer(i);
			}
			return ids;
		} else {
			return new Object[] {};
		}
	}

	public boolean has(int index, Scriptable start) {
		return javaObject != null && index < ((ReadOnlyList) javaObject).size();
	}

	@SuppressWarnings("unchecked")
	public void put(int index, Scriptable start, Object value) {
		if (javaObject != null && javaObject instanceof List) {
			List list = ((List) javaObject);
			int size = list.size();
			value = coerceComponentType(value);
			if (index >= size) {
				for (int i = size; i < index; i++)
					list.add(i, null);
				list.add(index, value);
			} else {
				list.set(index, value);
			}
			if (changeReceiver != null)
				updateChangeReceiver();
		}
	}

	public Object get(int index, Scriptable start) {
		if (javaObject != null) {
			if (changeReceiver != null)
				fetchChangeReceiver();
			try {
				Object obj = ((ReadOnlyList) javaObject).get(index);
				if (obj != null) {
					Object result = Context.javaToJS(obj, getParentScope());
					if (javaObject instanceof ChangeReceiver)
						handleChangeEmitter(result, Integer.toString(index));
				}
			} catch (IndexOutOfBoundsException e) {
				// Don't report
			}
		}
		return Scriptable.NOT_FOUND;
	}

	public boolean has(String name, Scriptable start) {
		return super.has(name, start) || name.equals("length") ||
			javaObject instanceof ReadOnlyStringIndexList && javaObject != null
			&& ((ReadOnlyStringIndexList) javaObject).get(name) != null;
	}

	@SuppressWarnings("unchecked")
	public void put(String name, Scriptable start, Object value) {
		// Since lists have the native size method that's already accessible
		// through "length", offer access here to get/setSize if these are
		// present, such as in Scriptographer's HierarchyList, where they
		// get / set the item's dimensions.
		if (name.equals("size") && members.has("setSize", false)) {
			Object obj = members.get(this, "setSize", javaObject, false);
			if (obj instanceof Callable) {
				((Callable) obj).call(Context.getCurrentContext(),
						start.getParentScope(), this, new Object[] { value });
				return;
			}
		} else if (javaObject instanceof StringIndexList
				&& !members.has(name, false)) {
			((StringIndexList) javaObject).put(name, coerceComponentType(value));
			return;
		}
		super.put(name, start, value);
	}

	public Object get(String name, Scriptable start) {
		// Again, allow access to getSize, if it's there. See #put
		if (name.equals("size") && members.has("getSize", false)) {
			Object obj = members.get(this, "getSize", javaObject, false);
			if (obj instanceof Callable)
				return ((Callable) obj).call(Context.getCurrentContext(),
						start.getParentScope(), this, new Object[] {});
		}
		if (javaObject != null) {
			if (name.equals("length")) {
				return new Integer(((ReadOnlyList) javaObject).size());
			} else if (javaObject instanceof ReadOnlyStringIndexList
					&& !members.has(name, false)) {
				// Only check ReadOnlyStringIndexList if members does not
				// define the same property
				Object obj = ((ReadOnlyStringIndexList) javaObject).get(name);
				if (obj != null)
					return Context.javaToJS(obj, getParentScope());
			}
		}
		return super.get(name, start);
	}

	public Object getDefaultValue(Class hint) {
		if (hint == null || hint == ScriptRuntime.StringClass) {
			StringBuffer buffer = new StringBuffer();
			ReadOnlyList list = (ReadOnlyList) javaObject;
			for (int i = 0, l = list.size(); i < l; i++) {
				if (i > 0)
					buffer.append(", ");
				Object entry = list.get(i);
				if (entry != null) {
	                Scriptable obj = Context.toObject(entry, this);
	                buffer.append(obj.getDefaultValue(hint));
				} else {
				    buffer.append("null");
				}
			}
			return buffer.toString();
		} else {
			return super.getDefaultValue(hint);
		}
	}

	private Object coerceComponentType(Object value) {
		Object unwrapped = value;
		if (unwrapped instanceof Wrapper)
			unwrapped = ((Wrapper) unwrapped).unwrap();
		Class type = ((List) javaObject).getComponentType();
		// Use WrapFactory to coerce type if not compatible
		return type.isInstance(value)
				? value
				: Context.getCurrentContext().getWrapFactory().coerceType(
						type, value, unwrapped);
	}
}
