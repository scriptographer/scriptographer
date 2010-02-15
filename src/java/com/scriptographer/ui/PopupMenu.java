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

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class PopupMenu extends ListItem<ListEntry> {

	protected PopupMenu(Dialog dialog, int handle) {
		super(dialog, handle);
	}

	public PopupMenuStyle getStyle() {
		return IntegerEnumUtils.get(PopupMenuStyle.class, nativeGetStyle());
	}

	public void setStyle(PopupMenuStyle style) {
		if (style != null)
			nativeSetStyle(style.value);
	}

	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	protected void onNotify(Notifier notifier) throws Exception {
		super.onNotify(notifier);
		// For PopupMenus, we need to notify entries by hand:
		switch(notifier) {
		case USER_CHANGED:
		case INTERMEDIATE_CHANGED:
			// Notify entry too:
			ListEntry entry = getActiveEntry();
			if (entry != null)
				entry.onNotify(notifier);
			onPreChange();
			break;
		}
	}	
}
