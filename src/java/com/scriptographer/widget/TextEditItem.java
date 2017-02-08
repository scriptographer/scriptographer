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
 * File created on 03.01.2005.
 */

package com.scriptographer.widget;

import java.util.EnumSet;
import com.scriptographer.ui.Size;
import com.scriptographer.ui.Border;

import com.scriptographer.ScriptographerEngine; 
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntegerEnumUtils;

/**
 * @author lehni
 * 
 * @jshide
 */
public abstract class TextEditItem<S> extends TextValueItem {

	private boolean unitInitialized = false;

	protected TextEditItem(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
	}

	/**
	 * For subclasses
	 * 
	 * @param dialog
	 * @param type
	 * @param options
	 */
	protected TextEditItem(Dialog dialog, ItemType type,
			EnumSet<TextOption> options) {
		super(dialog, type, IntegerEnumUtils.getFlags(options));
		// -1 == Maximum length.
		setMaxLength(-1);
	}
	
	protected TextEditItem(Dialog dialog, EnumSet<TextOption> options) {
		// filter out the pseudo styles from the options:
		// (max. real bit is 3, and the mask is (1 << (max + 1)) - 1
		this(dialog, getType(options), options);
	}

	private static ItemType getType(EnumSet<TextOption> options) {
		if (options != null) {
			// abuse the ADM's password style for creating it as a type...
			if (options.contains(TextOption.PASSWORD)) {
				return ItemType.TEXT_EDIT_PASSWORD;
			} else if (options.contains(TextOption.POPUP)) {
				return options.contains(TextOption.SCROLLING)
						? ItemType.TEXT_EDIT_SCROLLING_POPUP
						: ItemType.TEXT_EDIT_POPUP;
			} else {
				boolean multiline = (options.contains(TextOption.MULTILINE));
				if (options.contains(TextOption.READONLY)) {
					return multiline ? ItemType.TEXT_EDIT_MULTILINE_READONLY
							: ItemType.TEXT_EDIT_READONLY;
				} else if (multiline) {
					return ItemType.TEXT_EDIT_MULTILINE;
				}
			}
		}
		return ItemType.TEXT_EDIT;
	}

	public void setValue(float value) {
		super.setValue(value);
		// By default we don't want units to be shown, but this only
		// works when the text edit is actually used for values,
		// otherwise it would directly be forced into value mode...
		// So only set it to false if it is for values, and only once.
		if (!unitInitialized) {
			this.setShowUnits(false);
			unitInitialized = true;
		}
	}

	public abstract S getStyle();

	public abstract void setStyle(S style);

	/*
	 * Callback functions
	 */
	
	// TODO: are all these really needed?
	
	private Callable onPreCut = null;

	public Callable getOnPreCut() {
		return onPreCut;
	}

	public void setOnPreCut(Callable onPreCut) {
		this.onPreCut = onPreCut;
	}

	protected void onPreCut() {
		if (onPreCut != null)
			ScriptographerEngine.invoke(onPreCut, this);
	}
	
	private Callable onCut = null;

	public Callable getOnCut() {
		return onCut;
	}

	public void setOnCut(Callable onCut) {
		this.onCut = onCut;
	}

	protected void onCut() {
		if (onCut != null)
			ScriptographerEngine.invoke(onCut, this);
	}

	private Callable onPreCopy = null;

	public Callable getOnPreCopy() {
		return onPreCopy;
	}

	public void setOnPreCopy(Callable onPreCopy) {
		this.onPreCopy = onPreCopy;
	}

	protected void onPreCopy() {
		if (onPreCopy != null)
			ScriptographerEngine.invoke(onPreCopy, this);
	}

	private Callable onCopy = null;

	public Callable getOnCopy() {
		return onCopy;
	}

	public void setOnCopy(Callable onCopy) {
		this.onCopy = onCopy;
	}

	protected void onCopy() {
		if (onCopy != null)
			ScriptographerEngine.invoke(onCopy, this);
	}

	private Callable onPrePaste = null;

	public Callable getOnPrePaste() {
		return onPrePaste;
	}

	public void setOnPrePaste(Callable onPrePaste) {
		this.onPrePaste = onPrePaste;
	}

	protected void onPrePaste() {
		if (onPrePaste != null)
			ScriptographerEngine.invoke(onPrePaste, this);
	}

	private Callable onPaste = null;

	public Callable getOnPaste() {
		return onPaste;
	}

	public void setOnPaste(Callable onPaste) {
		this.onPaste = onPaste;
	}

	protected void onPaste() {
		if (onPaste != null)
			ScriptographerEngine.invoke(onPaste, this);
	}

	private Callable onPreClear = null;

	public Callable getOnPreClear() {
		return onPreClear;
	}

	public void setOnPreClear(Callable onPreClear) {
		this.onPreClear = onPreClear;
	}

	protected void onPreClear() {
		if (onPreClear != null)
			ScriptographerEngine.invoke(onPreClear, this);
	}

	private Callable onClear = null;

	public Callable getOnClear() {
		return onClear;
	}

	public void setOnClear(Callable onClear) {
		this.onClear = onClear;
	}

	protected void onClear() {
		if (onClear != null)
			ScriptographerEngine.invoke(onClear, this);
	}
	
	private Callable onPreSelectionChange = null;

	public Callable getOnPreSelectionChange() {
		return onPreSelectionChange;
	}

	public void setOnPreSelectionChange(Callable onPreSelectionChange) {
		this.onPreSelectionChange = onPreSelectionChange;
	}

	protected void onPreSelectionChange() {
		if (onPreSelectionChange != null)
			ScriptographerEngine.invoke(onPreSelectionChange, this);
	}

	private Callable onSelectionChange = null;

	public Callable getOnSelectionChange() {
		return onSelectionChange;
	}

	public void setOnSelectionChange(Callable onSelectionChange) {
		this.onSelectionChange = onSelectionChange;
	}

	protected void onSelectionChange() {
		if (onSelectionChange != null)
			ScriptographerEngine.invoke(onSelectionChange, this);
	}
	private Callable onPreRedo = null;

	public Callable getOnPreRedo() {
		return onPreRedo;
	}

	public void setOnPreRedo(Callable onPreRedo) {
		this.onPreRedo = onPreRedo;
	}

	protected void onPreRedo() {
		if (onPreRedo != null)
			ScriptographerEngine.invoke(onPreRedo, this);
	}

	private Callable onRedo = null;

	public Callable getOnRedo() {
		return onRedo;
	}

	public void setOnRedo(Callable onRedo) {
		this.onRedo = onRedo;
	}

	protected void onRedo() {
		if (onRedo != null)
			ScriptographerEngine.invoke(onRedo, this);
	}

	private Callable onPreUndo = null;

	public Callable getOnPreUndo() {
		return onPreUndo;
	}

	public void setOnPreUndo(Callable onPreUndo) {
		this.onPreUndo = onPreUndo;
	}

	protected void onPreUndo() {
		if (onPreUndo != null)
			ScriptographerEngine.invoke(onPreUndo, this);
	}

	private Callable onUndo = null;

	public Callable getOnUndo() {
		return onUndo;
	}

	public void setOnUndo(Callable onUndo) {
		this.onUndo = onUndo;
	}

	protected void onUndo() {
		if (onUndo != null)
			ScriptographerEngine.invoke(onUndo, this);
	}
	
	protected void onNotify(Notifier notifier, ListEntry entry) {
		switch (notifier) {
			case PRE_CLIPBOARD_CUT:
				onPreCut();
				break;
			case POST_CLIPBOARD_CUT:
				onCut();
				break;
			case PRE_CLIPBOARD_COPY:
				onPreCopy();
				break;
			case POST_CLIPBOARD_COPY:
				onCopy();
				break;
			case PRE_CLIPBOARD_PASTE:
				onPrePaste();
				break;
			case POST_CLIPBOARD_PASTE:
				onPaste();
				break;
			case PRE_CLIPBOARD_CLEAR:
				onPreClear();
				break;
			case POST_CLIPBOARD_CLEAR:
				onClear();
				break;
			case PRE_TEXT_SELECTION_CHANGED:
				onPreSelectionChange();
				break;
			case TEXT_SELECTION_CHANGED:
				onSelectionChange();
				break;
			case PRE_CLIPBOARD_REDO:
				onPreRedo();
				break;
			case POST_CLIPBOARD_REDO:
				onRedo();
				break;
			case PRE_CLIPBOARD_UNDO:
				onPreUndo();
				break;
			case POST_CLIPBOARD_UNDO:
				onUndo();
				break;
		}
	}

	/* 
	 * text edits
	 * 
	 */

	public void setStringValue(Object value) {
		if (value != null)
			setText(value.toString());
	}
	
	public String getStringValue() {
		return getText();
	}

	public native int getFractionDigits();
	public native void setFractionDigits(int precision);

	/**
	 * @jshide
	 * @deprecated
	 */
	public int getPrecision() {
		return getFractionDigits();
	}

	/**
	 * @jshide
	 * @deprecated
	 */
	public void setPrecision(int precision) {
		setFractionDigits(precision);
	}

	/**
	 * @param length -1 for maximum allowed value.
	 */
	public native void setMaxLength(int length);
	public native int getMaxLength();

	/**
	 * @jshide
	 */
	public native void setSelection(int start, int end);
	public native int[] getSelection();
	public native void selectAll();
	
	public void setSelection(int[] range) {
		setSelection(range[0], range[1]);
	}
	
	public void setSelection(int pos) {
		setSelection(pos, pos);
	}

	/*
	 * Needed for a workaround on CS4, Mac. See native code.
	 */
	private int setSelectionTimer = 0;

	public native void setAllowMath(boolean allowMath);
	public native boolean getAllowMath();
	
	public native void setAllowUnits(boolean allowUnits);
	public native boolean getAllowUnits();

	/*
	 *  child items
	 */
	private static final int
		ITEM_TEXTEDIT = 3,
		ITEM_POPUP = 4;

	private TextEdit textEdit;
	/* TODO	
	private PopupList popupList;
	
	public TextEdit getTextEdit() {
		if (textEdit == null) {
			int handle = getChildItemHandle(ITEM_TEXTEDIT);
			textEdit = handle != 0 ? new TextEdit(dialog, handle, true) : null;
		}
		return textEdit;
	}
	
	public PopupList getPopupList() {
		if (popupList == null) {
			int handle = getChildItemHandle(ITEM_POPUP);
			if (handle != 0) {
				// Pass on notification to this item, since we are not offering
				// listeners yet, and there is no other way to receive these
				// right now...
				// TODO: Consider adding  support for listeners.
				popupList = new PopupList(dialog, handle, true) {
					protected void onNotify(Notifier notifier) {
						onNotify(notifier);
					}
				};
			}
		}
		return popupList;
	}

	public boolean hasPopupList() {
		return type == ItemType.TEXT_EDIT_SCROLLING_POPUP
				|| type == ItemType.TEXT_EDIT_POPUP;
	}

	protected Border getNativeMargin() {
		if (ScriptographerEngine.isMacintosh()) {
			// On Mac, edit fields appear to be vertically offset by 1px.
			// Correct using margins
			return new Border(-1, this instanceof SpinEdit ? -2 : -1, 1, -1);
		} else if (ScriptographerEngine.isWindows()) {
			// Popup-lists on Windows appear to need a 1px margin at the
			// bottom as they would overlap otherwise.
			if (hasPopupList() || isMultiline())
				return new Border(0, 0, 1, 0);
		} 
		return MARGIN_NONE;
	}

	protected Size getSizeCorrection() {
		if (ScriptographerEngine.isMacintosh()) {
			// This seems needed on Mac, as all TextEditItems appear 2px smaller
			// than they are told to. Also if it has a popup list, the popup
			// button gets cropped on the 2nd time bounds are set otherwise.
			return new Size(0, 2);
		} else if (ScriptographerEngine.isWindows()) {
			if (!hasPopupList() && !(this instanceof SpinEdit))
				return new Size(0, 1);
		}
		return null;
	}
*/
	protected void updateBounds(int x, int y, int width, int height,
			boolean sizeChanged) {
		super.updateBounds(x, y, width, height, sizeChanged);
/*todo		
		PopupList list = hasPopupList() ? getPopupList() : null;
		if (list != null) {
			Size size = list.getSize();
			if (ScriptographerEngine.isMacintosh()) {
				// On the Mac, the height of popup list buttons are broken and
				// need to be corrected here.
				size.height = height;
			} else if (ScriptographerEngine.isWindows()) {
				// On Windows, the width of popup list items are set wrongly and
				// need to be corrected here.
				size.width = width;
			}
			list.setSize(size);
		}
*/		
	}
}
