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
 * $Id$
 */

package com.scriptographer.adm;

import java.io.IOException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.mozilla.javascript.ScriptRuntime;

import com.scriptographer.js.FunctionHelper;
import com.scriptographer.js.Unsealed;

/**
 * @author lehni
 */
public class ListEntry extends NotificationHandler implements Unsealed {
	private Image image;
	private Image selectedImage;
	private Image disabledImage;

	protected ListItem list;
	
	public ListEntry(ListItem list, int index) {
		if (!(this instanceof HierarchyListEntry)
				&& list instanceof HierarchyList)
			throw new RuntimeException(
					"Use HierarchyListEntry objects for HierarchyList");
		handle = nativeCreate(list, index, list.getUniqueId());
		if (handle == 0)
			throw new RuntimeException("Cannot create list entry");
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

	protected void onDraw(Drawer drawer) throws Exception {
		list.callFunction(list.onDrawEntry, new Object[] { drawer, this });
	}

	protected boolean onTrack(Tracker tracker) throws Exception {
		Object result = list.callFunction(list.onTrackEntry,
				new Object[] { tracker, this });
		if (result != null)
			return ScriptRuntime.toBoolean(result);
		return true;
	}

	protected void onDestroy() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onDestroy");
		list.callFunction("onDestroyEntry", new Object[] { this });
	}

	protected void onClick() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onClick");
		list.callFunction("onClickEntry", new Object[] { this });
	}
	
	protected void onChangeText() throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, "onChangeText");
		list.callFunction("onChangeEntryText", new Object[] { this });
	}
	
	protected void onNotify(int notifier) throws Exception {
		switch (notifier) {
			case Notifier.NOTIFIER_DESTROY:
				onDestroy();
			break;
			case Notifier.NOTIFIER_USER_CHANGED:
				onClick();
				break;
			case Notifier.NOTIFIER_ENTRY_TEXT_CHANGED:
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

	public Point localToScreen(Point2D pt) {
		return localToScreen((int) pt.getX(), (int) pt.getY());
	}

	public Point screenToLocal(Point2D pt) {
		return screenToLocal((int) pt.getX(), (int) pt.getY());
	}

	public Rectangle localToScreen(Rectangle2D rt) {
		return localToScreen((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
	}

	public Rectangle screenToLocal(Rectangle2D rt) {
		return screenToLocal((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
	}

	/* 
	 * redraw requests
	 * 
	 */
		
	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public void invalidate(Rectangle2D rt) {
		invalidate((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
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
