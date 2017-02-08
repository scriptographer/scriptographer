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

import java.util.EnumSet;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.EnumUtils;
import com.scratchdisk.util.IntegerEnumUtils;

import com.scriptographer.ui.Point;
import com.scriptographer.ui.Rectangle;
import com.scriptographer.ui.Size;

/**
 * @author lehni
 */
public class HierarchyListBox extends ListItem<HierarchyListEntry> {
	
	public HierarchyListBox(Dialog dialog) {
		super(dialog, ItemType.HIERARCHY_LISTBOX);
	}

	private HierarchyListEntry parentEntry = null;
	
	/**
	 * Creates a child List for a HierarchyListEntry. This constructor
	 * is used indirectly through HierarchyListEntry only.
	 * @param entry
	 */
	protected HierarchyListBox(HierarchyListEntry entry) {
		listHandle = nativeCreateChildList(entry);
		// determine the parent hierarchyList:
		parentEntry = entry;
		parentEntry.childList = this;
		HierarchyListBox parentList = (HierarchyListBox) entry.getList();
		// Pass through track / draw callback settings
		// This requires the callback to be set before child lists are created
		// as otherwise only the parent list would recieve callbacks!
		this.setTrackEntryCallback(parentList.getTrackEntryCallback());
		this.setDrawEntryCallback(parentList.getDrawEntryCallback());
	}

	public Class<? extends ListEntry> getComponentType() {
		return HierarchyListEntry.class;
	}

	public EnumSet<ListStyle> getStyle() {
		return IntegerEnumUtils.getSet(ListStyle.class, nativeGetStyle());
	}

	public void setStyle(EnumSet<ListStyle> style) {
		nativeSetStyle(IntegerEnumUtils.getFlags(style));
	}

	public void setStyle(ListStyle[] style) {
		setStyle(EnumUtils.asSet(style));
	}
	
	public boolean remove() {
		HierarchyListEntry parentEntry = nativeRemoveList(listHandle);
		if (parentEntry != null) {
			parentEntry.childList = null;
			listHandle = 0;
			return true;
		}
		return false;
	}
	
	protected int getUniqueId() {
		// walk the hierarchy up and use the root's uniqueId function only:
		if (parentEntry != null) {
			return parentEntry.getList().getUniqueId();
		} else {
			return super.getUniqueId();
		}
	}
	
	private native int nativeCreateChildList(HierarchyListEntry entry);
	private native HierarchyListEntry nativeRemoveList(int listHandle);

	/*
	 * Override all getters for ScriptFunctions in HierarchyListBoxes, so they
	 * walk up the list chain on find a function in the parent if they
	 * do not define one locally.
	 */
	public Callable getOnChangeEntryText() {
		Callable func = super.getOnChangeEntryText();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnChangeEntryText() : func;
	}

	public Callable getOnSelectEntry() {
		Callable func = super.getOnSelectEntry();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnSelectEntry() : func;
	}

	public Callable getOnDestroyEntry() {
		Callable func = super.getOnDestroyEntry();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnDestroyEntry() : func;
	}

	public Callable getOnDrawEntry() {
		Callable func = super.getOnDrawEntry();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnDrawEntry() : func;
	}

	public Callable getOnTrackEntry() {
		Callable func = super.getOnTrackEntry();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnTrackEntry() : func;
	}

	public Callable getOnDestroy() {
		Callable func = super.getOnDestroy();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnDestroy() : func;
	}

	public Callable getOnDraw() {
		Callable func = super.getOnDraw();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnDraw() : func;
	}

	public Callable getOnResize() {
		Callable func = super.getOnResize();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnResize() : func;
	}

	public Callable getOnTrack() {
		Callable func = super.getOnTrack();
		return func == null && parentEntry != null ?
				parentEntry.getList().getOnTrack() : func;
	}

	/*
	 * item draw proc
	 *
	 */

//	public native void setDrawProcRecursive(ADMListEntryDrawProc drawProc);
	
	/*
	 * item trackg proc
	 *
	 */
	
//	public native void setTrackProcRecursive(ADMListEntryTrackProc trackProc);

	/*
	 * item action mask
	 *
	 */
	
//	public native void setMaskRecursive(ADMHierarchyListRef entry, ADMActionMask mask);

	/*
	 * item notify proc
	 *
	 */
	
//	public native void setNotifyProcRecursive(ADMListEntryNotifyProc notifyProc);


	/*
	 * item destroy proc
	 *
	 */

//	public native void setDestroyProcRecursive(ADMListEntryDestroyProc destroyProc);

	/*
	 * item entry bounds
	 *
	 */

	public native void setEntrySize(int width, int height, boolean recursive);

	public native void setEntryTextRect(int x, int y, int width, int height,
			boolean recursive);

	public void setEntrySize(Size size, boolean recursive) {
		setEntrySize(size.width, size.height, recursive);
	}

	public void setEntryTextRect(Rectangle rect, boolean recursive) {
		setEntryTextRect(rect.x, rect.y, rect.width, rect.height, recursive);
	}
	
	public native int getNonLeafEntryWidth();

	public native void setNonLeafEntryTextRect(int x, int y, int width,
			int height, boolean recursive);

	public native Rectangle getNonLeafEntryTextRect();

	public void setNonLeafEntryTextRect(Rectangle rect, boolean recursive) {
		setNonLeafEntryTextRect(rect.x, rect.y, rect.width, rect.height, recursive);
	}

	public void setNonLeafEntryTextRect(Rectangle rect) {
		setNonLeafEntryTextRect(rect, false);
	}
		
	/*
	 * item hierarchy
	 *
	 */
	
	public HierarchyListEntry getParentEntry() {
		return parentEntry;
	}

	/*
	 * coordate system conversion
	 *
	 */

	public native Rectangle getLocalRect();

	public native Point localToScreen(int x, int y);
	public native Point screenToLocal(int x, int y);

	public native Point localToGlobal(int x, int y);
	public native Point globalToLocal(int x, int y);

	public native Rectangle localToGlobal(int x, int y, int width, int height);
	public native Rectangle globalToLocal(int x, int y, int width, int height);

	public Point localToScreen(Point point) {
		return localToScreen(point.x, point.y);
	}

	public Point screenToLocal(Point point) {
		return screenToLocal(point.x, point.y);
	}

	public Point localToGlobal(Point point) {
		return localToScreen(point.x, point.y);
	}

	public Point globalToLocal(Point point) {
		return screenToLocal(point.x, point.y);
	}

	public Rectangle localToGlobal(Rectangle rect) {
		return localToGlobal(rect.x, rect.y, rect.width, rect.height);
	}

	public Rectangle globalToLocal(Rectangle rect) {
		return globalToLocal(rect.x, rect.y, rect.width, rect.height);
	}

	/*
	 * item marg accessors
	 *
	 */

	public native void setIndentationWidth(int width, boolean recursive);

	public native int getIndentationWidth();

	public native void setLocalLeftMargin(int width);

	public native int getLocalLeftMargin();

	public native int getGlobalLeftMargin();

	public native void setDivided(boolean divided, boolean recursive);

	public native boolean isDivided();

	public native void setFlags(int flags, boolean recursive);

	public native int getFlags();

	public void setIndentationWidth(int width) {
		setIndentationWidth(width, false);
	}

	public void setDivided(boolean divided) {
		setDivided(divided, false);
	}

	public void setFlags(int flags) {
		setFlags(flags, false);
	}

	/*
	 * item invalidation
	 *
	 */

	public native void invalidate();

	/*
	 * leaf item accessors
	 *
	 */

	public native HierarchyListEntry[] getLeafEntries();

	/**
	 * @deprecated
	 */
	public HierarchyListEntry[] getLeafs() {
		return getLeafEntries();
	}

	public native int getLeafEntryIndex(HierarchyListEntry entry);

	/**
	 * @deprecated
	 */
	public int getLeafIndex(HierarchyListEntry entry) {
		return getLeafEntryIndex(entry);
	}

	/*
	 * item list manipulation
	 *
	 */
	
	protected HierarchyListEntry createEntry(int index) {
		return new HierarchyListEntry(this, index);
	}

	public native HierarchyListEntry getLeafEntryAt(int x, int y);

	/**
	 * @deprecated
	 */
	public HierarchyListEntry getLeafAt(int x, int y) {
		return getLeafEntryAt(x, y);
	}

	public native HierarchyListEntry getSelectedLeafEntry();

	/**
	 * @deprecated
	 */
	public HierarchyListEntry getActiveLeaf() {
		return getSelectedLeafEntry();
	}

	public void getLeafEntryAt(Point point) {
		getLeafEntryAt(point.x, point.y);
	}

	/*
	 * selection list manipulation
	 *
	 */

	/**
	 * Returns all selected entries.
	 */
	public native HierarchyListEntry[] getSelectedEntries();

	/**
	 * Returns all unnested selected entries.
	 */
	public native HierarchyListEntry[] getUnnestedSelectedEntries();

	/*
	 * item sequence manipulation
	 *
	 */
	
	public native void swap(int fromIndex, int toIndex);
	
	/*
	 * item selection
	 *
	 */

	public native void deselectAll();
	
	/*
	 * expanded item accessors
	 *
	 */

	public native HierarchyListEntry[] getExpandedEntries();

	public HierarchyListEntry[] getExpanded() {
		return getExpandedEntries();
	}

	public native int getExpandedEntryIndex(HierarchyListEntry entry);

	public int getExpandedIndex(HierarchyListEntry entry) {
		return getExpandedEntryIndex(entry);
	}

	/*
	 * restrict item invalidation
	 *
	 */
	/*
	public native void startMultipleItemInvalidate();
	public native void stopMultipleItemInvalidate();
	*/
}

