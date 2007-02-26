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
 * File created on 22.12.2004.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.script.ScriptMethod;

/**
 * @author lehni
 */
public abstract class Item extends CallbackHandler {

	// options
	public final static int
		OPTION_NONE = 0;

	// ADMItemType:
	protected final static int
		TYPE_DIAL = 0, // wrapped
		TYPE_FRAME = 1, // wrapped
		TYPE_ITEMGROUP = 2,  // wrapped
		TYPE_TABBED_MENU = 3,  // deprecated
		TYPE_LISTBOX = 4, // wrapped
		TYPE_HIERARCHY_LISTBOX = 5, // wrapped
		TYPE_PICTURE_CHECKBOX = 6, // wrapped
		TYPE_PICTURE_PUSHBUTTON = 7, // wrapped
		TYPE_PICTURE_RADIOBUTTON = 8, // wrapped
		TYPE_PICTURE_STATIC = 9, // wrapped
		TYPE_POPUP_CONTROL = 10,
		TYPE_POPUP_CONTROLBUTTON = 11,
		TYPE_POPUP_SPINEDIT_CONTROL = 12,
		TYPE_POPUP_LIST = 13, // wrapped
		TYPE_POPUP_MENU = 14, // wrapped
		TYPE_RESIZE = 15, // wrapped (dialog.getResizeButton() Button)
		TYPE_SCROLLBAR = 16, // wrapped
		TYPE_SCROLLING_POPUP_LIST = 17, // wrapped
		TYPE_SLIDER = 18, // wrapped
		TYPE_SPINEDIT = 19, // wrapped
		TYPE_SPINEDIT_POPUP = 20, // wrapped
		TYPE_SPINEDIT_SCROLLING_POPUP = 21, // wrapped
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
		"ADM Tabbed Menu Type", // deprecated
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

	// hashmap for conversation to unique ids that can be compared with ==
	// instead of .equals
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
	protected int options;

	protected Dialog dialog;

	protected Rectangle nativeBounds = null;
	protected Rectangle bounds;
	protected Insets insets;

	private String toolTip;

	protected AWTComponent component = null;
	private Dimension minSize = null;
	private Dimension maxSize = null;
	private Dimension prefSize = null;

	protected Item() {
		// Call function as it is overriden by Button, where it sets 
		// insets according to platform
		setInsets(0, 0, 0, 0);
	}
	
	/**
	 * Constructor for newly created Items
	 * 
	 * @param dialog
	 * @param type
	 * @param options
	 */
	protected Item(Dialog dialog, int type, int options) {
		this();
		this.handle = nativeCreate(dialog.handle, convertType(type), options);
		this.dialog = dialog;
		this.type = type;
		this.options = options;
		initBounds();
	}
	
	/**
	 * Constructor for allready existing Items that get wrapped,
	 * e.g. PopupMenu 
	 * 
	 * @param dialog
	 * @param handle
	 */
	protected Item(Dialog dialog, long handle) {
		this();
		this.handle = (int) handle;
		this.dialog = dialog;
		this.type = convertType(nativeInit(this.handle));
		this.options = 0;
		initBounds();
	}
	
	protected void initBounds() {
		nativeBounds = nativeGetBounds();
		// nativeSize and nativeBounds are set by the native environment
		// size and bounds need to be updated depending on insets and
		// internalInsets
		bounds = new Rectangle(
			nativeBounds.x - insets.left,
			nativeBounds.y - insets.top,
			nativeBounds.width + insets.left + insets.right,
			nativeBounds.height + insets.top + insets.bottom
		);
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
	
	/*
	 * Callback functions:
	 */

	private ScriptMethod onDestroy = null;
	
	public ScriptMethod getOnDestroy() {
		return onDestroy;
	}

	public void setOnDestroy(ScriptMethod onDestroy) {
		this.onDestroy = onDestroy;
	}

	protected void onDestroy() throws Exception {
		// retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		ScriptMethod onDestroy = this.getOnDestroy();
		if (onDestroy != null)
			onDestroy.execute(this);
	}

	protected void onNotify(int notifier) throws Exception {
		if (notifier == Notifier.NOTIFIER_DESTROY)
			onDestroy();
	}
	
	/*
	 * ADM stuff:
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
	
	protected native int getChildItemHandle(int itemID);

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

	private native Rectangle nativeGetBounds();
	private native void nativeSetBounds(int x, int y, int width, int height);

	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	protected void updateBounds(int x, int y, int width, int height) {
		// calculate native values
		int nativeX = x + insets.left;
		int nativeY = y + insets.top;
		int nativeWidth = width - insets.left - insets.right;
		int nativeHeight = height - insets.top - insets.bottom;
		int deltaX = nativeWidth - nativeBounds.width;
		int deltaY = nativeHeight - nativeBounds.height;

		boolean sizeChanged = deltaX != 0 || deltaY != 0;
		if (sizeChanged || nativeBounds.x != nativeX ||
				nativeBounds.y != nativeY) {
			nativeSetBounds(nativeX, nativeY, nativeWidth, nativeHeight);
			nativeBounds.setBounds(nativeX, nativeY, nativeWidth, nativeHeight);
		}

		// update bounds
		bounds.setBounds(x, y, width, height);
		if (component != null)
			component.updateBounds(bounds);

		if (sizeChanged) {
			// TODO: deal with Exception...
			try {
				onResize(deltaX, deltaY);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setBounds(int x, int y, int width, int height) {
		// Set prefSize so getPreferredSize does not return results from
		// getBestSize()
		prefSize = new Dimension(width, height);
		// updateBounds does all the heavy lifting, except for setting
		// prefSize, which shouldnt be set when changing location or insets.
		updateBounds(x, y, width, height);
	}

	public final void setBounds(Rectangle2D bounds) {
		setBounds((int) bounds.getX(), (int) bounds.getY(),
				(int) bounds.getWidth(), (int) bounds.getHeight());
	}

	public void setLocation(int x, int y) {
		updateBounds(x, y, bounds.width, bounds.height);
	}

	public final void setLocation(Point2D loc) {
		setLocation((int) loc.getX(), (int) loc.getY());
	}
	
	public Point getLocation() {
		return new Point(bounds.x, bounds.y);
	}

	public Dimension getSize() {
		return bounds.getSize();
	}

	public void setSize(int width, int height) {
		setBounds(bounds.x, bounds.y, width, height);
	}

	public final void setSize(Dimension size) {
		setSize(size.width, size.height);
	}

	public final void setSize(Point2D size) {
		setSize((int) size.getX(), (int) size.getY());
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
		Dimension size = null;
		switch (type) {
			case TYPE_PICTURE_STATIC:
			case TYPE_PICTURE_CHECKBOX:
			case TYPE_PICTURE_PUSHBUTTON:
			case TYPE_PICTURE_RADIOBUTTON:
				size = nativeGetBestSize();
			default:
				String text = null;
				if (this instanceof TextValueItem)
					text = ((TextValueItem) this).getText();
				else if (this instanceof TextItem)
					text = ((TextItem) this).getText();
			
				if (text != null && text.length() > 0) {
					size = getTextSize(text, -1);
					if (size != null) {
						size.height += 6;
						if (this instanceof Button) {
							size.width += 32;
						} else if (this instanceof ToggleItem) {
							size.width += 32;
						} else {
							size.width +=
								ScriptographerEngine.isMacintosh() ? 12 : 6;
						}
					}
				}
		}
		if (size == null) size = new Dimension(120, 20);
		// add insets
		size.width += insets.left + insets.right;
		size.height += insets.top + insets.bottom;
		return size;
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
		// return prefSize != null ? prefSize : getBestSize();
		Dimension size = prefSize != null ? prefSize : getBestSize();
// 		System.out.println("Item.getPreferredSize " + desc() + " " + size);
		return size;
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
	
	public void setInsets(int left, int top, int right, int bottom) {
		insets = new Insets(top, left, bottom, right);
		if (nativeBounds != null)
			updateBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public Insets getInsets() {
		return new Insets(insets.top, insets.left, insets.bottom, insets.right);
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
		return localToScreen((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
	}

	public Rectangle screenToLocal(Rectangle2D rt) {
		return screenToLocal((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
	}

	/* 
	 * item display
	 * 
	 */

	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public final void invalidate(Rectangle2D rt) {
		invalidate((int) rt.getX(), (int) rt.getY(),
				(int) rt.getWidth(), (int) rt.getHeight());
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
			// call the setBounds version in super that directly sets the
			// internal segmentValues.setBounds(Rectangle) would call the
			// overriden setBounds(int, int, int, int) which would change the
			// underlying Item.
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
			Item.this.setBounds(getBounds());
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
