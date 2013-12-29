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
 * File created on Apr 15, 2008.
 */

package com.scriptographer.widget;

import java.util.HashMap;

/**
 * ADMItemType
 * 
 * @author lehni
 */
public enum ItemType {
	DIAL("ADM Dial Type"), // wrapped		
	FRAME("ADM Frame Type"), // wrapped
	ITEMGROUP("ADM Item Group Type"), // wrapped
	TABBED_MENU("ADM Tabbed Menu Type"), // deprecated
	LISTBOX("ADM List Box Type"), // wrapped
	HIERARCHY_LISTBOX("ADM Hierarchy List Box Type"), // wrapped
	PICTURE_CHECKBOX("ADM Picture Check Box Button Type"), // wrapped
	PICTURE_PUSHBUTTON("ADM Picture Push Button Type"), // wrapped
	PICTURE_RADIOBUTTON("ADM Picture Radio Button Type"), // wrapped
	PICTURE_STATIC("ADM Picture Static Type"), // wrapped
	POPUP_CONTROL("ADM Popup Control Type"),
	POPUP_CONTROLBUTTON("ADM Popup Control Button Type"),
	POPUP_SPINEDIT_CONTROL("ADM Popup Spin Edit Control Type"),
	POPUP_LIST("ADM Popup List Type"), // wrapped
	POPUP_MENU("ADM Popup Menu Type"), // wrapped
	RESIZE("ADM Resize Type"), // wrapped (dialog.getResizeButton() Button)
	SCROLLBAR("ADM Scrollbar Type"), // wrapped
	SCROLLING_POPUP_LIST("ADM Scrolling Popup List Type"), // wrapped
	SLIDER("ADM Slider Type"), // wrapped
	SPINEDIT("ADM Spin Edit Type"), // wrapped
	SPINEDIT_POPUP("ADM Spin Edit Popup Type"), // wrapped
	SPINEDIT_SCROLLING_POPUP("ADM Spin Edit Scrolling Popup Type"), // wrapped
	TEXT_CHECKBOX("ADM Text Check Box Type"), // wrapped
	TEXT_EDIT("ADM Text Edit Type"), // wrapped
	TEXT_EDIT_READONLY("ADM Text Edit Read-only Type"), // wrapped
	TEXT_EDIT_MULTILINE("ADM Text Edit Multi Line Type"), // wrapped
	TEXT_EDIT_MULTILINE_READONLY("ADM Text Edit Multi Line Read-only Type"), // wrapped
	TEXT_EDIT_POPUP("ADM Text Edit Popup Type"), // wrapped
	TEXT_EDIT_SCROLLING_POPUP("ADM Text Edit Scrolling Popup Type"), // wrapped
	TEXT_EDIT_PASSWORD("ADM Password Text Edit Type"), // wrapped
	TEXT_PUSHBUTTON("ADM Text Push Button Type"), // wrapped
	TEXT_RADIOBUTTON("ADM Text Radio Button Type"), // wrapped
	TEXT_STATIC("ADM Text Static Type"), // wrapped
	TEXT_STATIC_MULTILINE("ADM Text Static Multi Line Type"), // wrapped
	PROGRESS_BAR("ADM Progress Bar Type"), // wrapped
	CHASING_ARROWS("ADM Chasing Arrows Type"), // wrapped
	USER("ADM User Type"),
	MULTICOLUMN_LISTVIEW("ADM Multi Column List View Type"),
	SCROLLING_VIEW("ADM Scrolling View Type"),
	TABGROUP("ADM Tab Group Type"),
	// Fake Types
	SPACER("ADM Spacer");


	protected String name;

	private ItemType(String name) {
		this.name = name;
	}

	// hashmap for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap<String, ItemType> types =
		new HashMap<String, ItemType>();

	static {
		for (ItemType type : values())
			types.put(type.name, type);
	}
	
	public static ItemType get(String name) {
		ItemType type = types.get(name);
		if (type != null)
			return type;
		System.err.println("ItemType not found " + name);
		return null;
	}
}
