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
 * File created on May 6, 2007.
 */

package com.scriptographer.sg;

import java.util.prefs.BackingStoreException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJSON;
import org.mozilla.javascript.Scriptable;

import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.rhino.RhinoScope;
import com.scratchdisk.util.AbstractMap;

/**
 * Preferences wraps a java.util.prefs.Preferences instance in a
 * Map interface.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class Preferences extends AbstractMap {

	java.util.prefs.Preferences prefs;

	public Preferences(java.util.prefs.Preferences prefs) {
		this.prefs = prefs;
	}

	protected Object[] keys() {
		try {
			return prefs.keys();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public void clear() {
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public Object get(Object key) {
		String value = prefs.get(key.toString(), null);
		if (value != null) {
			// Try conversion to numbers first, if it is not obiously Json data
			if (!value.startsWith("{") && !value.startsWith("\"")) {
				// Try converting to a long, if there is no decimal point
				try {
					if (value.indexOf('.') == -1)
						return new Long(value);
				} catch (NumberFormatException e) {
				}
				// Now try double
				try {
					return new Double(value);
				} catch (NumberFormatException e) {
				}
			}
			if (value.equals("true")) {
				return Boolean.TRUE;
			} else if (value.equals("false")) {
				return Boolean.FALSE;
			} else if (value.equals("null")) {
				return null;
			} else { 
				// Now try Json.
				// TODO: Note that this only makes sense for Rhino engine! Is
				// there ever another engine, this won't work. This is also the
				// reason why the above code is kept although all code be handled
				// by Json...
				// Another solution could be to force conversion to one of the Java
				// types and pass the result to a Java Json engine. The conversion
				// could be enforced through various conversion methods with different
				// argument types. But this will do for now, at least as long as there
				// are no other engines.
				try {
					ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
					Context cx = Context.getCurrentContext();
					Object json = NativeJSON.parse(cx,
							((RhinoScope) engine.getGlobalScope()).getScope(),
							value.toString());
					if (json != null)
						return json;
				} catch (Exception e) {
				}
			}
		}
		// If nothing of that works, return string
		return value;
	}

	public boolean containsKey(Object key) {
		return prefs.get(key.toString(), null) != null;
	}

	public Object put(Object key, Object value) {
		Object prev = get(key);
		String keyStr = key.toString();
		if (value instanceof Boolean) {
			prefs.putBoolean(keyStr, ((Boolean) value).booleanValue());
		} else if (value instanceof Double || value instanceof Float) {
			prefs.putDouble(keyStr, ((Number) value).doubleValue());
		} else if (value instanceof Number) {
			prefs.putLong(keyStr, ((Number) value).longValue());
		} else if (value instanceof Scriptable) {
			ScriptEngine engine = ScriptEngine.getEngineByName("JavaScript");
			Context cx = Context.getCurrentContext();
			Object json = NativeJSON.stringify(cx,
					((RhinoScope) engine.getGlobalScope()).getScope(),
					(Scriptable) value, null, null);
			prefs.put(keyStr, json  != null ? json.toString() : "null");
		} else if (value instanceof String) {
			prefs.put(keyStr, value.toString());
		}
		return prev;
	}

	public Object remove(Object key) {
		Object pre = get(key);
		prefs.remove(key.toString());
		return pre;
	}
}
