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
 * File created on 11.03.2005.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class PopupList extends ListItem<ListEntry> {
	
	protected PopupList(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	public PopupList(Dialog dialog, boolean scrolling) {
		super(dialog, scrolling ? ItemType.SCROLLING_POPUP_LIST : ItemType.POPUP_LIST);
	}

	public PopupList(Dialog dialog) {
		this(dialog, false);
	}

	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	protected static final Border MARGIN_POPUPLIST = ScriptographerEngine.isMacintosh() ?
			new Border(4, 4, 0, 4) : new Border(1, 2, 1, 2);

	protected Border getNativeMargin() {
		return MARGIN_POPUPLIST;
	}
}
