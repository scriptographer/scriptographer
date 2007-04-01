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
 * File created on Mar 30, 2007.
 *
 * $Id: $
 */

package com.scriptographer.script.rhino;

import java.util.HashMap;

import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

/**
 * @author lehni
 *
 */
public class ExtendedJavaArray extends NativeJavaArray {
	HashMap properties;

	public ExtendedJavaArray(Scriptable scope, Object array, Class staticType, boolean unsealed) {
		super(scope, array);
		properties = unsealed ? new HashMap() : null;
	}


	public void delete(String name) {
		if (properties != null)
			properties.remove(name);
	}
	
	public Object get(String name, Scriptable start) {
		Object obj;
		if (super.has(name, start)) {
			obj = super.get(name, start);
		} else if (properties != null && properties.containsKey(name)) {
			// see wether this object defines the property.
			obj = properties.get(name);
		} else {
			Scriptable prototype = this.getPrototype();
			if (name.equals("prototype")) {
				if (prototype == null) {
					// If no prototype object was created it, produce it on the fly.
					prototype = new NativeObject();
					this.setPrototype(prototype);
				}
				obj = prototype;
			} else if (prototype != null) {
				// if not, see wether the prototype maybe defines it.
				// NativeJavaObject misses to do so:
				obj = prototype.get(name, start);
			} else {
				obj = Scriptable.NOT_FOUND;
			}
		}
		return obj;
	}
	
	public void put(String name, Scriptable start, Object value) {
		if (super.has(name, start)) {
			super.put(name, start, value);
		} else if (name.equals("prototype")) {
			if (value instanceof Scriptable)
				this.setPrototype((Scriptable) value);
		} else if (properties != null) {
			properties.put(name, value);
		}
	}
	
	public boolean has(String name, Scriptable start) {
		boolean has = super.has(name, start);
		if (!has && properties != null)
			has = properties.get(name) != null;
		return has;
	}
}
