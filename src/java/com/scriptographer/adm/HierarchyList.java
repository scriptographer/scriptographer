/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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

package com.scriptographer.adm;

import com.scratchdisk.script.Callable;

/**
 * @author lehni
 */
public class HierarchyList extends List {
	public final static int
	// hathaway : 8/22/02 : Added to support creation of hierarchical palette
	// popups for Pangea Popup menu creation options
		OPTION_HIERARCHY_POPUP = (1 << 0);
	
	public HierarchyList(Dialog dialog, int options) {
		super(dialog, TYPE_HIERARCHY_LISTBOX, options);
	}
	
	public HierarchyList(Dialog dialog) {
		this(dialog, OPTION_NONE);
	}

	private HierarchyListEntry parentEntry = null;
	
	public HierarchyList(HierarchyListEntry entry) {
		listHandle = nativeCreateChildList(entry.handle);
		// determine the parent hierarchyList:
		parentEntry = entry;
		parentEntry.childList = this;
		HierarchyList parentList = (HierarchyList) entry.getList();
		// Pass through track / draw callback settings
		// This requires the callback to be set before child lists are created
		// as otherwise only the parent list would recieve callbacks!
		this.setTrackEntryCallback(parentList.getTrackEntryCallback());
		this.setDrawEntryCallback(parentList.getDrawEntryCallback());
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
	
	private native int nativeCreateChildList(int entryHandle);
	private native HierarchyListEntry nativeRemoveList(int listHandle);

	/*
	 * Override all getters for ScriptFunctions in HierarchyLists, so they
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

	public native HierarchyListEntry[] getLeafs();

	public native int getLeafIndex(HierarchyListEntry entry);

	/*
	 * item list manipulation
	 *
	 */
	
	protected ListEntry createEntry(int index) {
		return new HierarchyListEntry(this, index);
	}

	public native HierarchyListEntry getLeafAt(int x, int y);

	public native HierarchyListEntry getActiveLeaf();

	public void getLeafAt(Point point) {
		getLeafAt(point.x, point.y);
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

	public native HierarchyListEntry[] getExpanded();

	public native int getExpandedIndex(HierarchyListEntry entry);

	/*
	 * restrict item invalidation
	 *
	 */
	/*
	public native void startMultipleItemInvalidate();
	public native void stopMultipleItemInvalidate();
	*/
}
