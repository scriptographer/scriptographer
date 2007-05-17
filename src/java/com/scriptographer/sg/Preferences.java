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
 * $Id: $
 */

package com.scriptographer.sg;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;

/**
 * Preferences wraps a java.util.prefs.Preferences instance in a
 * Map interface.
 * All methods are implemented, even entrySet() / keySet()
 * 
 * @author lehni
 */
public class Preferences implements Map {

	java.util.prefs.Preferences prefs;

	/**
	 * @jshide
	 */
	public Preferences(java.util.prefs.Preferences prefs) {
		this.prefs = prefs;
	}

	public void clear() {
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public int size() {
		try {
			return prefs.keys().length;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isEmpty() {
		try {
			return prefs.keys().length == 0;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public Object get(Object key) {
		String value = prefs.get(key.toString(), null);
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
		// If nothing of that works, return string
		return value;
	}

	public boolean containsKey(Object key) {
		return prefs.get(key.toString(), null) != null;
	}

	public boolean containsValue(Object value) {
		try {
			String[] keys = prefs.keys();
			for (int i = 0; i < keys.length; i++) {
				Object val = prefs.get(keys[i], null);
				if (value != null && value.equals(val) || value == val)
					return true;
			}
			return false;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public Object put(Object key, Object value) {
		Object prev = get(key);
		if (value instanceof Boolean)
			prefs.putBoolean(key.toString(), ((Boolean) value).booleanValue());
		else if (value instanceof Double || value instanceof Float)
			prefs.putDouble(key.toString(), ((Double) value).doubleValue());
		else if (value instanceof Number)
			prefs.putLong(key.toString(), ((Number) value).longValue());
		else // TODO: serialization to byte array?!
			prefs.put(key.toString(), value  != null ? value.toString() : null);
		return prev;
	}

	public Object remove(Object key) {
		Object pre = get(key);
		prefs.remove(key.toString());
		return pre;
	}

	public void putAll(Map map) {
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	public Collection values() {
		// Just create an ArrayList containing the values
		try {
			String[] keys = prefs.keys();
			ArrayList values = new ArrayList();
			for (int i = 0; i < keys.length; i++) {
				values.add(get(keys[i]));
			}
			return values;
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public Set entrySet() {
		return new MapSet(true);
	}

	public Set keySet() {
		return new MapSet(false);
	}

	private class Entry implements Map.Entry {
		private Object key;

		Entry(Object key) {
			this.key = key;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return get(key);
		}

		public Object setValue(Object value) {
			return Preferences.this.put(key, value);
		}
	}

	private class MapSet extends AbstractSet {
		String[] keys;
		boolean entries;

		MapSet(boolean entries) {
			try {
				this.keys = prefs.keys();
				this.entries = entries;
			} catch (BackingStoreException e) {
				throw new RuntimeException(e);
			}
		}

		public Iterator iterator() {
			return new Iterator() {
				int index = 0;

				public boolean hasNext() {
					return index < keys.length;
				}

				public Object next() {
					Object key = keys[index++];
					return entries ? new Entry(key) : key;
				}

				public void remove() {
					// TODO: is incrementing correct here?
					Preferences.this.remove(keys[index++]);
				}
			};
		}

		public int size() {
			return keys.length;
		}
	}
}
