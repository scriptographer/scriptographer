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

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class List extends ListItem {
	
	// SegmentList box styles
	public static final int
		STYLE_MULTISELECT = (1 << 0),
		STYLE_DIVIDED = (1 << 1),
		STYLE_TILE = (1 << 2),
		STYLE_ENTRY_ALWAYS_SELECTED = (1 << 3),
		STYLE_BLACK_RECT = (1 << 4),
		STYLE_USE_IMAGE = (1 << 5),
		STYLE_ENTRYTEXT_EDITABLE = (1 << 6);

	protected List(Dialog dialog, int type, int options) {
		super(dialog, type, options);
	}

	public List(Dialog dialog) {
		this(dialog, TYPE_LISTBOX, OPTION_NONE);
	}
	
	/**
	 * Empty constructor used for nested HierarchyLists 
	 */
	protected List() {
	}
}
