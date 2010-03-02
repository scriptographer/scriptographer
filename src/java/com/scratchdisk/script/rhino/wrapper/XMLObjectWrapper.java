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
import org.mozilla.javascript.NativeWith;
import org.mozilla.javascript.Ref;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLObject;

import sun.org.mozilla.javascript.internal.Undefined;

/**
 * @author lehni
 * 
 */
public class XMLObjectWrapper extends XMLObject implements Wrapper, Wrapped {
	private XMLObject object;
	private Callable onChange;

	XMLObjectWrapper(XMLObject object, Callable onChange) {
		this.object = object;
		this.onChange = onChange;
	}

	public String getClassName() {
		return object.getClassName();
	}

	public Object[] getIds() {
		return object.getIds();
	}

	public Scriptable getPrototype() {
		return object.getPrototype();
	}

	public void setPrototype(Scriptable prototype) {
		object.setPrototype(prototype);
	}

	public Scriptable getParentScope() {
		return object.getParentScope();
	}

	public Object getDefaultValue(Class hint) {
		return object.getDefaultValue(hint);
	}

	public boolean hasInstance(Scriptable instance) {
		return object.hasInstance(instance);
	}

	public String toString() {
		return object.toString();
	}

	public int hashCode() {
		return object.hashCode();
	}

	public Object unwrap() {
		return object;
	}

	public Object ecmaGet(Context cx, Object id) {
		// Wrap sub elements again
		return ObjectWrapper.wrap(object.ecmaGet(cx, id), onChange);
	}

	public void ecmaPut(Context cx, Object id, Object value) {
		// Wrap values that are put in too as they might be changed after
		object.ecmaPut(cx, id,
				ObjectWrapper.wrap(value, onChange));
		onChange(id.toString(), value);
	}

	public boolean ecmaHas(Context cx, Object id) {
		return object.ecmaHas(cx, id);
	}

	public boolean ecmaDelete(Context cx, Object id) {
		boolean ret = object.ecmaDelete(cx, id);
		if (ret)
			onChange(id.toString(), Undefined.instance);
		return ret;
	}

	public NativeWith enterDotQuery(Scriptable scope) {
		return object.enterDotQuery(scope);
	}

	public NativeWith enterWith(Scriptable scope) {
		return object.enterWith(scope);
	}

	public Scriptable getExtraMethodSource(Context cx) {
		return object.getExtraMethodSource(cx);
	}

	public Ref memberRef(Context cx, Object elem, int memberTypeFlags) {
		return object.memberRef(cx, elem, memberTypeFlags);
	}

	public Ref memberRef(Context cx, Object namespace, Object elem,
			int memberTypeFlags) {
		return object.memberRef(cx, namespace, elem, memberTypeFlags);
	}

	private void onChange(String name, Object value) {
		if (onChange != null) {
			Context cx = Context.getCurrentContext();
			Scriptable scope = ScriptableObject.getTopLevelScope(this);
			onChange.call(cx, scope, this, new Object[] { name, value });
		}
	}
}
