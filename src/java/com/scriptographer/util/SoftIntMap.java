/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 19.10.2005.
 * 
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class SoftIntMap extends IntMap {

	public SoftIntMap() {
		super();
	}

	public SoftIntMap(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	/**
	 *  ReferenceQueue used to eliminate stale mappings.
	 */
	private transient ReferenceQueue queue = new ReferenceQueue();
	
	/**
	 * Entry with a soft reference
	 */
	private static class SoftEntry extends Entry {
		public SoftEntry(int key, Object value, Entry next, ReferenceQueue q) {
			super(key, new SoftReference(value, q), next);
		}

		public Object getValue() {
			return ((SoftReference) value).get();
		}
	}

	protected Entry createEntry(int key, Object value, Entry next) {
		return new SoftEntry(key, value, next, queue);
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
