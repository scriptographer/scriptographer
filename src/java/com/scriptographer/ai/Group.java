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
 * File created on 03.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.List;
import com.scratchdisk.list.Lists;

/**
 * A Group is a collection of items. When you transform a Group, it's
 * children are treated as a single unit without changing their relative
 * positions. The Group's style or transparency blending attributes affect the
 * rendering of it's children without changing their style/attributes.
 * 
 * @author lehni
 */
public class Group extends Item {
	
	protected Group(int handle) {
		super(handle);
	}

	/**
	 * Creates a group item
	 */
	public Group() {
		super(TYPE_GROUP);
	}
	
	/**
	 * Creates a group item from the supplied list of items
	 * @param children either an {@link ItemList} or an array
	 */
	public Group(List<? extends Item> children) {
		this();
		for (Item item : children)
			this.appendChild(item);
	}
	
	public Group(Item[] children) {
		this(Lists.asList(children));
	}
	
	/**
	 * @jsbean A boolean value that specifies whether the group item is to be clipped.
	 * @jsbean When setting to true, the first child in the group is automatically
	 * @jsbean defined as a clipMask by setting its clipMask property to true.
	 * @jsbean
	 * @jsbean Sample code:
	 * @jsbean
	 * @jsbean <pre>
	 * @jsbean var group = new Group();
	 * @jsbean group.appendChild(path);
	 * @jsbean group.clipped = true;
	 * @jsbean </pre>
	 */
	public native boolean isClipped();
	public native void setClipped(boolean clipped);

	/*
	 * Setting selected attribute on groups only works when also explicitly
	 * applying the same attributes to all the children, even when using setFully
	 * selected. So override both here.
	 */
	public void setSelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setSelected(selected);
			child = child.getNextSibling();
		}
		super.setSelected(selected);
	}

	public void setFullySelected(boolean selected) {
		Item child = getFirstChild();
		while (child != null) {
			child.setFullySelected(selected);
			child = child.getNextSibling();
		}
		super.setFullySelected(selected);
	}
}