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
 * $RCSfile: TextEdit.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/03/10 22:48:43 $
 */

package com.scriptographer.adm;

import java.awt.geom.Rectangle2D;

import com.scriptographer.js.ArgumentReader;

public class TextEdit extends TextItem {

	// ADMTextEditStyle, ADMTextEditPopupStyle
	public final static int
		// self defined pseudo styles, for creation of the right TYPE:
		STYLE_READONLY = 1 << 6,
		STYLE_MULTILINE = 1 << 7,
		STYLE_POPUP = 1 << 8, // exclusive, cannot be STYLE_READONLY | STYLE_MULTILINEÊ|  STYLE_MULTILINE!
		STYLE_SCROLLING = 1 << 9, // only for POPUP!

		// real ADM segmentValues:
		STYLE_SINGLELINE = 0,
		STYLE_NUMERIC = 2,        // 'Numeric' means float. Default.
		STYLE_EXCLUSIVE = 5,      // only for TextEditPopup
		STYLE_TRACK_RAW_KEYS = 4, // Mac-only; ignores default Carbon event processing; not compatible with kADMUnicodeEditCreateOption
		STYLE_PASSWORD = 32;      // Win32 value for ES_PADMSWORD
	
	// Options
	public final static int
		// TextEdit:
		OPTION_PASSWORD = (1 << 1),
		OPTION_UNICODE = (1 << 2), // [cpaduan] 6/18/02 - Creates a Unicode based edit box (if possible). Currently has no effect on Windows.
		OPTION_DISABLE_DRAG_DROP = (1 << 3); // Disables drag & drop from or to text edits. Currently mac-only.

	public TextEdit(Dialog dialog, Rectangle2D bounds, String text, int style, int options) {
		super(dialog, getType(style), bounds, text, getStyle(style), options);
	}

	public TextEdit(Dialog dialog, Rectangle2D bounds, String text, int style) {
		this(dialog, bounds, text, style, 0);
	}

	public TextEdit(Dialog dialog, Rectangle2D bounds, String text) {
		super(dialog, Item.TYPE_TEXT_EDIT, bounds, text,0, 0);
	}

	private static String getType(int style) {
		if ((style & STYLE_POPUP) != 0) {
			return (style & STYLE_SCROLLING) != 0 ? Item.TYPE_TEXT_EDIT_SCROLLING_POPUP
				: Item.TYPE_TEXT_EDIT_POPUP;
		} else {
			// abuse the ADM's password style for creating it as a type...
			if ((style & STYLE_PASSWORD) != 0) {
				return Item.TYPE_TEXT_EDIT_PASSWORD;
			} else {
				boolean multiline = ((style & STYLE_MULTILINE) != 0);
				if ((style & STYLE_READONLY) != 0) {
					return multiline ? Item.TYPE_TEXT_EDIT_MULTILINE_READONLY
						: Item.TYPE_TEXT_EDIT_READONLY;
				} else {
					return multiline ? Item.TYPE_TEXT_EDIT_MULTILINE
						: Item.TYPE_TEXT_EDIT;
				}
			}
		}
	}
	
	private static int getStyle(int style) {
		// filter out the pseudo styles:
		return style & ~STYLE_READONLY & ~STYLE_MULTILINE & ~STYLE_POPUP & ~STYLE_SCROLLING;
	}
	
	/*
	 * Callback stuff
	 */
	
	protected void onPreCut() throws Exception {
		callFunction("onPreCut");
	}
	
	protected void onCut() throws Exception {
		callFunction("onCut");
	}
	
	protected void onPreCopy() throws Exception {
		callFunction("onPreCopy");
	}
	
	protected void onCopy() throws Exception {
		callFunction("onCopy");
	}
	
	protected void onPrePaste() throws Exception {
		callFunction("onPrePaste");
	}
	
	protected void onPaste() throws Exception {
		callFunction("onPaste");
	}
	
	protected void onPreClear() throws Exception {
		callFunction("onPreClear");
	}
	
	protected void onClear() throws Exception {
		callFunction("onClear");
	}
	
	protected void onPreSelectionChange() throws Exception {
		callFunction("onPreSelectionChange");
	}
	
	protected void onSelectionChange() throws Exception {
		callFunction("onSelectionChange");
	}
	
	protected void onPreRedo() throws Exception {
		callFunction("onPreRedo");
	}
	
	protected void onRedo() throws Exception {
		callFunction("onRedo");
	}
	
	protected void onPreUndo() throws Exception {
		callFunction("onPreUndo");
	}
	
	protected void onUndo() throws Exception {
		callFunction("onUndo");
	}
	
	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		switch (notifier) {
			case Notifier.NOTIFIER_PRE_CLIPBOARD_CUT:
				onPreCut();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_CUT:
				onCut();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_COPY:
				onPreCopy();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_COPY:
				onCopy();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_PASTE:
				onPrePaste();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_PASTE:
				onPaste();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_CLEAR:
				onPreClear();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_CLEAR:
				onClear();
				break;
			case Notifier.NOTIFIER_PRE_TEXT_SELECTION_CHANGED:
				onPreSelectionChange();
				break;
			case Notifier.NOTIFIER_TEXT_SELECTION_CHANGED:
				onSelectionChange();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_REDO:
				onPreRedo();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_REDO:
				onRedo();
				break;
			case Notifier.NOTIFIER_PRE_CLIPBOARD_UNDO:
				onPreUndo();
				break;
			case Notifier.NOTIFIER_POST_CLIPBOARD_UNDO:
				onUndo();
				break;
		}
	}
	
	/*
	 * rest
	 */

	public String getText() {
		text = nativeGetText();
		return text;
	}

	public void setStringValue(Object value) {
		String str = new ArgumentReader().readString(value);
		if (str != null)
			setText(str);
	}
	
	public String getStringValue() {
		return getText();
	}

	/* 
	 * text edits
	 * 
	 */
		
	public native void setMaxLength(int length);
	public native int getMaxLength();

	public native void setSelection(int start, int end);
	public native int[] getSelection();
	public native void selectAll();
	
	public void setSelection(int[] range) {
		setSelection(range[0], range[1]);
	}
	
	public void setSelection(int pos) {
		setSelection(pos, pos);
	}

	public native void setAllowMath(boolean allowMath);
	public native boolean getAllowMath();
	
	public native void setAllowUnits(boolean allowUnits);
	public native boolean getAllowUnits();

}
