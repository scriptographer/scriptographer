/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 *
 * File created on 11.03.2005.
 */

package com.scriptographer.adm;

import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 */
public class PopupMenu extends ListItem<ListEntry> {

	protected PopupMenu(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
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

	protected void onNotify(Notifier notifier) {
		super.onNotify(notifier);
		// For PopupMenus, we need to notify entries by hand:
		switch(notifier) {
		case USER_CHANGED:
		case INTERMEDIATE_CHANGED:
			// Notify entry too:
			ListEntry entry = getSelectedEntry();
			if (entry != null)
				entry.onNotify(notifier);
			onPreChange();
			break;
		}
	}	
}
