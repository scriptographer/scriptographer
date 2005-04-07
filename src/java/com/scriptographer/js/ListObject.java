/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.02.2005.
 *
 * $RCSfile: ListObject.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.js;

import com.scriptographer.util.*;
import org.mozilla.javascript.*;

/**
 * Wrapper class for com.scriptographer.util.List objects
 * It adds array-like properties, so it is possible to access lists like this: list[i]
 * It also defines getIds(), so enumeration is possible too: for (var i in list) ...
 */
public class ListObject extends NativeJavaObject {
	private List list = null;

	public ListObject() {
	}

	public ListObject(Scriptable scope, List list, Class staticType) {
		super(scope, list, staticType);
		this.list = list;
	}

	public Object unwrap() {
		return list;
	}

	public String getClassName() {
		return "ListObject";
	}

	public Object[] getIds() {
		if (list != null) {
			// act like a JS list:
			Integer[] ids = new Integer[list.size()];
			for (int i = 0; i < ids.length; i++) {
				ids[i] = new Integer(i);
			}
			return ids;
		} else {
			return new Object[]{};
		}
	}

	public boolean has(int index, Scriptable start) {
		return list != null && index < list.size();
	}

	public Object get(int index, Scriptable scriptable) {
		if (list != null) {
			Object obj = list.get(index);
			return obj != null ? obj : Scriptable.NOT_FOUND;
		} else {
			return Scriptable.NOT_FOUND;
		}
	}

	public boolean has(String name, Scriptable start) {
		return super.has(name, start) ||
			name.equals("length") ||
			this instanceof StringIndexList && list != null && ((StringIndexList) list).get(name) != null;
	}

	public Object get(String name, Scriptable scriptable) {
		Object obj = super.get(name, scriptable);
		if (obj == Scriptable.NOT_FOUND && list != null) {
			if (name.equals("length")) {
				return new Integer(list.size());
			} else if (this instanceof StringIndexList) {
				obj = ((StringIndexList) list).get(name);
				if (obj == null)
					obj = Scriptable.NOT_FOUND;
			}
		}
		return obj;
	}

	public void put(int index, Scriptable start, Object value) {
		if (list != null) {
			int size = list.size();
			if (index > size) {
				for (int i = size; i < index; i++)
					list.add(i, null);
				list.add(index, value);
			} else {
				list.set(index, value);
			}
		}
	}
}

