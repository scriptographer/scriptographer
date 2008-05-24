/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
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
		if (obj == Scriptable.NOT_FOUND) return null;
		else if (obj instanceof Function) return new RhinoCallable(engine, (Function) obj);
		else if (obj instanceof Wrapper) return ((Wrapper) obj).unwrap();
		else return obj;
	}

	public Object put(String name, Object value, boolean readOnly) {
		Object prev = this.get(name);
		if (scope instanceof ScriptableObject) {
			// Remove READONLY attribute first if the field already existed,
			// to make sure new value can be set
			ScriptableObject scriptable = (ScriptableObject) scope;
			if (scriptable.has(name, scriptable))
				scriptable.setAttributes(name, ScriptableObject.DONTENUM);
			if (readOnly) {
				scriptable.defineProperty(name, value,
						ScriptableObject.READONLY | ScriptableObject.DONTENUM);
				return prev;
			}
		}
		scope.put(name, scope, Context.javaToJS(value, scope));
		return prev;
	}
}
