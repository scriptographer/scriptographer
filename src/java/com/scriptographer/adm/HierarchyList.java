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
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:35:07 $
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

import org.mozilla.javascript.Scriptable;

public class HierarchyList extends List {
	private HierarchyList parent;

	protected HierarchyList(int listRef) {
		super(listRef);
		// determine the parent bezierList:
		HierarchyListEntry parentEntry = (HierarchyListEntry) getParentEntry();
		if (parentEntry != null) {
			parent = (HierarchyList) parentEntry.getList();
			// pass through the handlers automatically. of desired otherwise, they have to be written over aftewards:
			this.setOnTrack(parent.getOnTrack());
			this.setOnDraw(parent.getOnDraw());
		} else {
			parent = null;
		}
	}

	public void setWrapper(Scriptable wrapper) {
		super.setWrapper(wrapper);
		if (parent != null) {
			Scriptable parentWrapper = parent.getWrapper();
			if (parentWrapper != null) {
				// simply set parentWrapper as the prototype of this object the handler calls will be delegated:
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
	 * item bezierList manipulation
	 *
	 */
	
	public native ListEntry getLeafEntry(int x, int y);
	public native ListEntry getActiveLeafEntry();

	public void getLeafEntry(Point2D point) {
		getLeafEntry((int)point.getX(), (int)point.getY());
	}

	/*
	 * selection bezierList manipulation
	 *
	 */

	public native ListEntry[] getAllSelectedEntries();
	public native ListEntry[] getAllUnnestedSelectedEntries();
		
	/*
	 * item hierarchy
	 *
	 */
	
	public native ListEntry getParentEntry();

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

	public native ListEntry[] getLeafEntries();
	public native int getLeafIndex(ListEntry entry);

	/*
	 * item sequence manipulation
	 *
	 */
	
	public native void swapEntries(int fromIndex, int toIndex);
	public native ListEntry insertEntry(ListEntry entry, int index);
	public native ListEntry unlinkEntry(int index);
	
	/*
	 * item selection
	 *
	 */

	public native void deselectAll();
	
	/*
	 * expanded item accessors
	 *
	 */

	public native ListEntry[] getExpandedEntries();
	public native int getExpandedIndex(ListEntry entry);

	/*
	 * restrict item invalidation
	 *
	 */
	/*
	public native void startMultipleItemInvalidate();
	public native void stopMultipleItemInvalidate();
	*/
}
