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
 * File created on May 6, 2007.
 *
 * $Id$
 */

package com.scriptographer.sg;

import java.util.prefs.BackingStoreException;

import com.scratchdisk.list.AbstractMap;

/**
 * Preferences wraps a java.util.prefs.Preferences instance in a
 * Map interface.
 * 
 * @author lehni
 */
public class Preferences extends AbstractMap {

	java.util.prefs.Preferences prefs;

	/**
	 * @jshide
	 */
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
		// Try converting to a long, if there is no decimal point
		if (value != null) {
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
			if (value.equals("true")) return Boolean.TRUE;
			else if (value.equals("false")) return Boolean.FALSE;
			else if (value.equals("null")) return null;
		}
		// If nothing of that works, return string
		return value;
	}

	public boolean containsKey(Object key) {
		return prefs.get(key.toString(), null) != null;
	}

	public Object put(Object key, Object value) {
		Object prev = get(key);
		if (value instanceof Boolean)
			prefs.putBoolean(key.toString(), ((Boolean) value).booleanValue());
		else if (value instanceof Double || value instanceof Float)
			prefs.putDouble(key.toString(), ((Number) value).doubleValue());
		else if (value instanceof Number)
			prefs.putLong(key.toString(), ((Number) value).longValue());
		else // TODO: serialization to byte array?!
			prefs.put(key.toString(), value  != null ? value.toString() : "null");
		return prev;
	}

	public Object remove(Object key) {
		Object pre = get(key);
		prefs.remove(key.toString());
		return pre;
	}
}
