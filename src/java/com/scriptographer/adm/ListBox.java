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
 * File created on 03.01.2005.
 *
 * $RCSfile: ListBox.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/10 22:48:43 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;

public class ListBox extends Item {
	
	// SegmentList box styles
	public final static int
		STYLE_MULTISELECT = (1 << 0),
		STYLE_DIVIDED = (1 << 1),
		STYLE_TILE = (1 << 2),
		STYLE_ENTRY_ALWAYS_SELECTED = (1 << 3),
		STYLE_BLACK_RECT = (1 << 4),
		STYLE_USE_IMAGE = (1 << 5),
		STYLE_ENTRYTEXT_EDITABLE = (1 << 6);

	protected List list;

	protected ListBox(Dialog dialog, String type, Rectangle2D bounds, int style, int options) {
		super(dialog, type, bounds, style, options);
	}

	public ListBox(Dialog dialog, Rectangle2D bounds, int style) {
		this(dialog, Item.TYPE_LISTBOX, bounds, style, 0);
	}

	public ListBox(Dialog dialog, Rectangle2D bounds) {
		this(dialog, bounds, 0);
	}

	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		// TODO: could there be some notifiactions for Frame?
	}

	public List getList() {
		if (list == null)
			list = new List(this);
		return list;
	}
}
