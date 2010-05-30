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
 * File created on 31.12.2004.
 */

package com.scriptographer.ui;

import java.io.IOException;
import java.util.Iterator;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.List;
import com.scratchdisk.list.ListIterator;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;
import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine;

/**
 * ListItem is a UI list item (e.g. ListBox, PopupMenu, ...) and a
 * UI list object (e.g. ListBox, HierarchyListBox) in one. It melts the
 * two objects to one and makes handling much easier.
 * 
 * @author lehni
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
#define kADMSpinEditScrollingPopupType "ADM Spin Edit Scrolling Popup Type"
#define kADMTextEditPopupType "ADM Text Edit Popup Type"
#define kADMTextEditScrollingPopupType "ADM Text Edit Scrolling Popup Type"
*/

public abstract class ListItem<E extends ListEntry> extends Item implements
		List<E>, ReadOnlyStringIndexList<E> {

	protected ListItem(Dialog dialog, ItemType type) {
		super(dialog, type);
		listHandle = nativeInit(handle);
	}

	protected ListItem(Dialog dialog, int handle, boolean isChild) {
		super(dialog, handle, isChild);
		listHandle = nativeInit((int) handle);
	}

	/**
	 * Empty constructor used for nested HierarchyListBoxes 
	 */
	protected ListItem() {
	}
	
	private Callable onPreChange = null;

	public Callable getOnPreChange() {
		return onPreChange;
	}

	public void setOnPreChange(Callable onPreChange) {
		this.onPreChange = onPreChange;
	}
	
	protected void onPreChange() {
		if (onPreChange != null)
			ScriptographerEngine.invoke(onPreChange, this);
	}
	
	private Callable onChange = null;
	
	protected void onChange() {
		if (onChange != null)
			ScriptographerEngine.invoke(onChange, this);
	}

	public Callable getOnChange() {
		return onChange;
	}

	public void setOnChange(Callable onChange) {
		this.onChange = onChange;
	}

	protected void onNotify(Notifier notifier) {
		super.onNotify(notifier);
		switch(notifier) {
		case USER_CHANGED:
			onChange();
			break;
		case INTERMEDIATE_CHANGED:
			onPreChange();
			break;
		}
	}
	
	protected int listHandle = 0;
	private int uniqueId = 0;

	private DialogColor bgColor = DialogColor.BACKGROUND;
	
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
	
	private Callable onTrackEntry = null;

	public Callable getOnTrackEntry() {
		return onTrackEntry;
	}

	public void setOnTrackEntry(Callable func) {
		setTrackEntryCallback(func != null);
		onTrackEntry = func;
	}

	private Callable onDrawEntry = null;

	public Callable getOnDrawEntry() {
		return onDrawEntry;
	}
	
	public void setOnDrawEntry(Callable func) {
		setDrawEntryCallback(func != null);
		onDrawEntry = func;
	}

	private Callable onDestroyEntry = null;

	public Callable getOnDestroyEntry() {
		return onDestroyEntry;
	}

	public void setOnDestroyEntry(Callable func) {
		onDestroyEntry = func;
	}

	private Callable onSelectEntry = null;

	public Callable getOnSelectEntry() {
		return onSelectEntry;
	}

	public void setOnSelectEntry(Callable func) {
		onSelectEntry = func;
	}
	
	private Callable onChangeEntryText = null;

	public Callable getOnChangeEntryText() {
		return onChangeEntryText;
	}

	public void setOnChangeEntryText(Callable onChangeEntryText) {
		this.onChangeEntryText = onChangeEntryText;
	}

	protected void onNotify(Notifier notifier, E entry) {
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
	
	/**
	 * @jshide
	 */
	public native void setEntrySize(int width, int height);

	public native Size getEntrySize();
	
	public void setEntrySize(Size size) {
		setEntrySize(size.width, size.height);
	}
	
	/**
	 * @jshide
	 */
	public native void setEntryTextRect(int x, int y, int width, int height);

	public native Rectangle getEntryTextRect();
	
	public void setEntryTextRect(Rectangle rect) {
		Size entrySize = getEntrySize();
		if (rect.width > entrySize.width || rect.height > entrySize.height)
			setEntrySize(
					Math.max(rect.width, entrySize.width),
					Math.max(rect.height, entrySize.height));
		setEntryTextRect(rect.x, rect.y, rect.width, rect.height);
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
	
	private native void nativeSetBackgroundColor(int color);

	public void setBackgroundColor(DialogColor color) {
		if (color != null) {
			bgColor = color;
			nativeSetBackgroundColor(color.value);
		}
	}

	public DialogColor getBackgroundColor() {
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

	public native E get(String text);

	/**
	 * @jshide
	 */
	public native E getAt(int x, int y);

	public E getAt(Point point) {
		return getAt(point.x, point.y);
	}

	public native E getSelectedEntry();

	public void setSelectedEntry(E entry) {
		if (entry.getList() == this) {
			for (E prev : getSelectedEntries())
				prev.setSelected(false);
			entry.setSelected(true);
		}
	}

	/**
	 * @deprecated
	 */
	public E getActiveEntry() {
		return getSelectedEntry();
	}

	public native E[] getSelectedEntries();

	/**
	 * @deprecated
	 */
	public E[] getSelected() {
		return getSelectedEntries();
	}

	/*
	 * List interface 
	 * 
	 */

	public native int size();

	public native E get(int index);

	public E set(int index, E element) {
		E old = remove(index);
		if (old != null && add(index, element) != null) {
			return old;
		}
		return null;
	}
	
	protected abstract E createEntry(int index);

	public E add(int index, E entry) {
		E res = createEntry(index);
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
		return res;
	}

	public E add(int index, String element) {
		E entry = createEntry(index);
		entry.setText(element);
		return entry;
	}

	// TODO: think about some kind of field value cashing in E
	// otherwise values returned by remove() are unusable and cannot 
	// be added to the list again.
	public native E remove(int index);

	// as there is no Polymorphism in Java, so all these need to be copied 
	// over from AbstractList.
	
	public E add(E element) {
		return add(-1, element);
	}

	public E add(String element) {
		return add(-1, element);
	}

	public void remove(int fromIndex, int toIndex) {
		for (int i = toIndex - 1; i >= fromIndex; i--)
			remove(i);
	}

	public final void removeAll() {
		remove(0, size());
	}

	/**
	 * @jshide
	 */
	public boolean addAll(ReadOnlyList<? extends E> elements) {
		boolean modified = false;
		int size = elements.size();
		int index = size();
		for (int i = 0; i < size; i++) {
			if (add(index++, elements.get(i)) != null)
				modified = true;
		}
		return modified;
	}

	public final boolean addAll(E[] elements) {
		return addAll(Lists.asList(elements));
	}

	/**
	 * @jshide
	 */
	public ExtendedList<E> getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}

	/**
	 * @jshide
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	public Iterator<E> iterator() {
		return new ListIterator<E>(this);
	}

	public E getFirst() {
		return size() > 0 ? get(0) : null;
	}

	public E getLast() {
		int size = size();
		return size > 0 ? get(size - 1) : null;
	}

	public Class<? extends ListEntry> getComponentType() {
		return ListEntry.class;
	}
}
