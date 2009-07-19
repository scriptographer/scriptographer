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
 * File created on Jul 19, 2009.
 *
 * $Id$
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.JavaMembers;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 * A class that wraps a JavaMembers object in a prototype object and allow extension of it through js members.
 * 
 * @author lehni
 */
public class ExtendedJavaPrototype extends NativeObject {
	private JavaMembers members;

	ExtendedJavaPrototype(JavaMembers members) {
		this.members = members;
	}
	
	public Object[] getIds() {
		Object[] javaIds = members.getIds(false);
		Object[] ids = super.getIds();
		Object[] result = new Object[javaIds.length + ids.length];
		System.arraycopy(javaIds, 0, result, 0, javaIds.length);
		System.arraycopy(ids, 0, result, javaIds.length, ids.length);
		return result;
	}

	public boolean has(String name, Scriptable start) {
		return super.has(name, start)
				|| members.has(name, false)
				// TODO: Decide if static fields should be hidden completely from instances, just like in JS.
				|| members.has(name, true);
	}

	public Object get(String name, Scriptable start) {
		// Try the prototype first to see if things override native methods.
		// If nothing is returns, go through members.
		Object result = super.get(name, start);
		if (result == Scriptable.NOT_FOUND) {
			// Get the java object. Not in all cases start is actually this object, 
			// but let's just unwrap it if it's a wrapper and use that. If it fails,
			// we are e.g. getting values directly from the prototype object, in which
			// case the convention is to just return undefined instead of throwing
			// an error.
			Object obj = start instanceof Wrapper
					? ((Wrapper) start).unwrap()
					: start;
			try {
				// This might throw an exception since obj is potentially not
				// of the underlying class. Just catch it and return undefined.
				result = members.get(start, name, obj, false);
			} catch (RuntimeException e) {
				result = Undefined.instance;
			}
		}
		return result;
	}

    public void delete(String name) {
    	// TODO: throw exception when trying to delete native members.
    	super.delete(name);
    }
}
