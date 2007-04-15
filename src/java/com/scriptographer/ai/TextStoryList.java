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
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.util.ExtendedArrayList;
import com.scratchdisk.util.ExtendedList;
import com.scratchdisk.util.Lists;
import com.scratchdisk.util.ReadOnlyList;

/**
 * @author lehni
 */
class TextStoryList extends AIObject implements ReadOnlyList {
	ExtendedArrayList.List list;
	
	TextStoryList(int handle) {
		super(handle);
		list = new ExtendedArrayList.List();
	}

	private native int nativeSize(int handle);

	public int size() {
		return nativeSize(handle);
	}

	private native TextStory nativeGet(int handle, int index, TextStory curStory);

	public Object get(int index) {
		// update buffer length
		list.setSize(nativeSize(handle));
		// native get returns the old cached value in case it's
		// referencing the same object, otherwise it wraps the new
		// story and returns it
		TextStory curStory = (TextStory) list.get(index);
		TextStory story = nativeGet(handle, index, curStory);
		// update cache if story has changed
		if (story != curStory)
			list.set(index, story);
		return story;
	}

	public boolean isEmpty() {
		return nativeSize(handle) == 0;
	}

	public ExtendedList getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}
}
