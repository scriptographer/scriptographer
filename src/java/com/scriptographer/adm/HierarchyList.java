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
 * File created on 31.12.2004.
 *
 * $RCSfile: HierarchyList.java,v $
 * $Author: lehni $
 * $Revision: 1.5 $
 * $Date: 2005/10/23 00:33:04 $
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

import org.mozilla.javascript.Scriptable;

public class HierarchyList extends List {
	public final static int
	// hathaway : 8/22/02 : Added to support creation of hierarchical palette popups for Pangea
	// Popup menu creation options
	OPTION_HIERARCHY_POPUP = (1 << 0);
	
	public HierarchyList(Dialog dialog, int options) {
		super(dialog, TYPE_HIERARCHY_LISTBOX, options);
	}
	
	public HierarchyList(Dialog dialog) {
		this(dialog, 0);
	}

	private HierarchyListEntry parentEntry = null;
	
	public HierarchyList(HierarchyListEntry entry) {
		listHandle = nativeCreateChildList(entry.handle);
		// determine the parent hierarchyList:
		parentEntry = entry;
		HierarchyList parentList = (HierarchyList) entry.getList();
		// pass through the handlers automatically.
		// if desired otherwise, they need to be written over aftewards:
		this.setTrackEntryCallback(parentList.getTrackEntryCallback());
		this.setDrawEntryCallback(parentList.getDrawEntryCallback());
		this.onDrawEntry = parentList.onDrawEntry;
		this.onTrackEntry = parentList.onTrackEntry;
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

	public void setWrapper(Scriptable wrapper) {
		super.setWrapper(wrapper);
		if (parentEntry != null) {
			Scriptable parentWrapper = parentEntry.getList().getWrapper();
			if (parentWrapper != null) {
				// simply set parentWrapper as the prototype of this object so the handler calls will be delegated:
				wrapper.setPrototype(parentWrapper);
			}
		}
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
	public native void setEntryTextRect(int x, int y, int width, int height, boolean recursive);

	public void setEntrySize(Dimension size, boolean recursive) {
		setEntrySize(size.width, size.height, recursive);
	}

	public void setEntrySize(Point2D size, boolean recursive) {
		setEntrySize((int) size.getX(), (int) size.getY(), recursive);
	}
	
	public void setEntrySize(int[] size, boolean recursive) {
		setEntrySize(size[0], size[1], recursive);
	}

	public void setEntryTextRect(Rectangle2D rect, boolean recursive) {
		setEntryTextRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight(), recursive);
	}
	
	public void setEntryTextRect(int[] rect, boolean recursive) {
		setEntryTextRect(rect[0], rect[1], rect[2], rect[3], recursive);
	}

	public native int getNonLeafEntryWidth();
	public native void setNonLeafEntryTextRect(int x, int y, int width, int height, boolean recursive);
	public native Rectangle getNonLeafEntryTextRect();

	public void setNonLeafEntryTextRect(Rectangle2D rect, boolean recursive) {
		setNonLeafEntryTextRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight(), recursive);
	}

	public void setNonLeafEntryTextRect(int[] rect, boolean recursive) {
		setNonLeafEntryTextRect(rect[0], rect[1], rect[2], rect[3], recursive);
	}

	public void setNonLeafEntryTextRect(Rectangle2D rect) {
		setNonLeafEntryTextRect(rect, false);
	}

	public void setNonLeafEntryTextRect(int[] rect) {
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

	public Point localToScreen(Point2D pt) {
		return localToScreen((int) pt.getX(), (int) pt.getY());
	}

	public Point screenToLocal(Point2D pt) {
		return screenToLocal((int) pt.getX(), (int) pt.getY());
	}

	public Point localToGlobal(Point2D pt) {
		return localToScreen((int) pt.getX(), (int) pt.getY());
	}

	public Point globalToLocal(Point2D pt) {
		return screenToLocal((int) pt.getX(), (int) pt.getY());
	}

	public Rectangle localToGlobal(Rectangle2D rt) {
		return localToGlobal((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	public Rectangle globalToLocal(Rectangle2D rt) {
		return globalToLocal((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
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

	public void getLeafAt(Point2D point) {
		getLeafAt((int)point.getX(), (int)point.getY());
	}

	/*
	 * selection list manipulation
	 *
	 */

	public native HierarchyListEntry[] getAllSelected();
	public native HierarchyListEntry[] getAllUnnestedSelected();

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
