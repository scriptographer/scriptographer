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
 * File created on 22.12.2004.
 *
 * $RCSfile: Item.java,v $
 * $Author: lehni $
 * $Revision: 1.9 $
 * $Date: 2005/07/22 17:39:22 $
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.scriptographer.ScriptographerEngine;

public abstract class Item extends CallbackHandler {
	// TODO: move constants to their places!
	
	// ADMSpinEditStyle
	public final static int
		SPINEDIT_VERTICAL = 0,
		SPINEDIT_HORIZONTAL = 1;

	// ADMSpinEditPopupStyle
	public final static int
		SPINEDIT_POPUP_VERTICAL = 0,
		SPINEDIT_POPUP_HORIZONTAL = 4;
	
	// options
	public final static int
		OPTION_NONE = 0;

	// ADMItemType:
	protected final static int
		TYPE_DIAL = 0, // wrapped
		TYPE_FRAME = 1, // wrapped
		TYPE_ITEMGROUP = 2,  // wrapped
		TYPE_TABBED_MENU = 3,
		TYPE_LISTBOX = 4, // wrapped
		TYPE_HIERARCHY_LISTBOX = 5, // wrapped
		TYPE_PICTURE_CHECKBOX = 6, // wrapped
		TYPE_PICTURE_PUSHBUTTON = 7, // wrapped
		TYPE_PICTURE_RADIOBUTTON = 8, // wrapped
		TYPE_PICTURE_STATIC = 9, // wrapped
		TYPE_POPUP_CONTROL = 10,
		TYPE_POPUP_CONTROLBUTTON = 11,
		TYPE_POPUP_SPINEDIT_CONTROL = 12,
		TYPE_POPUP_LIST = 13,
		TYPE_POPUP_MENU = 14, // wrapped
		TYPE_RESIZE = 15,
		TYPE_SCROLLBAR = 16, // wrapped
		TYPE_SCROLLING_POPUP_LIST = 17,
		TYPE_SLIDER = 18, // wrapped
		TYPE_SPINEDIT = 19,
		TYPE_SPINEDIT_POPUP = 20,
		TYPE_SPINEDIT_SCROLLING_POPUP = 21,
		TYPE_TEXT_CHECKBOX = 22, // wrapped
		TYPE_TEXT_EDIT = 23, // wrapped
		TYPE_TEXT_EDIT_READONLY = 24, // wrapped
		TYPE_TEXT_EDIT_MULTILINE = 25, // wrapped
		TYPE_TEXT_EDIT_MULTILINE_READONLY = 26, // wrapped
		TYPE_TEXT_EDIT_POPUP = 27, // wrapped
		TYPE_TEXT_EDIT_SCROLLING_POPUP = 28, // wrapped
		TYPE_TEXT_EDIT_PASSWORD = 29, // wrapped
		TYPE_TEXT_PUSHBUTTON = 30, // wrapped
		TYPE_TEXT_RADIOBUTTON = 31, // wrapped
		TYPE_TEXT_STATIC = 32, // wrapped
		TYPE_TEXT_STATIC_MULTILINE = 33, // wrapped
		TYPE_PROGRESS_BAR = 34, // wrapped
		TYPE_CHASING_ARROWS = 35, // wrapped
		TYPE_USER = 36,
		TYPE_MULTICOLUMN_LISTVIEW = 37,
		TYPE_SCROLLING_VIEW = 38,
		TYPE_TABGROUP = 39;
	
	protected static final String[] itemTypes = {
		"ADM Dial Type",
		"ADM Frame Type",
		"ADM Item Group Type",
		"ADM Tabbed Menu Type",
		"ADM List Box Type",
		"ADM Hierarchy List Box Type",
		"ADM Picture Check Box Button Type",
		"ADM Picture Push Button Type",
		"ADM Picture Radio Button Type",
		"ADM Picture Static Type",
		"ADM Popup Control Type",
		"ADM Popup Control Button Type",
		"ADM Popup Spin Edit Control Type",
		"ADM Popup List Type",
		"ADM Popup Menu Type",
		"ADM Resize Type",
		"ADM Scrollbar Type",
		"ADM Scrolling Popup List Type",
		"ADM Slider Type",
		"ADM Spin Edit Type",
		"ADM Spin Edit Popup Type",
		"ADM Spin Edit Scrolling Popup Type",
		"ADM Text Check Box Type",
		"ADM Text Edit Type",
		"ADM Text Edit Read-only Type",
		"ADM Text Edit Multi Line Type",
		"ADM Text Edit Multi Line Read-only Type",
		"ADM Text Edit Popup Type",
		"ADM Text Edit Scrolling Popup Type",
		"ADM Password Text Edit Type",
		"ADM Text Push Button Type",
		"ADM Text Radio Button Type",
		"ADM Text Static Type",
		"ADM Text Static Multi Line Type",
		"ADM Progress Bar Type",
		"ADM Chasing Arrows Type",
		"ADM User Type",
		"ADM Multi Column List View Type",
		"ADM Scrolling View Type",
		"ADM Tab Group Type"
	};

	// hashmap for conversation to unique ids that can be compared with == instead of .equals
	private static HashMap types = new HashMap();
	
	static {
		for (int i = 0; i < itemTypes.length; i++) {
			types.put(itemTypes[i], new Integer(i));
		}
	}
	
	protected static int convertType(String type) {
		Integer value = (Integer) types.get(type);
		if (value != null)
			return value.intValue();
		else return -1;
	}
	
	protected static String convertType(int type) {
		return itemTypes[type];
	}
	
	/**
	 * used for storing the ADMItemRef in this object
	 */
	protected int type;

	protected Dialog dialog;

	protected Dimension size = null;
	protected Rectangle bounds = null;

	private String toolTip;

	protected AWTComponent component = null;
	private Dimension minSize = null;
	private Dimension maxSize = null;
	private Dimension prefSize = null;

	/**
	 * Constructor for newly created Items
	 * 
	 * @param dialog
	 * @param type
	 * @param options
	 */
	protected Item(Dialog dialog, int type, int options) {
		this.dialog = dialog;
		this.type = type;
		handle = nativeCreate(dialog.handle, convertType(type), options);
	}
	
	/**
	 * Constructor for allready existing Items that get wrapped,
	 * e.g. PopupMenu 
	 * 
	 * @param dialog
	 * @param handle
	 */
	protected Item(Dialog dialog, int handle) {
		this.dialog = dialog;
		this.handle = handle;
		this.type = convertType(nativeInit(handle));
	}

	/**
	 * needed for Spacer
	 */
	protected Item() {
	}

	public void destroy() {
		if (handle != 0) {
			nativeDestroy(handle);
			handle = 0;
			dialog.removeItem(this);
			dialog = null;
		}
	}
	
	public Dialog getDialog() {
		return dialog;
	}
	
	protected AWTComponent getComponent() {
		if (component == null)
			component = new AWTComponent();
		return component;
	}
	
	protected void onResize(int dx, int dy) throws Exception {
		if (component != null) {
			component.updateBounds(getBounds());
		}
		super.onResize(dx, dy);
	}
	
	/*
	 * Callback functions:
	 */

	protected void onDestroy() throws Exception {
		callFunction("onDestroy");
	}

	protected void onNotify(int notifier) throws Exception {
		if (notifier == Notifier.NOTIFIER_DESTROY)
			onDestroy();
	}
	
	/*
	 * ADM stuff:
	 */
	
	/* TODO: Check these:
	 * - getChildItem
	 */

	/*
	 * item creation/destruction
	 * 
	 */

	/**
	 * sets size and bounds to valid values
	 */
	private native int nativeCreate(int dialogHandle, String type, int options);
	
	/**
	 * sets size and bounds to valid values
	 */
	private native String nativeInit(int handle);
	
	private native void nativeDestroy(int handle);

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackCallback(boolean enabled);
	protected native void nativeSetDrawCallback(boolean enabled);

	public native int getTrackMask();
	public native void setTrackMask(int mask);

	/* 
	 * item timer
	 * 
	 */
	/*
	public native ADMTimerRef createTimer(ADMUInt32 inMilliseconds,
				ADMActionMask inAbortMask, ADMItemTimerProc inTimerProc,
				ADMItemTimerAbortProc inTimerAbortProc, ADMInt32 inOptions);

	public native void abortTimer(ADMTimerRef inTimer);
	*/

	/*
	 * item information accessors
	 * 
	 */
	
	protected int getType() {
		return type;
	}

	public native void setStyle(int style);
	public native int getStyle();

	/* 
	 * item state accessors
	 * 
	 */
	
	public native boolean isVisible();
	public native void setVisible(boolean visible);

	public native boolean isEnabled();
	public native void setEnabled(boolean enabled);
	
	public native boolean isActive();
	public native void setActive(boolean active);

	public native boolean isKnown();
	public native void setKnown(boolean known);
	
	/*
	 * others...
	 * 
	 */
	
	public native boolean wantsFocus();
	public native void setWantsFocus(boolean wantsFocus);

	/* 
	 * item bounds accessors
	 * 
	 */

	private native Dimension nativeGetSize();
	private native void nativeSetSize(int width, int height);

	private native Rectangle nativeGetBounds();
	private native void nativeSetBounds(int x, int y, int width, int height);

	public void setLocation(int x, int y) {
		setBounds(x, y, bounds.width, bounds.height);
	}

	public final void setLocation(Point2D loc) {
		setLocation((int) loc.getX(), (int) loc.getY());
	}
	
	public Point getLocation() {
		return new Point(bounds.x, bounds.y);
	}

	public Dimension getSize() {
		return new Dimension(size);
	}

	public void setSize(int width, int height) {
		if (size.width != width || size.height != height) {
			size.setSize(width, height);
			nativeSetSize(width, height);
			// also updatePoint the internal bounds field:
			bounds = nativeGetBounds();
			if (component != null) {
				component.updateBounds(bounds);
			}
		}
	}

	public final void setSize(Dimension size) {
		setSize(size.width, size.height);
	}

	public final void setSize(Point2D size) {
		setSize((int) size.getX(), (int) size.getY());
		// Set prefSize so getPreferredSize does not return results from getBestSize()
		prefSize = this.size;
	}

	private native Dimension nativeGetTextSize(String text, int maxWidth);
	
	public Dimension getTextSize(String text, int maxWidth) {
		String delim = System.getProperty("line.separator");
		StringTokenizer st = new StringTokenizer(text, delim, true);
		Dimension size = new Dimension(0, 0);
		boolean isDelim = false;
		while (st.hasMoreTokens()) {
			// detect several newlines in a row, and use a " " instead, so
			// we get the height of the line:
			boolean wasDelim = isDelim;
			text = st.nextToken();
			isDelim = delim.indexOf(text) != -1; 
			if (isDelim) {
				if (text.charAt(0) == delim.charAt(0)) {
					if (wasDelim) text = " ";
					else continue;
				} else continue;
			}
			Dimension partSize = nativeGetTextSize(text, maxWidth);
			if (partSize.width > size.width)
				size.width = partSize.width;
			size.height += partSize.height;
		}
		return size;
	}

	private native Dimension nativeGetBestSize();

	public Dimension getBestSize() {
		// TODO: verify for which items getBestSize really works!
		switch (type) {
//			case TYPE_TEXT_STATIC:
//			case TYPE_TEXT_STATIC_MULTILINE:
			case TYPE_PICTURE_STATIC:
			case TYPE_PICTURE_CHECKBOX:
			case TYPE_PICTURE_PUSHBUTTON:
			case TYPE_PICTURE_RADIOBUTTON:
				return nativeGetBestSize();
			default:
				if (this instanceof TextItem) {
					String text = ((TextItem) this).getText();
					if (text != null && text.length() > 0) {
						Dimension size = getTextSize(text, -1);
						if (size != null) {
							size.height += 6;
							if (this instanceof Button) {
								size.width += 20;
							} else if (this instanceof ToggleItem) {
								size.width += 32;
							} else {
								size.width += ScriptographerEngine.isMacintosh() ? 12 : 6;
							}
							return size;
						}
					}
				}
		}
		return new Dimension(120, 20);
	}

	public void setPreferredSize(int width, int height) {
		prefSize = new Dimension(width, height);
	}

	public void setPreferredSize(Dimension size) {
		if (size == null) prefSize = null;
		else setPreferredSize(size.width, size.height);
	}

	public final void setPreferredSize(Point2D size) {
		if (size == null) prefSize = null;
		else setPreferredSize((int) size.getX(), (int) size.getY());
	}

	public Dimension getPreferredSize() {
		return prefSize != null ? minSize : getBestSize();
	}

	public void setMinimumSize(int width, int height) {
		minSize = new Dimension(width, height);
	}

	public void setMinimumSize(Dimension size) {
		if (size == null) minSize = null;
		else setMinimumSize(size.width, size.height);
	}

	public final void setMinimumSize(Point2D size) {
		if (size == null) minSize = null;
		else setMinimumSize((int) size.getX(), (int) size.getY());
	}

	public Dimension getMinimumSize() {
		return minSize != null ? minSize : getSize();
	}

	public void setMaximumSize(int width, int height) {
		maxSize = new Dimension(width, height);
	}

	public void setMaximumSize(Dimension size) {
		if (size == null) maxSize = null;
		else setMaximumSize(size.width, size.height);
	}

	public final void setMaximumSize(Point2D size) {
		if (size == null) maxSize = null;
		else setMaximumSize((int) size.getX(), (int) size.getY());
	}

	public Dimension getMaximumSize() {
		return maxSize != null ? maxSize : getSize();
	}

	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	public void setBounds(int x, int y, int width, int height) {
		boolean sizeChanged = (bounds.width != width || bounds.height != height);
		if (sizeChanged || bounds.x != x || bounds.y != y) {
			bounds.setBounds(x, y, width, height);
			nativeSetBounds(x, y, width, height);
			if (sizeChanged) {
				// also updatePoint the internal length field:
				size = nativeGetSize();
				if (component != null) {
					component.updateBounds(bounds);
				}
			}
		}
	}

	public final void setBounds(Rectangle2D bounds) {
		setBounds((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
	}

	/* 
	 * coordinate system transformations
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
		return localToScreen((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	public Rectangle screenToLocal(Rectangle2D rt) {
		return screenToLocal((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	/* 
	 * item display
	 * 
	 */

	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public final void invalidate(Rectangle2D rt) {
		invalidate((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	public native int getFont();
	public native void setFont(int font);

	public native void setBackgroundColor(int color); // Drawer.COLOR_*
	public native int getBackgroundColor();

	/* 
	 * cursor ID accessors
	 * 
	 */
	
	public native int getCursor();
	public native void setCursor(int cursor);
	
	/*
	 * tooltips
	 * 
	 */
	
	private native void nativeSetTooltip(String tooltip);
	public native boolean isToolTipEnabled();
	public native void setToolTipEnabled(boolean enabled);
	public native void showToolTip(int x, int y);
	public native void hideToolTip();

	public final void showToolTip(Point2D point) {
		showToolTip((int) point.getX(), (int) point.getY());
	}

	public String getToolTip() {
		return toolTip;
	}
	
	public void setToolTip(String tooltip) {
		this.toolTip = tooltip;
		nativeSetTooltip(tooltip);
	}

	/**
	 * AWTComponent wrapps an ADM Item and prentends it is a AWT Component, in
	 * order to take advantage of all the nice LayoutManagers in AWT.
	 */
	class AWTComponent extends Component {
		public AWTComponent() {
			updateBounds(Item.this.getBounds());
		}

		public void doLayout() {
			// do nothing here...
		}

		public void updateBounds(Rectangle bounds) {
			// call the setBounds maxVersion in super that directly sets the internal segmentValues.
			// setBounds(Rectangle) would call the overriden setBounds(int, int, int, int)
			super.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		public Dimension getMaximumSize() {
			return Item.this.getMaximumSize();
		}

		public Dimension getMinimumSize() {
			return Item.this.getMinimumSize();
		}

		public Dimension getPreferredSize() {
			return Item.this.getPreferredSize();
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Point origin = getOrigin();
			Item.this.setBounds(x + origin.x, y + origin.y, width, height);
		}

		public void setBounds(Rectangle r) {
			setBounds(r.x, r.y, r.width, r.height);
		}

		protected Point getOrigin() {
			Point delta = new Point();
			Container parent = getParent();
			while (true) {
				Container next = parent.getParent();
				if (next == null)
					break;
				Point loc = parent.getLocation();
				delta.x += loc.x;
				delta.y += loc.y;
				parent = next;
			}
			return delta;
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			Rectangle bounds = getBounds();
			bounds.setSize(width, height);
			Item.this.setBounds(bounds);
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void setLocation(int x, int y) {
			super.setLocation(x, y);
			Point origin = getOrigin();
			Item.this.setLocation(x + origin.x, y + origin.y);
		}

		public void setLocation(Point p) {
			setLocation(p.x, p.y);
		}

		public boolean isVisible() {
			return Item.this.isVisible();
		}
	}
}
