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
 * File created on 03.01.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class List extends ListItem<ListEntry> {

	protected List(Dialog dialog, ItemType type) {
		super(dialog, type);
	}

	public List(Dialog dialog) {
		this(dialog, ItemType.LISTBOX);
	}
	
	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	/**
	 * Empty constructor used for nested HierarchyLists 
	 */
	protected List() {
	}

	public EnumSet<ListStyle> getStyle() {
		return IntegerEnumUtils.getSet(ListStyle.class, nativeGetStyle());
	}

	public void setStyle(EnumSet<ListStyle> style) {
		nativeSetStyle(IntegerEnumUtils.getFlags(style));
	}

	public void setStyle(ListStyle[] style) {
		setStyle(EnumUtils.asSet(style));
	}
}
