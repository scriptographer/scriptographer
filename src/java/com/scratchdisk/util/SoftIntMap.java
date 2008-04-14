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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftIntMap<V> extends IntMap<V> {

	public SoftIntMap() {
		super();
	}

	public SoftIntMap(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	/**
	 *  ReferenceQueue used to eliminate stale mappings.
	 */
	private transient ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	/**
	 * Entry with a soft reference
	 * 
	 * Dont use generics for the superclass, since it's storing the
	 * value in a SoftReference<V> instead of V.
	 */
	
	@SuppressWarnings("unchecked")
	private static class SoftEntry<V> extends Entry {
		public SoftEntry(int key, V value, Entry<V> next, ReferenceQueue<V> q) {
			super(key, new SoftReference<V>(value, q), next);
		}

		public V getValue() {
			return ((SoftReference<V>) value).get();
		}
	}

	@SuppressWarnings("unchecked")
	protected Entry<V> createEntry(int key, V value, Entry<V> next) {
		return new SoftEntry<V>(key, value, next, queue);
	}

	/**
	 *  Purges stale mappings from this map.<P>
	 *
	 *  Ordinarily, stale mappings are only removed during
	 *  a write operation; typically a write operation will    
	 *  occur often enough that you'll never need to manually
	 *  invoke this method.<P>
	 *
	 *  Note that this method is not synchronized!  Special
	 *  care must be taken if, for instance, you want stale
	 *  mappings to be removed on a periodic basis by some
	 *  background thread.
	 */
	protected void purge() {
		Reference ref = queue.poll();
		while (ref != null) {
			ref.clear();
			ref = queue.poll();
		}
	}

	/**
	 * Clears this map.
	 */
	public void clear() {
		super.clear();
		while (queue.poll() != null) {
			// drain the queue
		}
	}
}
