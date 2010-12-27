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
 * File created on 31.12.2004.
 */

package com.scriptographer.adm;

import com.scriptographer.ai.Rectangle;

/**
 * @author lehni
 */
public class HierarchyListEntry extends ListEntry {
	private DialogFont font = DialogFont.DEFAULT;
	private DialogColor bgColor = DialogColor.BACKGROUND;
	private DialogColor textColor = DialogColor.TEXT;
	private DialogColor dividerColor = DialogColor.BLACK;
	protected HierarchyListBox childList = null;
	
	/**
	 * This constructor is used by HierarchyListBox#createEntry / ListItem#add
	 * @param list
	 * @param index
	 */
	public HierarchyListEntry(HierarchyListBox list, int index) {
		super(list, index);
	}

	public HierarchyListEntry(HierarchyListBox list, boolean hasChildren) {
		super(list);
		if (hasChildren)
			createChildList();
	}
	
	public HierarchyListEntry(HierarchyListBox list) {
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
	
	public HierarchyListEntry getParentEntry() {
		return ((HierarchyListBox) getList()).getParentEntry();
	}
	
	public HierarchyListBox getChildList() {
		return childList;
	}
	
	public HierarchyListBox createChildList() {
		if (childList == null)
			childList = new HierarchyListBox(this);
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
