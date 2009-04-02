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
 * File created on 31.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scriptographer.ai.Rectangle;

/**
 * @author lehni
 */
public class HierarchyListEntry extends ListEntry {
	private DialogFont font = DialogFont.DEFAULT;
	private DialogColor bgColor = DialogColor.BACKGROUND;
	private DialogColor textColor = DialogColor.TEXT;
	private DialogColor dividerColor = DialogColor.BLACK;
	protected HierarchyList childList = null;
	
	/**
	 * This constructor is used by HierarchyList#createEntry / ListItem#add
	 * @param list
	 * @param index
	 */
	public HierarchyListEntry(HierarchyList list, int index) {
		super(list, index);
	}

	public HierarchyListEntry(HierarchyList list, boolean hasChildren) {
		super(list);
		if (hasChildren)
			createChildList();
	}
	
	public HierarchyListEntry(HierarchyList list) {
		this(list, false);
	}

	/*
	 * container accessors
	 *
	 */
	
	public native Item getItem();

	/*
	 * hierarchy accessors
	 *
	 */
	
	
	public HierarchyList getChildList() {
		return childList;
	}
	
	public HierarchyList createChildList() {
		if (childList == null)
			childList = new HierarchyList(this);
		return childList;
	}

	public native void setExpanded(boolean expanded);
	public native boolean isExpanded();

	public native void setEntryNameHidden(boolean nameHidden);
	public native boolean isEntryNameHidden();

	public native void setChildSelectable(boolean selectable);
	public native boolean isChildSelectable();

	public native int getDepth();
	public native int getVisualDepth();

	/*
	 * selection
	 *
	 */

	public native boolean areChildrenSelected();
	
	// for the automatic bean detection in rhino, so it becomes exposes
	// as a read-only property named .childrenSelected
	// TODO: find better name!
	public boolean getChildrenSelected() {
		return areChildrenSelected();
	}

	/*
	 * bounds accessors
	 *
	 */

	public native Rectangle getExpandArrowRect();

	/*
	 * for in-place editing: text rect used to display the edit field.
	 *
	 */

	public native void setTextRect(Rectangle rect);

	/*
	 * for controls in lists
	 *
	 */

	public native Item getEntryItem();
	public native void setEntryItem(Item item);
		
	/*
	 * customizing appearance
	 *
	 */
	
	public native void nativeSetFont(int font);
	public native void nativeSetTextColor(int color);
	public native void nativeSetBackgroundColor(int color); // Drawer.COLOR_*
	public native void nativeSetDividerColor(int color);

	public void setFont(DialogFont font) {
		if (font != null) {
			this.font = font;
			nativeSetFont(font.value);
		}
	}
	
	public DialogFont getFont() {
		return font;
	}

	public void setTextColor(DialogColor color) {
		if (color != null) {
			textColor = color;
			nativeSetTextColor(color.value);
		}
	}
	
	public DialogColor getTextColor() {
		return textColor;
	}

	public void setBackgroundColor(DialogColor color) {
		if (color != null) {
			bgColor = color;
			nativeSetBackgroundColor(color.value);
		}
	}
	
	public DialogColor getBackgroundColor() {
		return bgColor;
	}

	public void setDividerColor(DialogColor color) {
		if (color != null) {
			dividerColor = color;
			nativeSetDividerColor(color.value);
		}
	}
	
	public DialogColor getDividerColor() {
		return dividerColor;
	}
}
