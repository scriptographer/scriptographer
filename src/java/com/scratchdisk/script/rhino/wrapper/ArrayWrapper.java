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
 * File created on Mar 1, 2010.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino.wrapper;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

import sun.org.mozilla.javascript.internal.Undefined;

/**
 * @author lehni
 *
 */
public class ArrayWrapper extends NativeArray implements Wrapper, Wrapped  {

	private NativeArray array;
	private Callable onChange;

	public ArrayWrapper(NativeArray object, Callable onChange) {
		super(0);
		this.array = object;
		this.onChange = onChange;
	}

	public String getClassName() {
		return array.getClassName();
	}

	public Object[] getIds() {
		return array.getIds();
	}

	public Scriptable getPrototype() {
		return array.getPrototype();
	}

	public void setPrototype(Scriptable prototype) {
		array.setPrototype(prototype);
	}

	public Scriptable getParentScope() {
		return array.getParentScope();
	}

	public Object getDefaultValue(Class hint) {
		return array.getDefaultValue(hint);
	}

	public boolean hasInstance(Scriptable instance) {
		return array.hasInstance(instance);
	}

	public String toString() {
		return array.toString();
	}

	public int hashCode() {
		return array.hashCode();
	}

	public Object unwrap() {
		return array;
	}

	public Object get(String name, Scriptable start) {
		// Wrap sub elements again
		return ObjectWrapper.wrap(
				ScriptableObject.getProperty(array, name), onChange);
	}

	public Object get(int index, Scriptable start) {
		// Wrap sub elements again
		return ObjectWrapper.wrap(
				ScriptableObject.getProperty(array, index), onChange);
	}

	public void put(String name, Scriptable start, Object value) {
		// Wrap values that are put in too as they might be changed after
		ScriptableObject.putProperty(array, name,
				ObjectWrapper.wrap(value, onChange));
		onChange(name, value);
	}

	public void put(int index, Scriptable start, Object value) {
		// Wrap values that are put in too as they might be changed after
		ScriptableObject.putProperty(array, index,
				ObjectWrapper.wrap(value, onChange));
		onChange(Integer.toString(index), value);
	}

	public boolean has(String name, Scriptable start) {
		return ScriptableObject.hasProperty(array, name);
	}

	public boolean has(int index, Scriptable start) {
		return ScriptableObject.hasProperty(array, index);
	}

	public void delete(String name) {
		if (ScriptableObject.deleteProperty(array, name))
			onChange(name, Undefined.instance);
	}

	public void delete(int index) {
		if (ScriptableObject.deleteProperty(array, index))
			onChange(Integer.toString(index), Undefined.instance);
	}

	private void onChange(String name, Object value) {
		if (onChange != null) {
			Context cx = Context.getCurrentContext();
			Scriptable scope = ScriptableObject.getTopLevelScope(this);
			onChange.call(cx, scope, this, new Object[] { name, value });
		}
	}
}
