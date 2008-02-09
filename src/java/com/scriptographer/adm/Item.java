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
 * $Id:Item.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.adm;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine; 

/**
 * @author lehni
 */
public abstract class Item extends Component {

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
	protected Border margin;

	private String toolTip;

	protected java.awt.Component component = null;
	private Size minSize = null;
	private Size maxSize = null;
	private Size prefSize = null;

	protected Item() {
		// Call function as it is overridden by Button, where it sets 
		// margin according to platform
		setMargin(0, 0, 0, 0);
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
		// size and bounds need to be updated depending on margins and
		// internalInsets
		bounds = new Rectangle(nativeBounds).add(margin);
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
	
	/*
	 * Callback functions:
	 */

	private Callable onInitialize = null;

	public Callable getOnInitialize() {
		return onInitialize;
	}

	public void setOnInitialize(Callable onInitialize) {
		this.onInitialize = onInitialize;
	}

	protected void onInitialize() throws Exception {
		if (onInitialize != null)
			ScriptographerEngine.invoke(onInitialize, this);
	}

	private Callable onDestroy = null;
	
	public Callable getOnDestroy() {
		return onDestroy;
	}

	public void setOnDestroy(Callable onDestroy) {
		this.onDestroy = onDestroy;
	}

	protected void onDestroy() throws Exception {
		// retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onDestroy = this.getOnDestroy();
		if (onDestroy != null)
			ScriptographerEngine.invoke(onDestroy, this);
	}

	protected void onNotify(int notifier) throws Exception {
		switch (notifier) {
		case Notifier.NOTIFIER_INITIALIZE:
			onInitialize();
			break;
		case Notifier.NOTIFIER_DESTROY:
			onDestroy();
			break;
		}
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
		int nativeX = x + margin.left;
		int nativeY = y + margin.top;
		int nativeWidth = width - margin.left - margin.right;
		int nativeHeight = height - margin.top - margin.bottom;
		int deltaX = nativeWidth - nativeBounds.width;
		int deltaY = nativeHeight - nativeBounds.height;

		boolean sizeChanged = deltaX != 0 || deltaY != 0;
		if (sizeChanged || nativeBounds.x != nativeX ||
				nativeBounds.y != nativeY) {
			nativeSetBounds(nativeX, nativeY, nativeWidth, nativeHeight);
			nativeBounds.set(nativeX, nativeY, nativeWidth, nativeHeight);
		}

		// Update bounds
		bounds.set(x, y, width, height);

		// Update bounds in AWT proxy:
		updateAWTBounds(bounds);

		if (sizeChanged) {
			try {
				onResize(deltaX, deltaY);
			} catch (Exception e) {
				// TODO: deal with Exception...
				throw new RuntimeException(e);
			}
		}
	}

	protected void updateAWTBounds(Rectangle bounds) {
		if (component != null) {
			if (component instanceof AWTItemComponent)
				((AWTItemComponent) component).updateBounds(bounds);
			else if (component instanceof AWTItemContainer)
				((AWTItemContainer) component).updateBounds(bounds);
		}
	}

	protected void updateAWTMargin(Border margin) {
		if (component != null && this instanceof ComponentGroup)
			this.getAWTContainer().setInsets(margin.top, margin.left,
					margin.bottom, margin.right);
	}

	public void setBounds(int x, int y, int width, int height) {
		// Set prefSize so getPreferredSize does not return results from
		// getBestSize()
		prefSize = new Size(width, height);
		// Set minSize if it is not set yet, so getBestSize() is not used
		// anymore.
		if (minSize == null)
			minSize = prefSize;
		// updateBounds does all the heavy lifting, except for setting
		// prefSize, which shouldn't be set when changing location or margins.
		updateBounds(x, y, width, height);
	}

	public final void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public void setBounds(int[] bounds) {
		setBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
	}

	public void setPosition(int x, int y) {
		updateBounds(x, y, bounds.width, bounds.height);
	}

	public final void setPosition(Point loc) {
		setPosition(loc.x, loc.y);
	}

	public void setPosition(int[] point) {
		setPosition(point[0], point[1]);
	}
	
	public Point getPosition() {
		return new Point(bounds.x, bounds.y);
	}

	public Size getSize() {
		return bounds.getSize();
	}

	public void setSize(int width, int height) {
		setBounds(bounds.x, bounds.y, width, height);
	}

	public final void setSize(Size size) {
		setSize(size.width, size.height);
	}

	public final void setSize(Point size) {
		setSize(size.x, size.y);
	}

	public void setSize(int[] size) {
		setSize(size[0], size[1]);
	}

	private native Size nativeGetTextSize(String text, int maxWidth);
	
	public Size getTextSize(String text, int maxWidth) {
		String delim = System.getProperty("line.separator");
		StringTokenizer st = new StringTokenizer(text, delim, true);
		Size size = new Size(0, 0);
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
			Size partSize = nativeGetTextSize(text, maxWidth);
			if (partSize.width > size.width)
				size.width = partSize.width;
			size.height += partSize.height;
		}
		return size;
	}

	public Size getBestSize() {
		// TODO: verify for which items nativeGetBestSize really works!
		Size size = null;
		switch (type) {
			case TYPE_PICTURE_STATIC:
			case TYPE_PICTURE_CHECKBOX:
			case TYPE_PICTURE_PUSHBUTTON:
			case TYPE_PICTURE_RADIOBUTTON:
				Image image = null;
				if (this instanceof ImageStatic)
					image = ((ImageStatic) this).getImage();
				else if (this instanceof Button)
					image = ((Button) this).getImage();
				if (image != null)
					size = image.getSize();
				break;
			case TYPE_POPUP_LIST:
				PopupList list = (PopupList) this;
				if (list.size() > 0) {
					size = new Size(0, 0);
					for (int i = 0, l = list.size(); i < l; i++) {
						ListEntry entry = (ListEntry) list.get(i);
						String text = entry.getText();
						Size entrySize = getTextSize(text, -1);
						size.width = Math.max(size.width, entrySize.width);
						size.height = Math.max(size.height, entrySize.height);
					}
				} else {
					// Empty list, make sure height is at least set
					size = getTextSize(" ", -1);
				}
				// 38 is a mac specific value, defined by the size
				// of pulldown menu interface elements.
				// TODO: Check on windows!
				size.width += size.height >= 16 ? 38 : 32;
				size.height += 8;
				break;
			default:
				String text = null;
				if (this instanceof TextValueItem)
					text = ((TextValueItem) this).getText();
				else if (this instanceof TextItem)
					text = ((TextItem) this).getText();
				if (text != null) {
					if (text.equals(""))
						text = " ";
					size = getTextSize(text, -1);
					if (size != null) {
						if (this instanceof Button) {
							size.width += size.height * 2;
							size.height += 6;
						} else if (this instanceof TextEdit) {
							// Ignore the text width for a TextEdit,
							// just use the text height and use a
							// default width across Scriptographer.
							size.height += 6;
							size.width = size.height * 3;
						}
					}
				}
		}
		if (size == null) {
			// If it's not a button, use the current size of the object.
			// This is needed e.g. for Spacers, where its current size
			// is the preferred size too.
			size = (this instanceof Button) ? new Size(120, 20) : getSize();
		}
		// add margins
		size.width += margin.left + margin.right;
		size.height += margin.top + margin.bottom;
		return size;
	}

	public void setPreferredSize(int width, int height) {
		prefSize = new Size(width, height);
	}

	public void setPreferredSize(Size size) {
		if (size == null) prefSize = null;
		else setPreferredSize(size.width, size.height);
	}

	public final void setPreferredSize(Point size) {
		if (size == null) prefSize = null;
		else setPreferredSize(size.x, size.y);
	}

	public Size getPreferredSize() {
		return prefSize != null ? prefSize : getBestSize();
	}

	public void setMinimumSize(int width, int height) {
		minSize = new Size(width, height);
	}

	public void setMinimumSize(Size size) {
		if (size == null) minSize = null;
		else setMinimumSize(size.width, size.height);
	}

	public final void setMinimumSize(Point size) {
		if (size == null) minSize = null;
		else setMinimumSize(size.x, size.y);
	}

	public void setMinimumSize(int[] size) {
		if (size == null) minSize = null;
		else setMinimumSize(size[0], size[1]);
	}

	public Size getMinimumSize() {
		return minSize != null ? minSize : getBestSize();
	}

	public void setMaximumSize(int width, int height) {
		maxSize = new Size(width, height);
	}

	public void setMaximumSize(Size size) {
		if (size == null) maxSize = null;
		else setMaximumSize(size.width, size.height);
	}

	public final void setMaximumSize(Point size) {
		if (size == null) maxSize = null;
		else setMaximumSize(size.x, size.y);
	}

	public void setMaximumSize(int[] size) {
		if (size == null) maxSize = null;
		else setMaximumSize(size[0], size[1]);
	}

	public Size getMaximumSize() {
		return maxSize != null ? maxSize : getSize();
	}

	public Border getMargin() {
		return (Border) margin.clone();
	}

	public void setMargin(int top, int right, int bottom, int left) {
		margin = new Border(top, right, bottom, left);
		if (nativeBounds != null)
			updateBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		// Update the margins int he AWT proxy as well
		updateAWTMargin(margin);
	}

	/* 
	 * coordinate system transformations
	 * 
	 */
	
	public native Point localToScreen(int x, int y);
	public native Point screenToLocal(int x, int y);

	public native Rectangle localToScreen(int x, int y, int width, int height);
	public native Rectangle screenToLocal(int x, int y, int width, int height);

	public Point localToScreen(Point pt) {
		return localToScreen(pt.x, pt.y);
	}

	public Point screenToLocal(Point pt) {
		return screenToLocal(pt.x, pt.y);
	}

	public Rectangle localToScreen(Rectangle rt) {
		return localToScreen(rt.x, rt.y, rt.width, rt.height);
	}

	public Rectangle screenToLocal(Rectangle rt) {
		return screenToLocal(rt.x, rt.y, rt.width, rt.height);
	}

	/* 
	 * item display
	 * 
	 */

	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public final void invalidate(Rectangle rt) {
		invalidate(rt.x, rt.y, rt.width, rt.height);
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

	public final void showToolTip(Point pt) {
		showToolTip(pt.x, pt.y);
	}

	public String getToolTip() {
		return toolTip;
	}
	
	public void setToolTip(String tooltip) {
		this.toolTip = tooltip;
		nativeSetTooltip(tooltip);
	}

	/*
	 * AWT LayoutManager integration:
	 */

	protected java.awt.Component getAWTComponent() {
		if (component == null) {
			if (this instanceof ComponentGroup) {
				component = new AWTItemContainer();
			} else {
				component = new AWTItemComponent();
			}
			// Take over margin and bounds from the item.
			updateAWTMargin(margin);
			updateAWTBounds(bounds);
		}
		return component;
	}

	/*
	 * Calculates the absolute origin of the AWT component.
	 */
	protected Point getOrigin(java.awt.Component component) {
		Point delta = new Point();
		java.awt.Container parent = component.getParent();
		while (true) {
			java.awt.Container next = parent.getParent();
			if (next == null)
				break;
			java.awt.Point loc = parent.getLocation();
			delta.x += loc.x;
			delta.y += loc.y;
			parent = next;
		}
		return delta;
	}

	/**
	 * AWTComponent wraps an ADM Item and pretends it is a AWT Component, in
	 * order to take advantage of all the nice LayoutManagers in AWT.
	 * 
	 * @author lehni
	 */
	class AWTItemComponent extends java.awt.Component implements ComponentWrapper {

		public Component getComponent() {
			return Item.this;
		}

		public void doLayout() {
			// do nothing here...
		}

		public void updateBounds(Rectangle bounds) {
			// call the setBounds version in super that directly sets the
			// internal values. setBounds(Rectangle) would call the
			// overridden setBounds(int, int, int, int) which would change the
			// underlying Item.
			super.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		public Dimension getMaximumSize() {
			Size size = Item.this.getMaximumSize();
			return new Dimension(size.width, size.height);
		}

		public Dimension getMinimumSize() {
			Size size = Item.this.getMinimumSize();
			return new Dimension(size.width, size.height);
		}

		public Dimension getPreferredSize() {
			Size size = Item.this.getPreferredSize();
			return new Dimension(size.width, size.height);
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Point origin = Item.this.getOrigin(this);
			Item.this.setBounds(x + origin.x, y + origin.y, width, height);
		}

		public void setBounds(java.awt.Rectangle r) {
			setBounds(r.x, r.y, r.width, r.height);
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			java.awt.Rectangle rect = getBounds();
			Item.this.setBounds(rect.x, rect.y, rect.width, rect.height);
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void setLocation(int x, int y) {
			super.setLocation(x, y);
			Point origin = Item.this.getOrigin(this);
			Item.this.setPosition(x + origin.x, y + origin.y);
		}

		public void setLocation(java.awt.Point p) {
			setLocation(p.x, p.y);
		}

		public boolean isVisible() {
			return Item.this.isVisible();
		}
	}

	/**
	 * The actually AWT class for ItemContainer that does the work of collecting
	 * wrap items or other ItemContainers and redirecting doLayout calls to its
	 * children.
	 * 
	 * @author lehni
	 */
	class AWTItemContainer extends AWTContainer {

		public Component getComponent() {
			return Item.this;
		}

		public void updateBounds(Rectangle bounds) {
			// call the setBounds version in super that directly sets the
			// internal values. setBounds(Rectangle) would call the
			// overridden setBounds(int, int, int, int) which would change the
			// underlying Item.
			super.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		public Dimension getMinimumSize() {
			// If this is a group item such as Frame or ItemGroup, do not
			// use the native items's minimum size
			if (Item.this instanceof ComponentGroup)
				return super.getMinimumSize();
			Size size = Item.this.getMinimumSize();
			return new Dimension(size.width, size.height);
		}

		public Dimension getMaximumSize() {
			// If this is a group item such as Frame or ItemGroup, do not
			// use the native items's maximum size
			if (Item.this instanceof ComponentGroup)
				return super.getMaximumSize();
			Size size = Item.this.getMaximumSize();
			return new Dimension(size.width, size.height);
		}

		public Dimension getPreferredSize() {
			// If this is a group item such as Frame or ItemGroup, do not
			// use the native items's preferred size
			if (Item.this instanceof ComponentGroup)
				return super.getPreferredSize();
			Size size = Item.this.getPreferredSize();
			return new Dimension(size.width, size.height);
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Point origin = Item.this.getOrigin(this);
			Item.this.setBounds(x + origin.x, y + origin.y, width, height);
		}

		public void setBounds(java.awt.Rectangle r) {
			setBounds(r.x, r.y, r.width, r.height);
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			java.awt.Rectangle rect = getBounds();
			Item.this.setBounds(rect.x, rect.y, rect.width, rect.height);
			/*
			if (frame != null) {
				java.awt.Point loc = getLocation();
				frame.setBounds(loc.x, loc.y, width, height);
			}
			*/
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void setLocation(int x, int y) {
			super.setLocation(x, y);
			Point origin = Item.this.getOrigin(this);
			Item.this.setPosition(x + origin.x, y + origin.y);
		}

		public void setLocation(java.awt.Point p) {
			setLocation(p.x, p.y);
		}

		public boolean isVisible() {
			return Item.this.isVisible();
		}
	}
}
