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
 * $Revision: 1.4 $
 * $Date: 2005/03/10 22:48:43 $
 */

package com.scriptographer.adm;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.*;

public abstract class Item extends CallbackHandler {

	// ADMItemType:
	protected final static String
		TYPE_DIAL = "ADM Dial Type", // wrapped
		TYPE_FRAME = "ADM Frame Type", // wrapped
		TYPE_ITEMGROUP = "ADM Item Group Type",  // wrapped
		TYPE_TABBED_MENU = "ADM Tabbed MenuItem Type",
		TYPE_LISTBOX = "ADM List Box Type", // wrapped
		TYPE_HIERARCHY_LISTBOX = "ADM Hierarchy List Box Type", // wrapped
		TYPE_PICTURE_CHECKBOX = "ADM Picture Check Box Button Type", // wrapped
		TYPE_PICTURE_PUSHBUTTON = "ADM Picture Push Button Type", // wrapped
		TYPE_PICTURE_RADIOBUTTON = "ADM Picture Radio Button Type", // wrapped
		TYPE_PICTURE_STATIC = "ADM Picture Static Type", // wrapped
		TYPE_POPUP_CONTROL = "ADM Popup Control Type",
		TYPE_POPUP_CONTROLBUTTON = "ADM Popup Control Button Type",
		TYPE_POPUP_SPINEDIT_CONTROL = "ADM Popup Spin Edit Control Type",
		TYPE_POPUP_LIST = "ADM Popup List Type",
		TYPE_POPUP_MENU = "ADM Popup MenuItem Type",
		TYPE_RESIZE = "ADM Resize Type",
		TYPE_SCROLLBAR = "ADM Scrollbar Type", // wrapped
		TYPE_SCROLLING_POPUP_LIST = "ADM Scrolling Popup List Type",
		TYPE_SLIDER = "ADM Slider Type", // wrapped
		TYPE_SPINEDIT = "ADM Spin Edit Type",
		TYPE_SPINEDIT_POPUP = "ADM Spin Edit Popup Type",
		TYPE_SPINEDIT_SCROLLING_POPUP = "ADM Spin Edit Scrolling Popup Type",
		TYPE_TEXT_CHECKBOX = "ADM Text Check Box Type", // wrapped
		TYPE_TEXT_EDIT = "ADM Text Edit Type", // wrapped
		TYPE_TEXT_EDIT_READONLY = "ADM Text Edit Read-only Type", // wrapped
		TYPE_TEXT_EDIT_MULTILINE = "ADM Text Edit Multi Line Type", // wrapped
		TYPE_TEXT_EDIT_MULTILINE_READONLY = "ADM Text Edit Multi Line Read-only Type", // wrapped
		TYPE_TEXT_EDIT_POPUP = "ADM Text Edit Popup Type", // wrapped
		TYPE_TEXT_EDIT_SCROLLING_POPUP = "ADM Text Edit Scrolling Popup Type", // wrapped
		TYPE_TEXT_EDIT_PASSWORD = "ADM Password Text Edit Type", // wrapped
		TYPE_TEXT_PUSHBUTTON = "ADM Text Push Button Type", // wrapped
		TYPE_TEXT_RADIOBUTTON = "ADM Text Radio Button Type", // wrapped
		TYPE_TEXT_STATIC = "ADM Text Static Type", // wrapped
		TYPE_TEXT_STATIC_MULTILINE = "ADM Text Static Multi Line Type", // wrapped
		TYPE_PROGRESS_BAR = "ADM Progress Bar Type", // wrapped
		TYPE_CHASING_ARROWS = "ADM Chasing Arrows Type", // wrapped
		TYPE_USER = "ADM User Type",
		TYPE_MULTICOLUMN_LISTVIEW = "ADM Multi Column List View Type",
		TYPE_SCROLLING_VIEW = "ADM Scrolling View Type",
		TYPE_TABGROUP = "ADM Tab Group Type";
	
	// TODO: move constants to their places!
	
	// ADMPopupMenuStyle
	public final static int
		POPUP_MENU_RIGHT = 0,
		POPUP_MENU_BOTTOM = 1,
		POPUP_MENU_ROUND = 2,
		POPUP_MENU_ROUND_HIERARCHY = 4;
	
	// ADMSpinEditStyle
	public final static int
		SPINEDIT_VERTICAL = 0,
		SPINEDIT_HORIZONTAL = 1;

	// ADMSpinEditPopupStyle
	public final static int
		SPINEDIT_POPUP_VERTICAL = 0,
		SPINEDIT_POPUP_HORIZONTAL = 4;
	
	/**
	 * used for storing the ADMItemRef in this object
	 */
	protected int itemRef = 0;

	protected Dialog dialog;

	protected Dimension size;
	protected Rectangle bounds;
	protected String type;
	protected int style;
	protected int options;

	private String toolTip;

	protected AWTComponent component = null;
	private Dimension minSize = null;
	private Dimension maxSize = null;

	public Item(Dialog dialog, String type, Rectangle2D bounds, int style, int options) {
		this.dialog = dialog;
		this.type = type;
		this.bounds = new Rectangle((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
		this.options = options;

		itemRef = nativeCreate(dialog, type, this.bounds, options);
		// get length:
		size = nativeGetSize();
		// length needs to be set for this, as it may cause a onResize call:
		nativeSetStyle(style);
	}

	/**
	 * needed for Spacer
	 */
	protected Item() {
	}

	public void destroy() {
		if (itemRef != 0) {
			nativeDestroy();
			itemRef = 0;
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
	 * Callback stuff:
	 */
	
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

	private native int nativeCreate(Dialog dialog, String type, Rectangle bounds, int options);
	private native void nativeDestroy();

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackCallbackEnabled(boolean enabled);
	protected native void nativeSetDrawCallbackEnabled(boolean enabled);

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

	public native void nativeSetStyle(int style);
	
	public int getStyle() {
		return style;
	}
	
	public void setStyle(int style) {
		this.style = style;
		nativeSetStyle(style);
	}

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

	public native void setLocation(int x, int y);
	public native Point getLocation();

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
	}

	public native Dimension getPreferredSize();

	public void setMinimumSize(int width, int height) {
		minSize = new Dimension(width, height);
	}

	public void setMinimumSize(Dimension size) {
		setMinimumSize(size.width, size.height);
	}

	public final void setMinimumSize(Point2D size) {
		setMinimumSize((int) size.getX(), (int) size.getY());
	}

	public Dimension getMinimumSize() {
		return minSize != null ? minSize : getSize();
	}

	public void setMaximumSize(int width, int height) {
		maxSize = new Dimension(width, height);
	}

	public void setMaximumSize(Dimension size) {
		setMaximumSize(size.width, size.height);
	}

	public final void setMaximumSize(Point2D size) {
		setMaximumSize((int) size.getX(), (int) size.getY());
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

	public final void setLocation(Point2D loc) {
		setLocation((int) loc.getX(), (int) loc.getY());
	}

	/* 
	 * coordinate system transformations
	 * 
	 */
	
	public native Point localToScreen(int x, int y);
	public native Point screenToLocal(int x, int y);

	public native Rectangle localToScreen(int x, int y, int width, int height);
	public native Rectangle screenToLocal(int x, int y, int width, int height);

	public final Point localToScreen(Point2D pt) {
		return localToScreen((int) pt.getX(), (int) pt.getY());
	}

	public final Point screenToLocal(Point2D pt) {
		return screenToLocal((int) pt.getX(), (int) pt.getY());
	}

	public final Rectangle localToScreen(Rectangle2D rt) {
		return localToScreen((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	public final Rectangle screenToLocal(Rectangle2D rt) {
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
