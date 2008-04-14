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
 * File created on Jun 13, 2007.
 *
 * $Id$
 */

package com.scratchdisk.list;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * AbstractMap is the base for wrapping native objects in a
 * Map interface.
 * 
 * This helps easily implementing entrySet() / keySet()
 * 
 * @author lehni
 */
public abstract class AbstractMap<K,V> implements Map<K,V> {

	/**
	 * This method needs to be defined by extending classes.
	 * It returns all the map-like object's keys in an array.
	 */
	protected abstract K[] keys();

	public int size() {
		return keys().length;
	}

	public boolean isEmpty() {
		return keys().length == 0;
	}

	public void clear() {
		Object[] keys = keys();
		for (int i = 0; i < keys.length; i++)
			remove(keys[i]);
	}

	public Collection<V> values() {
		// Just create an ArrayList containing the values
		Object[] ids = keys();
		ArrayList<V> values = new ArrayList<V>();
		for (int i = 0; i < ids.length; i++) {
			values.add(get(ids[i]));
		}
		return values;
	}

	public boolean containsKey(Object key) {
		Object[] keys = keys();
		// Search for it the slow way...
		for (int i = 0; i < keys.length; i++) {
			if (get(keys[i]).equals(key))
				return true;
		}
		return false;
	}

	public boolean containsValue(Object value) {
		Object[] keys = keys();
		// Search for it the slow way...
		for (int i = 0; i < keys.length; i++) {
			Object obj = get(keys[i]);
			if (value == obj || value != null && value.equals(obj))
				return true;
		}
		return false;
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return new MapSet<Map.Entry<K, V>>(true);
	}

	public Set<K> keySet() {
		return new MapSet<K>(false);
	}

	private class Entry implements Map.Entry<K,V> {
		private K key;

		Entry(K key) {
			this.key = key;
		}

		public K getKey() {
			return key;
		}

		public V getValue() {
			return AbstractMap.this.get(key);
		}

		public V setValue(V value) {
			return AbstractMap.this.put(key, value);
		}
	}

	private class MapSet<E> extends AbstractSet<E> {
		K[] keys;
		boolean entries;

		MapSet(boolean entries) {
			this.keys = AbstractMap.this.keys();
			this.entries = entries;
		}

		public Iterator<E> iterator() {
			return new Iterator<E>() {
				int index = 0;

				public boolean hasNext() {
					return index < keys.length;
				}

				@SuppressWarnings("unchecked")
				public E next() {
					K key = keys[index++];
					return (E) (entries ? new Entry(key) : key);
				}

				public void remove() {
					// TODO: is incrementing correct here?
					AbstractMap.this.remove(keys[index++]);
				}
			};
		}

		public int size() {
			return keys.length;
		}
	}
}
