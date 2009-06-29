/**
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.scratchdisk.util;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *  Hashtable-based map with integer keys that allows values to be removed 
 *  by the garbage collector.<P>
 *
 *  The algorithms used are basically the same as those
 *  in {@link java.util.HashMap}.  In particular, you 
 *  can specify a load factor and capacity to suit your
 *  needs.
 *
 *  This map does <I>not</I> allow null values.  Attempting to add a null 
 *  value to the map will raise a <Code>NullPointerException</Code>.<P>
 *
 *  This data structure is not synchronized.
 *
 *  @see java.lang.ref.Reference
 */

public class IntMap<V> extends AbstractMap<Integer, V> {

	protected static class Entry<V> implements Map.Entry<Integer, V>{
		protected int key;
		protected Entry<V> next;
		/**
		 * Reference value.  Note this can never be null.
		 */
		protected V value;

		public Entry(int key, V value, Entry<V> next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}

		public V getValue() {
			return value;
		}

		public Integer getKey() {
			return key;
		}

		public V setValue(V value) {
			V prev = this.value;
			this.value = value;
			return prev;
		}
	}

	int entryCount;

	/**
	 *  The threshold variable is calculated by multiplying
	 *  table.length and loadFactor.  
	 *  Note: I originally marked this field as final, but then this class
	 *   didn't compile under JDK1.2.2.
	 *  @serial
	 */
	float loadFactor;

	/**
	 *  Number of mappings in this map.
	 */
	transient int size;

	/**
	 *  The hash table.  Its length is always a power of two.  
	 */
	transient Entry<V>[] table;

	/**
	 *  When size reaches threshold, the map is resized.  
	 *  @see #resize()
	 */
	transient int threshold;

	/**
	 * Number of times this map has been modified.
	 */
	transient volatile int modCount;

	/**
	 * Cached key set. May be null if key set is never accessed.
	 */
	transient Set<Integer> keySet;


    /**
	 * Cached entry set. May be null if entry set is never accessed.
	 */
	transient Set<Map.Entry<Integer, V>> entrySet;

	/**
	 * Cached values. May be null if values() is never accessed.
	 */
	transient Collection<V> values;

    /**
     * Constructs a new <Code>IntMap</Code>
     */
    public IntMap() {
        this(16, 0.75f);
    }

	/**
	 *  Constructs a new <Code>IntMap</Code> with the
	 *  specified load factor and initial capacity.
	 *
	 *  @param capacity  the initial capacity for the map
	 *  @param loadFactor  the load factor for the map
	 */
	@SuppressWarnings("unchecked")
	public IntMap(int capacity, float loadFactor) {
		super();
		if (capacity <= 0)
			throw new IllegalArgumentException("capacity must be positive");
		if ((loadFactor <= 0.0f) || (loadFactor >= 1.0f))
			throw new IllegalArgumentException("Load factor must be greater than 0 and less than 1.");

		int initialSize = 1;
		while (initialSize < capacity)
			initialSize *= 2;

		this.table = new Entry[initialSize];
		this.loadFactor = loadFactor;
		this.threshold = (int) (initialSize * loadFactor);
	}

	/**
	 * @param key
	 */
	protected V doRemove(int key) {
		int index = indexFor(key);
		Entry<V> previous = null;
		Entry<V> entry = table[index];
		while (entry != null) {
			if (key == entry.key) {
				if (previous == null)
					table[index] = entry.next;
				else
					previous.next = entry.next;
				this.size--;
				modCount++;
				return entry.getValue();
			}
			previous = entry;
			entry = entry.next;
		}
		return null;
	}

	/**
	 *  Converts the given hash code into an index into the
	 *  hash table.
	 */
	private int indexFor(int hash) {
		// mix the bits to avoid bucket collisions...
		hash += ~(hash << 15);
		hash ^= (hash >>> 10);
		hash += (hash << 3);
		hash ^= (hash >>> 6);
		hash += ~(hash << 11);
		hash ^= (hash >>> 16);
		return hash & (table.length - 1);
	}

    /**
     *  Returns the entry associated with the given key.
     *
     *  @param key  the key of the entry to look up
     *  @return  the entry associated with that key, or null
     *    if the key is not in this map
     */
	public Entry<V> getEntry(int key) {
		for (Entry<V> entry = table[indexFor(key)]; entry != null; entry = entry.next)
			if (entry.key == key)
				return entry;
		return null;
	}

	protected void purge() {
	}

    /**
	 * Returns the size of this map.
	 * 
	 * @return the size of this map
	 */
	public int size() {
		purge();
		return size;
	}

	/**
	 * Returns <Code>true</Code> if this map is empty.
	 * 
	 * @return <Code>true</Code> if this map is empty
	 */
	public boolean isEmpty() {
		purge();
		return size == 0;
	}
	
    /**
     *  Returns <Code>true</Code> if this map contains the given key.
     *
     *  @return true if the given key is in this map
     */
    public boolean containsKey(int key) {
        purge();
        Entry entry = getEntry(key);
        if (entry == null) return false;
        return entry.getValue() != null;
    }

	/**
	 *  Returns the value associated with the given key, if any.
	 *
	 *  @return the value associated with the given key, or <Code>null</Code>
	 *   if the key maps to no value
	 */
	public V get(int key) {
        purge();
        Entry<V> entry = getEntry(key);
        if (entry == null) return null;
        return entry.getValue();
	}

	/**
	 * java.util.Map compatible version of get
	 */
	public V get(Integer key) {
		return get(key.intValue());
	}

	/**
	 * Constructs a new table entry for the given data
	 * 
	 * @param key The entry key
	 * @param value The entry value
	 * @param next The next value in the entry's collision chain
	 * @return The new table entry
	 */
	protected Entry<V> createEntry(int key, V value, Entry<V> next) {
		return new Entry<V>(key, value, next);
	}
	
	/**
	 * Associates the given key with the given value.
	 * <P>
	 * Neither the key nor the value may be null.
	 * 
	 * @param key the key of the mapping
	 * @param value the value of the mapping
	 * @throws NullPointerException if either the key or value is null
	 */
	public V put(int key, V value) {
		if (value == null)
			throw new NullPointerException("null values not allowed");

		purge();

		if (size + 1 > threshold)
			resize();

		int index = indexFor(key);
		Entry<V> previous = null;
		Entry<V> entry = table[index];
		while (entry != null) {
			if (key == entry.key) {
				V result = entry.getValue();
				if (previous == null)
					table[index] = createEntry(key, value, entry.next);
				else
					previous.next = createEntry(key, value, entry.next);
				return result;
			}
			previous = entry;
			entry = entry.next;
		}
		this.size++;
		modCount++;
		table[index] = createEntry(key, value, table[index]);
		entryCount++;
		return null;
	}

	public V put(Integer key, V value) {
		return put(key.intValue(), value);
	}

	/**
	 * Removes the key and its associated value from this map.
	 * 
	 * @param key the key to remove
	 * @return the value associated with that key, or null if the key was not in
	 * the map
	 */
	public V remove(int key) {
		purge();
		return doRemove(key);
	}
	
	public V remove(Integer key) {
		return remove(key.intValue());
	}

    /**
	 * Clears this map.
	 */
	public void clear() {
		Arrays.fill(table, null);
		size = 0;
	}

	/**
	 *  Resizes this hash table by doubling its capacity.
	 *  This is an expensive operation, as entries must
	 *  be copied from the old smaller table to the new 
	 *  bigger table.
	 */
	@SuppressWarnings("unchecked")
	private void resize() {
		Entry<V>[] old = table;
		table = new Entry[old.length * 2];

		for (int i = 0; i < old.length; i++) {
			Entry<V> next = old[i];
			while (next != null) {
				Entry<V> entry = next;
				next = next.next;
				int index = indexFor(entry.key);
				entry.next = table[index];
				table[index] = entry;
			}
			old[i] = null;
		}
		threshold = (int) (table.length * loadFactor);
	}

    /**
	 * Returns a set view of this map's entries.
	 * 
	 * @return a set view of this map's entries
	 */
	public Set<Map.Entry<Integer, V>> entrySet() {
		if (entrySet != null)
			return entrySet;
		entrySet = new AbstractSet<Map.Entry<Integer, V>>() {
			public int size() {
				return IntMap.this.size();
			}

			public void clear() {
				IntMap.this.clear();
			}

			public boolean contains(Object o) {
				if (o != null && o instanceof Map.Entry) {
					Map.Entry e = (Map.Entry) o;
					Object k = e.getKey();
					if (k instanceof Number) {
						Entry e2 = getEntry(((Number) k).intValue());
						return (e2 != null) && e.equals(e2);
					}
				}
				return false;
			}

			public boolean remove(Object o) {
				boolean r = contains(o);
				if (r) {
					Map.Entry e = (Map.Entry) o;
					Object k = e.getKey();
					if (k instanceof Number)
						IntMap.this.remove(((Number) k).intValue());
					else
						r = false;
				}
				return r;
			}

			public Iterator<Map.Entry<Integer, V>> iterator() {
				return new EntryIterator();
			}

			public Object[] toArray() {
				return toArray(new Object[0]);
			}
		};
		return entrySet;
	}

	/**
	 *  Returns a set view of this map's keys.
	 *
	 *  @return a set view of this map's keys
	 */
	public Set<Integer> keySet() {
		if (keySet != null)
			return keySet;
		keySet = new AbstractSet<Integer>() {
			public int size() {
				return size;
			}

			public Iterator<Integer> iterator() {
				return new KeyIterator();
			}

			public boolean contains(Object o) {
				return o instanceof Number ? containsKey(((Number) o).intValue()) : false;
			}

			public boolean remove(Object o) {
				return o instanceof Number ? IntMap.this.remove(((Number) o).intValue()) != null : false;
			}

			public void clear() {
				IntMap.this.clear();
			}

		};
		return keySet;
	}

	/**
	 *  Returns a collection view of this map's values.
	 *
	 *  @return a collection view of this map's values.
	 */
	public Collection<V> values() {
		if (values != null)
			return values;
		values = new AbstractCollection<V>() {
			public int size() {
				return size;
			}

			public void clear() {
				IntMap.this.clear();
			}

			public Iterator<V> iterator() {
				return new ValueIterator();
			}
		};
		return values;
	}

	private abstract class AbstractIterator<E> implements Iterator<E> {
		// These fields keep track of where we are in the table.
		int index;
		Entry<V> entry;
		Entry<V> previous;

		// These Object fields provide hard references to the
		// current and next entry; this assures that if hasNext()
		// returns true, next() will actually return a valid element.
		int currentKey, nextKey;
		Object nextValue;

		int expectedModCount;

		public AbstractIterator() {
			index = (size() != 0 ? table.length : 0);
			// have to do this here!  size() invocation above
			// may have altered the modCount.
			expectedModCount = modCount;
		}

		public boolean hasNext() {
			checkMod();
			while (nextNull()) {
				Entry<V> e = entry;
				int i = index;
				while ((e == null) && (i > 0)) {
					i--;
					e = table[i];
				}
				entry = e;
				index = i;
				if (e == null) {
					currentKey = -1;
					return false;
				}
				nextKey = e.key;
				nextValue = e.getValue();
				if (nextNull())
					entry = entry.next;
			}
			return true;
		}

		private void checkMod() {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}

		private boolean nextNull() {
			return (nextKey == -1) || (nextValue == null);
		}

		protected Entry<V> nextEntry() {
			checkMod();
			if (nextNull() && !hasNext())
				throw new NoSuchElementException();
			previous = entry;
			entry = entry.next;
			currentKey = nextKey;
			nextKey = -1;
			nextValue = null;
			return previous;
		}

		public void remove() {
			checkMod();
			if (previous == null)
				throw new IllegalStateException();
			IntMap.this.remove(currentKey);
			previous = null;
			currentKey = -1;
			expectedModCount = modCount;
		}
	}

	private class EntryIterator extends AbstractIterator<Map.Entry<Integer, V>> {
		public Map.Entry<Integer, V> next() {
			return nextEntry();
		}
	}

	private class ValueIterator extends AbstractIterator<V> {
		public V next() {
			return nextEntry().getValue();
		}
	}

	private class KeyIterator extends AbstractIterator<Integer> {
		public Integer next() {
			return new Integer(nextEntry().key);
		}
	}
}