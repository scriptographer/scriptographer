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
 * File created on 15.02.2005.
 *
 * $Id$
 */

package com.scriptographer;

import java.util.IdentityHashMap;
import java.util.Iterator;

import com.scriptographer.ai.Document;
import com.scriptographer.ai.TextFrame;

/**
 * @author lehni
 */
public class CommitManager {

	private CommitManager() {
		// Don't let anyone instantiate this class.
	}

	private static CommitableMap commitables = new CommitableMap();

	/**
	 * The version that gets increased by one on each commit. This can be used
	 * to keep track of values that need synching only once on each execution of
	 * a script, or when things are changed
	 * 
	 * It also gets increased in Art.updateIfWrapped
	 */
	public static int version = 0;

	/**
	 * Commits changes to objects that are associated with the given key this is
	 * usually a art object, where path styles and segment lists use the art
	 * object as a key when calling markDirty. If key is null, all changes are
	 * commited.
	 */
	public static void commit(Object key) {
		if (key != null) {
			Commitable obj = (Commitable) commitables.get(key);
			if (obj != null) {
				obj.commit();
				// case it's a text, use the story as a key as well. it's used
				// like
				// that in CharacterAttributes
				if (obj instanceof TextFrame)
					commit(((TextFrame) obj).getStory());
			}
		} else if (commitables.size() > 0) {
			for (Iterator iterator = commitables.values().iterator();
					iterator.hasNext();)
				((Commitable) iterator.next()).commit();
			commitables.clear();
		}
	}

	/**
	 * commit all changes and increases the internal commit version
	 */
	public static void commit() {
		version++;
		commit(null);
	}

	public static void markDirty(Object key, Commitable commitable) {
		commitables.put(key, commitable);
	}

	/**
	 * a hash map that overrides put so that it creates a CommitableList in case
	 * there was one object under a key already Uses IdentityHashMaps
	 * internally, to avoid calling of equals on objects
	 */
	static class CommitableMap extends IdentityHashMap {
		// keep track of values that have been added already, maybe under
		// another key
		IdentityHashMap values = new IdentityHashMap();

		/**
		 * A helper class that's needed when there are more than on object for
		 * one key. It forwards calls to commit() It actually uses a map so that
		 * every object can only be added once in order to void more than one
		 * call to commit at a time
		 * 
		 * Use a LinkedHashMap in order to preserve sequence of commits
		 */
		class CommitableList extends IdentityHashMap implements Commitable {
			public void commit() {
				for (Iterator iterator = values().iterator();
						iterator.hasNext();)
					((Commitable) iterator.next()).commit();
			}

			public void add(Object obj) {
				this.put(obj, obj);
			}
		}

		public Object put(Object key, Object obj) {
			if (values.containsKey(obj)) {
				return null;
			} else {
				values.put(obj, obj);
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
						return super.put(key, list);
					}
				} else {
					// simply add this object
					return super.put(key, obj);
				}
			}
		}

		public void clear() {
			super.clear();
			values.clear();
		}
	}
}
