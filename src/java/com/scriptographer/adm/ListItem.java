/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: ListItem.java,v $
 * $Author: lehni $
 * $Revision: 1.9 $
 * $Date: 2006/10/18 14:08:29 $
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.io.IOException;

import org.mozilla.javascript.Function;

import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;
import com.scriptographer.util.SimpleList;

/**
 * ListItem is a ADM list item (e.g. ListBox, PopupMenu, ...) and a
 * ADM list object (e.g. List, HierarchyList) in one. It melts the
 * two objects to one and makes handling much easier.
 */
/*
The GetList() function returns a reference to inItem's ADM List object.
Once obtained, the ADM List suite functions can be used to access the list.
The following item types have valid list objects:
#define kADMListBoxType "ADM List Box Type"
#define kADMPopupListType "ADM Popup List Type"
#define kADMPopupMenuType "ADM Popup Menu Type"
#define kADMScrollingPopupListType "ADM Scrolling Popup List Type"
#define kADMSpinEditPopupType "ADM Spin Edit Popup Type"
#define kADMSpinEditScrollingPopupType
"ADM Spin Edit Scrolling Popup Type"
#define kADMTextEditPopupType "ADM Text Edit Popup Type"
#define kADMTextEditScrollingPopupType
"ADM Text Edit Scrolling Popup Type"
*/

public abstract class ListItem extends Item implements SimpleList {

	protected ListItem(Dialog dialog, int type, int options) {
		super(dialog, type, options);
		listHandle = nativeInit(handle);
	}
	
	protected ListItem(Dialog dialog, long itemHandle) {
		super(dialog, itemHandle);
		listHandle = nativeInit((int) itemHandle);
	}
	
	protected ListItem() {
	}

	protected void onNotify(int notifier) throws Exception {
		super.onNotify(notifier);
		if (notifier == Notifier.NOTIFIER_USER_CHANGED) {
			// redirect to entry:
			ListEntry entry = getActiveEntry();
			if (entry != null) {
				entry.onNotify(notifier);
			}
		}
	}
	
	protected int listHandle = 0;
	private int uniqueId = 0;

	private int bgColor = Drawer.COLOR_BACKGROUND;
	
	protected int getUniqueId() {
		return uniqueId++;
	}
	
	private native int nativeInit(int itemHandle);
	
	/*
	 * Callback functions
	 */

	private boolean trackEntryCallback = false;
	private boolean drawEntryCallback = false;

	public void setTrackEntryCallback(boolean enabled) {
		nativeSetTrackEntryCallback(enabled);
		trackEntryCallback = enabled;
	}

	public boolean getTrackEntryCallback() {
		return trackEntryCallback;
	}

	public void setDrawEntryCallback(boolean enabled) {
		nativeSetDrawEntryCallback(enabled);
		drawEntryCallback = enabled;
	}

	public boolean getDrawEntryCallback() {
		return drawEntryCallback;
	}
	
	protected Function onTrackEntry = null;
	protected Function onDrawEntry = null;

	public void setOnTrackEntry(Function func) {
		setTrackEntryCallback(func != null);
		onTrackEntry = func;
	}

	public Function getOnTrackEntry() {
		return onTrackEntry;
	}
	
	public void setOnDrawEntry(Function func) {
		setDrawEntryCallback(func != null);
		onDrawEntry = func;
	}

	public Function getOnDrawEntry() {
		return onDrawEntry;
	}

	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		// redirect to the entry that takes care of this:
		entry.onNotify(notifier);
	}

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackEntryCallback(boolean enabled);
	protected native void nativeSetDrawEntryCallback(boolean enabled);

	/*
	 * menu IDs
	 *
	 */

	/*
	public native void setMenuID(SPPluginRef inMenuResPlugin,
				ADMInt32 inMenuResID, const char* inMenuResName);

	public native ADMInt32 getMenuID(ADMListRef inList);
	*/

	/*
	 * container accessor
	 *
	 */
	
//	public native Item getItem();

	/*
	 * override hooks
	 *
	 */

	/*
	 * entry bounds accessors
	 *
	 */
	
	public native void setEntrySize(int width, int height);
	public native Dimension getEntrySize();
	
	public void setEntrySize(Dimension size) {
		setEntrySize(size.width, size.height);
	}

	public void setEntrySize(Point2D size) {
		setEntrySize((int)size.getX(), (int)size.getY());
	}
	
	public void setEntrySize(int[] size) {
		setEntrySize(size[0], size[1]);
	}
	
	public native void setEntryTextRect(int x, int y, int width, int height);
	public native Rectangle getEntryTextRect();
	
	public void setEntryTextRect(Rectangle2D rect) {
		setEntryTextRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
	}
	
	public void setEntryTextRect(int[] rect) {
		setEntryTextRect(rect[0], rect[1], rect[2], rect[3]);
	}

	/*
	 * item action mask
	 *
	 */

	/**
	 * TODO: these are for the list object underneath, not the
	 * dialog item object. but they override the one from item
	 * therefore they should be renamed. but how?
	 */
	public native void setTrackMask(int mask);
	public native int getTrackMask();

	/*
	 * customizing appearance
	 *
	 */
	
	public native void nativeSetBackgroundColor(int color); // Drawer.COLOR_*

	public void setBackgroundColor(int color) {
		bgColor = color;
		nativeSetBackgroundColor(color);
	}

	public int getBackgroundColor() {
		return bgColor;
	}

	/*
	 * searching
	 *
	 */
	
	public native void selectByText(String text);
	
	/*
	 * entry array accessors
	 *
	 */

	public native ListEntry get(String text);
	public native ListEntry getAt(int x, int y);
	
	public void getAt(Point2D point) {
		getAt((int)point.getX(), (int)point.getY());
	}

	public native ListEntry getActiveEntry();
	public native ListEntry[] getSelected();
	
	/*
	 * List interface 
	 * 
	 */

	public native int getLength();

	public native Object get(int index);

	public Object set(int index, Object element) {
		Object old = remove(index);
		if (old != null && add(index, element) != null) {
			return old;
		}
		return null;
	}
	
	protected ListEntry createEntry(int index) {
		return new ListEntry(this, index);
	}

	public Object add(int index, Object element) {
		ListEntry res = createEntry(index);
		if (element instanceof ListEntry) {
			ListEntry entry = (ListEntry) element;
			res.setText(entry.getText());
			res.setEnabled(entry.isEnabled());
			res.setActive(entry.isActive());
			res.setChecked(res.isChecked());
			res.setSeparator(entry.isSeparator());
			try {
				res.setImage(entry.getImage());
				res.setSelectedImage(entry.getSelectedImage());
				res.setDisabledImage(entry.getDisabledImage());
			} catch (IOException e) {
				// will never happen with images
			}
		} else if (element != null) {
			res.setText(element.toString());
		}
		return res;
	}

	// TODO: think about some kind of field value cashing in ListEntry
	// otherwise values returned by remove() are unusable and cannot 
	// be added to the list again.
	public native Object remove(int index);

	// as there is no Polymorphism in Java, so all these need to be copied 
	// over from AbstractList.
	
	public Object add(Object element) {
		return add(-1, element);
	}

	public void remove(int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--)
			remove(i);
	}

	public final void removeAll() {
		remove(0, getLength());
	}

	public boolean addAll(ExtendedList elements) {
		boolean modified = false;
		int size = elements.getLength();
		int index = getLength();
		for (int i = 0; i < size; i++) {
			if (add(index++, elements.get(i)) != null)
				modified = true;
		}
		return modified;
	}

	public final boolean addAll(Object[] elements) {
		return addAll(Lists.asList(elements));
	}

	public ExtendedList getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	public boolean isEmpty() {
		return getLength() == 0;
	}
}
