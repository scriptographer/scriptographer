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
 */

package com.scriptographer.widget;

import java.io.IOException;
import com.scriptographer.ui.Rectangle;
import com.scriptographer.ui.Point;

import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 */
public class ListEntry extends NotificationHandler {
	private Image image;
	private Image selectedImage;
	private Image disabledImage;

	protected ListItem list;
	
	/**
	 * This constructor is used by ListItem#createEntry / ListItem#add
	 * @param list
	 * @param index
	 */
	public ListEntry(ListItem list, int index) {
/*	todo	
		if (list instanceof HierarchyListBox && !(this instanceof HierarchyListEntry))
			throw new ScriptographerException(
					"Use HierarchyListEntry objects for HierarchyListBox");
*/	
		handle = nativeCreate(list, index, list.getUniqueId());
		if (handle == 0)
			throw new ScriptographerException("Unable to create list entry.");
		this.list = list;
	}

	public ListEntry(ListItem list) {
		this(list, -1); // -1 == insert at the end
	}
	
	public boolean remove() {
		if (handle > 0) {
			nativeDestroy();
			handle = 0;
			list = null;
			return true;
		}
		return false;
	}
	
	/*
	 * Callback functions
	 */
	
/* todo	
	public native boolean defaultTrack(Tracker tracker);
	public native void defaultDraw(Drawer drawer);

	protected boolean onDraw(Drawer drawer) {
		Callable onDrawEntry = list.getOnDrawEntry();
		if (onDrawEntry != null) {
			Object result = ScriptographerEngine.invoke(onDrawEntry, list, drawer, this);
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return true;
	}

	protected boolean onTrack(Tracker tracker) {
		Callable onTrackEntry = list.getOnTrackEntry();
		if (onTrackEntry != null) {
			Object res = ScriptographerEngine.invoke(onTrackEntry, list, tracker, this);
			if (res != null)
				return ConversionUtils.toBoolean(res);
		}
		return true;
	}
*/	
	Callable onDestroy = null;

	public Callable getOnDestroy() {
		return onDestroy;
	}

	public void setOnDestroy(Callable onDestroy) {
		this.onDestroy = onDestroy;
	}

	protected void onDestroy() {
		if (onDestroy != null)
			ScriptographerEngine.invoke(onDestroy, this);
		
		Callable onDestroyEntry = list.getOnDestroyEntry();
		if (onDestroyEntry != null)
			ScriptographerEngine.invoke(onDestroyEntry, list, this);
	}
	
	Callable onSelect = null;

	public Callable getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(Callable onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() {
		if (onSelect != null)
			ScriptographerEngine.invoke(onSelect, this);
		Callable onSelectEntry = list.getOnSelectEntry();
		if (onSelectEntry != null)
			ScriptographerEngine.invoke(onSelectEntry, list, this);
	}
	
	Callable onChangeText = null;

	public Callable getOnChangeText() {
		return onChangeText;
	}

	public void setOnChangeText(Callable onChangeText) {
		this.onChangeText = onChangeText;
	}
	
	protected void onChangeText() {
		if (onChangeText != null)
			ScriptographerEngine.invoke(onChangeText, this);
		Callable onChangeEntryText = list.getOnChangeEntryText();
		if (onChangeEntryText != null)
			ScriptographerEngine.invoke(onChangeEntryText, list, this);
	}
	
	protected void onNotify(Notifier notifier) {
		switch (notifier) {
		case USER_CHANGED:
			onSelect();
			break;
		case DESTROY:
			onDestroy();
			break;
		case ENTRY_TEXT_CHANGED:
			onChangeText();
			break;
		}
	}

	/*
	 * entry creation/destruction
	 * 
	 */

	protected native int nativeCreate(ListItem list, int index, int id);
	protected native void nativeDestroy();


	/* 
	 * contaer accessors
	 * 
	 */
	
	public native int getIndex();
	
	public ListItem getList() {
		return list;
	}

	/* 
	 * entry ID
	 * 
	 */
	/*
	public native void setID(int entryID);
	public native int getID();
	*/
	
	/* 
	 * entry selection segmentFlags
	 * 
	 */

	public native void setSelected(boolean select);
	public native boolean isSelected();

	/* 
	 * entry visibility
	 * 
	 */
	
	public native void makeInBounds();
	public native boolean isInBounds();

	/* 
	 * entry state accessors
	 * 
	 */

	public native void setEnabled(boolean enable);
	public native boolean isEnabled();
	
	public native void setActive(boolean activate);
	public native boolean isActive();
	
	public native void setChecked(boolean check);
	public native boolean isChecked();
	
	public native void setSeparator(boolean separator);
	public native boolean isSeparator();
/* 
	 * entry bounds accessors
	 * 
	 */

	public native Rectangle getLocalRect();
	public native Rectangle getBounds();
/* 
	 * coordate transformations
	 * 
	 */

	public native Point localToScreen(int x, int y);
	public native Point screenToLocal(int x, int y);

	public native Rectangle localToScreen(int x, int y, int width, int height);
	public native Rectangle screenToLocal(int x, int y, int width, int height);

	public Point localToScreen(Point point) {
		return localToScreen(point.x, point.y);
	}

	public Point screenToLocal(Point point) {
		return screenToLocal(point.x, point.y);
	}

	public Rectangle localToScreen(Rectangle rect) {
		return localToScreen(rect.x, rect.y, rect.width, rect.height);
	}

	public Rectangle screenToLocal(Rectangle rect) {
		return screenToLocal(rect.x, rect.y, rect.width, rect.height);
	}

	/* 
	 * redraw requests
	 * 
	 */
		
	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public void invalidate(Rectangle rect) {
		invalidate(rect.x, rect.y, rect.width, rect.height);
	}

	/* 
	 * entry picture accessors
	 * 
	 */

	private native void nativeSetImage(int iconHandle);
	private native void nativeSetSelectedImage(int iconHandle);
	private native void nativeSetDisabledImage(int iconHandle);

	public Image getImage() {
		return image;
	}
		
	public void setImage(Object obj) throws IOException {
		image = Image.getImage(obj);
		nativeSetImage(image != null ? image.createIconHandle() : 0);
	}
	
	public Image getSelectedImage() {
		return selectedImage;
	}
	
	public void setSelectedImage(Object obj) throws IOException {
		selectedImage = Image.getImage(obj);
		nativeSetSelectedImage(selectedImage != null ?
				selectedImage.createIconHandle() : 0);
	}

	public Image getDisabledImage() {
		return disabledImage;
	}

	public void setDisabledImage(Object obj) throws IOException {
		disabledImage = Image.getImage(obj);
		nativeSetDisabledImage(disabledImage != null ?
				disabledImage.createIconHandle() : 0);
	}

	/* 
	 * entry text accessors
	 * 
	 */

	public native void setText(String text);
	public native String getText();
	
	/*
	 *  entry timer accessors
	 *
	 */

	/*
	public native ADMTimerRef createTimer(ADMUt32 milliseconds,
				ADMActionMask abortMask, ADMEntryTimerProc timerProc,
				ADMEntryTimerAbortProc timerAbortProc, ADMt32 options);

	public native void abortTimer(ADMTimerRef timer);
	*/

	/* 
	 * entry checkmark accessors
	 * 
	 */

	/*
	public native void setCheckGlyph(ADMStandardCheckGlyphID checkGlyph);
	public native ADMStandardCheckGlyphID getCheckGlyph();
	*/

}
