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
 * File created on 15.02.2005.
 *
 * $Id$
 */

package com.scriptographer;

import java.util.Collection;
import java.util.LinkedHashMap;

import com.scratchdisk.script.rhino.ExtendedJavaObject;
import com.scriptographer.ai.TextItem;

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
	 * It also gets increased in Item.updateIfWrapped
	 */
	public static int version = 0;

	/**
	 * Commits changes to objects that are associated with the given key this is
	 * usually a item, where path styles and segment lists use the item
	 * as a key when calling markDirty. If key is null, all changes are
	 * committed.
	 */
	public static void commit(Object key) {
		if (key != null) {
			Commitable obj = commitables.get(key);
			if (obj != null) {
				obj.commit();
				// case it's a text, use the story as a key as well. it's used
				// like that in CharacterAttributes
				if (obj instanceof TextItem)
					commit(((TextItem) obj).getStory());
				// Remove object after committing
				commitables.remove(key);
			}
		} else if (commitables.size() > 0) {
			for (Commitable commitable : commitables.values())
				commitable.commit();
			commitables.clear();
		}
	}

	/**
	 * commit all changes and increases the internal commit version
	 */
	public static void commit() {
		version++;
		// Also set the modificationVersion to allow versioning for increased performance.
		ExtendedJavaObject.changeVersion = version;
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
	/*
	 * Ideally we would be using a LinkedIdentityHashMap here but that class does not exist.
	 */
	static class CommitableMap {
		// A linked map of commitables identified by the identity hash of their key object.
		// If there is more than one comitable per key, a CommitableGroup is created under the key
		LinkedHashMap<Integer, Commitable> commitables = new LinkedHashMap<Integer, Commitable>();
		// Keep track of values that have been added already, identified by their own identity hash.
		// Commitables that are grouped are still kept in one list here too.
		LinkedHashMap<Integer, Commitable> values = new LinkedHashMap<Integer, Commitable>();

		/**
		 * A helper class that's needed when there are more than on object for
		 * one key. It forwards calls to commit() It actually uses a map so that
		 * every object can only be added once in order to void more than one
		 * call to commit at a time
		 * 
		 * Use a LinkedHashMap in order to preserve sequence of commits
		 */
		static class CommitableGroup implements Commitable {
			LinkedHashMap<Integer, Commitable> commitables = new LinkedHashMap<Integer, Commitable>();

			public void commit() {
				for (Commitable commitable : commitables.values())
					commitable.commit();
			}

			public void add(Commitable obj) {
				commitables.put(System.identityHashCode(obj), obj);
			}

			public Collection<Commitable> values() {
				return commitables.values();
			}
		}

		public Commitable put(Object key, Commitable obj) {
			int valueHash = System.identityHashCode(obj);
			if (values.containsKey(valueHash)) {
				return null;
			} else {
				values.put(valueHash, obj);
				int keyHash = System.identityHashCode(key);
				Commitable prev = commitables.get(keyHash);
				if (prev != null) {
					if (prev instanceof CommitableGroup) {
						// Add to existing group
						((CommitableGroup) prev).add(obj);
						return prev;
					} else {
						// Create a new group, add both, and put back
						CommitableGroup list = new CommitableGroup();
						list.add(prev);
						list.add(obj);
						return commitables.put(keyHash, list);
					}
				} else {
					// Simply add this object
					return commitables.put(keyHash, obj);
				}
			}
		}

		public Commitable get(Object key) {
			return commitables.get(key);
		}

		public Collection<Commitable> values() {
			return commitables.values();
		}

		public int size() {
			return commitables.size();
		}

		public Commitable remove(Object key) {
			int keyHash = System.identityHashCode(key);
			Commitable obj = commitables.remove(keyHash);
			if (obj instanceof CommitableGroup) {
				for (Commitable commitable : ((CommitableGroup) obj).values()) {
					values.remove(System.identityHashCode(commitable));
				}
			} else {
				values.remove(System.identityHashCode(obj));
			}
			return obj;
		}

		public void clear() {
			commitables.clear();
			values.clear();
		}
	}
}
