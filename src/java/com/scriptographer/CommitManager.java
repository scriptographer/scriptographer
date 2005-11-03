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
 * File created on 15.02.2005.
 *
 * $RCSfile: CommitManager.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/11/03 00:00:15 $
 */

package com.scriptographer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CommitManager {
	
	private CommitManager() {
		// Don't let anyone instantiate this class.
	}
	
	private static CommitableMap commitables = new CommitableMap();

	/**
	 * The version that gets increased by one on each commit.
	 * This can be used to keep track of values that need synching only
	 * once on each execution of a script, or when things are changed
	 * 
	 * It also gets increased in Art.updateIfWrapped
	 */
	public static int version = 0;

	/**
	 * commits only changes to objects that are associated with the given key
	 * this is usually a art object, where path styles and segment lists use
	 * the art object as a key when calling markDirty
	 */
	public static void commit(Object key) {
		Commitable obj = (Commitable) commitables.get(key);
		if (obj != null)
			obj.commit();
	}
	
	/**
	 * commit all changes
	 */
	public static void commit() {
		if (commitables.size() > 0) {
			for (Iterator iterator = commitables.entrySet().iterator(); iterator.hasNext();) {
				((Commitable) iterator.next()).commit();
			}
			commitables.clear();
		}
		version++;
	}

	public static void markDirty(Object key, Commitable commitable) {
		commitables.put(key, commitable);
	}
	
	/**
	 * a hash map that overrides put so that it creates a CommitableList
	 * in case there was one object under a key already
	 */
	static class CommitableMap extends HashMap {
		/**
		 * A helper class that's needed when there are more than on object for one key.
		 * It forwards calls to commit()
		 */
		class CommitableList extends ArrayList implements Commitable {
			public void commit() {
				for (int i = size() - 1; i >= 0; i--)
					((Commitable) get(i)).commit();
			}
		}

		public Object put(Object key, Object obj) {
			Object prev = this.get(key);
			if (prev != null) {
				if (prev instanceof CommitableList) {
					// add to existing list
					((CommitableList) prev).add(obj);
					return prev;
				} else {
					// create a new list, add both, and put back
					CommitableList list = new CommitableList();
					list.add(prev);
					list.add(obj);
					return this.put(key, list);
				}
			} else {
				// simply add this object
				return this.put(key, obj);
			}
		}
	}
}
