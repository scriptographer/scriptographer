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

import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class PopupList extends ListItem<ListEntry> {
	
	protected PopupList(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	public PopupList(Dialog dialog, boolean scrolling) {
		super(dialog, scrolling ? ItemType.SCROLLING_POPUP_LIST
				: ItemType.POPUP_LIST);
	}

	public PopupList(Dialog dialog) {
		this(dialog, false);
	}

	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	protected void updateBounds(int x, int y, int width, int height,
			boolean sizeChanged) {
		// When resizing PopupLists on Mac, weird artifacts of previous popup
		// lists stay around if they are not made invisible first.
		boolean fixRedraw = ScriptographerEngine.isMacintosh() && isVisible();
		if (fixRedraw)
			setVisible(false);
		super.updateBounds(x, y, width, height, sizeChanged);
		if (fixRedraw)
			setVisible(true);
	}
}

