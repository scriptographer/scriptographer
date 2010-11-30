/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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

	private static CommittableMap committables = new CommittableMap();

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
	 * 
	 * @return true if anything was committed.
	 */
	protected static boolean commit(Object key, boolean endExecution) {
		boolean committed = false;
		if (key != null) {
			Committable obj = committables.get(key);
			if (obj != null) {
				obj.commit(endExecution);
				committed = true;
				// case it's a text, use the story as a key as well. it's used
				// like that in CharacterAttributes
				if (obj instanceof TextItem)
					commit(((TextItem) obj).getStory(), endExecution);
				// Remove object after committing
				committables.remove(key);
			}
		} else if (committables.size() > 0) {
			for (Committable committable : committables.values())
				committable.commit(endExecution);
			committed = true;
			committables.clear();
		}
		return committed;
	}

	/**
	 * Commits changes to objects that are associated with the given key this is
	 * usually a item, where path styles and segment lists use the item
	 * as a key when calling markDirty. If key is null, all changes are
	 * committed.
	 * 
	 * @return true if anything was committed.
	 */
	public static boolean commit(Object key) {
		return commit(key, false);
	}

	/**
	 * commit all changes and increases the internal commit version
	 */
	public static boolean commit() {
		version++;
		// Also set the modificationVersion to allow versioning for increased performance.
		ExtendedJavaObject.changeVersion = version;
		return commit(null, true);
	}

	public static void markDirty(Object key, Committable committable) {
		committables.put(key, committable);
	}

	/**
	 * a hash map that overrides put so that it creates a CommittableList in case
	 * there was one object under a key already Uses IdentityHashMaps
	 * internally, to avoid calling of equals on objects
	 */
	/*
	 * Ideally we would be using a LinkedIdentityHashMap here but that class does not exist.
	 */
	static class CommittableMap {
		// A linked map of committables identified by the identity hash of their
		// key object. If there is more than one committable per key, a
		// CommittableGroup is created under the key
		LinkedHashMap<Integer, Committable> committables =
				new LinkedHashMap<Integer, Committable>();
		// Keep track of values that have been added already, identified by
		// their own identity hash. Committables that are grouped are still kept
		// in one list here too.
		LinkedHashMap<Integer, Committable> values =
				new LinkedHashMap<Integer, Committable>();

		/**
		 * A helper class that's needed when there are more than on object for
		 * one key. It forwards calls to commit() It actually uses a map so that
		 * every object can only be added once in order to void more than one
		 * call to commit at a time
		 * 
		 * Use a LinkedHashMap in order to preserve sequence of commits
		 */
		static class CommittableGroup implements Committable {
			LinkedHashMap<Integer, Committable> committables = new LinkedHashMap<Integer, Committable>();

			public void commit(boolean endExecution) {
				for (Committable committable : committables.values())
					committable.commit(endExecution);
			}

			public void add(Committable obj) {
				committables.put(System.identityHashCode(obj), obj);
			}

			public Collection<Committable> values() {
				return committables.values();
			}
		}

		public Committable put(Object key, Committable obj) {
			int valueHash = System.identityHashCode(obj);
			if (values.containsKey(valueHash))
				return null;
			values.put(valueHash, obj);
			int keyHash = System.identityHashCode(key);
			Committable prev = committables.get(keyHash);
			if (prev != null) {
				if (prev instanceof CommittableGroup) {
					// Add to existing group
					((CommittableGroup) prev).add(obj);
					return prev;
				}
				// Create a new group, add both, and put back
				CommittableGroup list = new CommittableGroup();
				list.add(prev);
				list.add(obj);
				return committables.put(keyHash, list);
			}
			// Simply add this object
			return committables.put(keyHash, obj);
		}

		public Committable get(Object key) {
			return committables.get(System.identityHashCode(key));
		}

		public Collection<Committable> values() {
			return committables.values();
		}

		public int size() {
			return committables.size();
		}

		public Committable remove(Object key) {
			int keyHash = System.identityHashCode(key);
			Committable obj = committables.remove(keyHash);
			if (obj instanceof CommittableGroup) {
				for (Committable committable : ((CommittableGroup) obj).values()) {
					values.remove(System.identityHashCode(committable));
				}
			} else {
				values.remove(System.identityHashCode(obj));
			}
			return obj;
		}

		public void clear() {
			committables.clear();
			values.clear();
		}
	}
}
