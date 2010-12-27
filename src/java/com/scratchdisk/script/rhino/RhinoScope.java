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
 * File created on Apr 10, 2007.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import com.scratchdisk.script.Scope;

/**
 * @author lehni
 *
 */
public class RhinoScope extends Scope {
	private Scriptable scope;
	private RhinoEngine engine;

	public RhinoScope(RhinoEngine engine, Scriptable scope) {
		this.scope = scope;
		this.engine = engine;
	}

	public Scriptable getScope() {
		return scope;
	}

	public Object get(String name) {
		Object obj = scope.get(name, scope); 
		if (obj == Scriptable.NOT_FOUND) {
			return null;
		} else if (obj instanceof Function) {
			return new RhinoCallable(engine, (Function) obj);
		} else if (obj instanceof Wrapper) {
			return ((Wrapper) obj).unwrap();
		} else {
			return obj;
		}
	}

	public Object put(String name, Object value, boolean readOnly) {
		Object prev = get(name);
		value = Context.javaToJS(value, scope);
		if (scope instanceof ScriptableObject) {
			// Remove READONLY attribute first if the field already existed,
			// to make sure new value can be set
			ScriptableObject scriptable = (ScriptableObject) scope;
			if (scriptable.has(name, scriptable))
				scriptable.setAttributes(name, ScriptableObject.EMPTY);
			if (readOnly) {
				scriptable.defineProperty(name, value,
						ScriptableObject.READONLY);
				return prev;
			}
		}
		scope.put(name, scope, value);
		return prev;
	}

	public Object[] getKeys() {
		return scope.getIds();
	}
}
