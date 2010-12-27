/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on 19.02.2005.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
	
import java.util.Map;

/**
 * Wrapper class for java.util.Map objects It adds JS object-like properties, so
 * it is possible to access lists like this: map["value"], or map[10].
 * 
 * It also defines getIds(), so enumeration is possible too: for (var i in list)
 * ... But it does not allow to access by anything else than Integer or String,
 * so not the whole functionality Of maps is provided. For Scriptographer it is
 * enough, though.
 * 
 * @author lehni
 */
public class MapWrapper extends ScriptableObject implements Wrapper {
	private Map map;

	public MapWrapper(Scriptable scope, Map map) {
		super(scope, ScriptableObject.getObjectPrototype(scope));
		this.map = map;
	}

	public boolean has(String name, Scriptable start) {
		if (map != null) {
			return map.containsKey(name);
		} else {
			return super.has(name, start);
		}
	}

	public boolean has(int index, Scriptable start) {
		if (map != null) {
			return map.containsKey(Integer.toString(index));
		} else {
			return super.has(index, start);
		}
	}

	public Object get(String name, Scriptable start) {
		// Retrieve from map first, then from super, to give entries priority
		// over methods and fields.
		if (map != null)
			return getInternal(name);
		else
			return super.get(name, start);
	}

	public Object get(int index, Scriptable start) {
		// Retrieve from map first, then from super, to give entries priority over methods and fields.
		if (map != null)
			return getInternal(Integer.toString(index));
		else
			return super.get(index, start);
	}

	private Object getInternal(Object key) {
		if (map.containsKey(key)) {
			Object value = map.get(key);
			return value != null ? Context.javaToJS(value, getParentScope()) : null;
		}
		return Scriptable.NOT_FOUND;
	}

	public void put(String name, Scriptable start, Object value) {
		if (map != null)
			putInternal(name, value);
		else
			super.put(name, start, value);
	}

	public void put(int index, Scriptable start, Object value) {
		if (map != null)
			putInternal(Integer.toString(index), value);
		else
			super.put(index, start, value);
	}

	@SuppressWarnings("unchecked")
	private void putInternal(Object key, Object value) {
		try {
			map.put(key, Context.jsToJava(value,
					ScriptRuntime.ObjectClass));
		} catch (RuntimeException e) {
			Context.throwAsScriptRuntimeEx(e);
		}
	}

	public void delete(String name) {
		if (map != null) {
			try {
				map.remove(name);
			} catch (RuntimeException e) {
				Context.throwAsScriptRuntimeEx(e);
			}
		} else {
			super.delete(name);
		}
	}

	public void delete(int index) {
		if (map != null) {
			try {
				map.remove(new Integer(index));
			} catch (RuntimeException e) {
				Context.throwAsScriptRuntimeEx(e);
			}
		} else {
			super.delete(index);
		}
	}

	public Object[] getIds() {
		if (map != null) {
			return map.keySet().toArray();
		} else {
			return new Object[0];
		}
	}

	public String getClassName() {
		// It looks like an Object, inherits from it, so why not pretending to
		// be one.
		return "Object";
	}

	public Object unwrap() {
		return map;
	}
}
