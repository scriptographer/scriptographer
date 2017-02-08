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
 * File created on 22.12.2004.
 */

package com.scriptographer.adm;

import java.awt.Dimension;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scriptographer.adm.ComponentWrapper;
import com.scriptographer.ui.DialogColor;
import com.scriptographer.ui.Point;
import com.scriptographer.ui.Size;
import com.scriptographer.ui.Rectangle;
import com.scriptographer.ui.Border;


/**
 * @author lehni
 */
public abstract class Item extends Component {
	
	protected ItemType type;

	protected Dialog dialog;

	// The native bounds of the native item, including margin fixes for faulty
	// size handling by ADM (e.g. TextEditItem)
	protected Rectangle nativeBounds = null;
	// The bounds of the items, excluding any margins.
	protected Rectangle bounds;
	// The visual margins, containing both user set margins and the values
	// returned by getNativeMargin() which was introduced to easily compensate
	// for any native margins required by controls.
	protected Border margin;

	private String toolTip;

	protected java.awt.Component component = null;
	private Size minSize = null;
	private Size maxSize = null;
	private Size prefSize = null;
	private boolean sizeSet = false;
	private boolean isChild = false;

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
	protected Item(Dialog dialog, ItemType type, int options) {
		// Tell the dialog to ignore notifications during item creation, as on
		// some versions of Illustrator (CS2 and below), window activation 
		// notifications seem to be triggered by item creation, confusing 
		// dialog.initialize() calls.
		dialog.ignoreNotifications = true;
		int handle = nativeCreate(dialog, type.name, options);
		dialog.ignoreNotifications = false;
		init(dialog, handle, type);
		setFont(dialog.getFont());
	}

	protected Item(Dialog dialog, ItemType type) {
		this(dialog, type, 0);
	}

	/**
	 * Constructor for already existing Items that get wrapped,
	 * e.g. PopupMenu 
	 * 
	 * @param dialog
	 * @param handle
	 * @param isChild
	 */
	protected Item(Dialog dialog, int handle, boolean isChild) {
		this.isChild = isChild;
		init(dialog, handle, ItemType.get(nativeInit(handle, isChild)));
	}

	protected void init(Dialog dialog, int handle, ItemType type) {
		this.dialog = dialog;
		this.handle = handle;
		this.type = type;
		// Set margin only after type was set, as getNativeMargin() sometimes
		// depends on it to be set.
		setMargin(0, 0, 0, 0);
		dialog.items.add(this);
		nativeBounds = nativeGetBounds();
		// nativeSize and nativeBounds are set by the native environment
		// size and bounds need to be updated depending on margins and
		// internalInsets
		bounds = new Rectangle(nativeBounds).add(margin);
	}

	protected void initBounds() {
		if (!sizeSet)
			setSize(getBestSize());
		// This is used to fix ADM bugs on CS4 where an item does not update its
		// native bounds in certain situations (hidden window?) even if it was
		// asked to do so.
		Rectangle bounds = nativeGetBounds();
		if (!bounds.equals(nativeBounds))
			setNativeBounds(nativeBounds.x, nativeBounds.y,
						nativeBounds.width, nativeBounds.height);
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

	protected void onInitialize() {
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

	protected void onDestroy() {
		// retrieve through getter so it can be overridden by subclasses,
		// e.g. HierarchyListBox
		Callable onDestroy = this.getOnDestroy();
		if (onDestroy != null)
			ScriptographerEngine.invoke(onDestroy, this);
	}

	protected void onNotify(Notifier notifier) {
		switch (notifier) {
		case INITIALIZE:
			onInitialize();
			break;
		case DESTROY:
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
	private native int nativeCreate(Dialog dialog, String type, int options);
	
	/**
	 * sets size and bounds to valid values
	 */
	private native String nativeInit(int handle, boolean isChild);
	
	private native void nativeDestroy(int handle);

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackCallback(boolean enabled);
	protected native void nativeSetDrawCallback(boolean enabled);

	public native boolean defaultTrack(Tracker tracker);
	public native void defaultDraw(Drawer drawer);

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

	protected native void nativeSetStyle(int style);
	protected native int nativeGetStyle();
	
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

	protected native Rectangle nativeGetBounds();
	protected native void nativeSetBounds(int x, int y, int width, int height);
	
	protected native void nativeSetSize(int width, int height);

	protected final void setNativeBounds(int x, int y, int width, int height) {
		nativeBounds.set(x, y, width, height);
		// Do not change location for children as this messes things up e.g.
		// for SpinEdit.
		if (isChild)
			nativeSetSize(width, height);
		else
			nativeSetBounds(x, y, width, height);
	}

	public Rectangle getBounds() {
		return new Rectangle(bounds);
	}

	/**
	 * @jshide
	 */
	public void setBounds(int x, int y, int width, int height) {
		updateBounds(x, y, width, height, true);
	}

	public final void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	protected void updateBounds(int x, int y, int width, int height,
			boolean sizeChanged) {
		if (sizeChanged) {
			// Set prefSize so getPreferredSize does not return results from
			// getBestSize()
			prefSize = new Size(width, height);
			// Set minSize if it is not set yet, so getBestSize() is not used
			// anymore.
			if (minSize == null)
				minSize = prefSize;
			sizeSet = true;
		}
		// Update bounds
		bounds.set(x, y, width, height);
		// Update bounds in AWT proxy:
		updateAWTBounds(bounds);
		// updateNativeBounds does all the work with margins.
		updateNativeBounds(x, y, width, height);
	}

	protected Size getSizeCorrection() {
		// Allow subclasses to correct the native size, to fix ADM issues with
		// layouts. Used in getSizeCorrection.
		return null;
	}

	protected void updateNativeBounds(int x, int y, int width, int height) {
		// Calculate native values
		int nativeX = x + margin.left;
		int nativeY = y + margin.top;
		int nativeWidth = width - margin.left - margin.right;
		int nativeHeight = height - margin.top - margin.bottom;

		Size fix = getSizeCorrection();
		if (fix != null) {
			nativeWidth += fix.width;
			nativeHeight += fix.height;
		}

		int deltaX = nativeWidth - nativeBounds.width;
		int deltaY = nativeHeight - nativeBounds.height;

		boolean sizeChanged = deltaX != 0 || deltaY != 0;
		if (sizeChanged || nativeBounds.x != nativeX
				|| nativeBounds.y != nativeY) {
			// Same as setNativeBounds here, only TextEditItem overrides this.
			setNativeBounds(nativeX, nativeY, nativeWidth, nativeHeight);
		}
		if (sizeChanged) {
			try {
				onResize(deltaX, deltaY);
			} catch (Exception e) {
				throw new ScriptographerException(e);
			}
		}
	}

	protected void updateAWTBounds(Rectangle bounds) {
		if (component != null) {
			if (component instanceof AWTItemComponent)
				((AWTItemComponent) component).updateBounds(bounds);
			else if (component instanceof AWTComponentGroupContainer)
				((AWTComponentGroupContainer) component).updateBounds(bounds);
		}
	}

	protected void updateAWTMargin(Border margin) {
		if (component != null && this instanceof ComponentGroup)
			this.getAWTContainer(true).setInsets(margin.top, margin.left,
					margin.bottom, margin.right);
	}

	/**
	 * @jshide
	 */
	public void setPosition(int x, int y) {
		updateBounds(x, y, bounds.width, bounds.height, false);
	}

	public final void setPosition(Point loc) {
		setPosition(loc.x, loc.y);
	}
	
	public Point getPosition() {
		return new Point(bounds.x, bounds.y);
	}

	public Size getSize() {
		return bounds.getSize();
	}

	/**
	 * @jshide
	 */
	public void setSize(int width, int height) {
		updateBounds(bounds.x, bounds.y, width, height, true);
	}

	public final void setSize(Size size) {
		setSize(size.width, size.height);
	}

	public int getWidth() {
		return getSize().width;
	}

	public void setWidth(int width) {
		Size size = sizeSet ? getSize() : getBestSize();
		size.width = width;
		setSize(size);
	}

	public int getHeight() {
		return getSize().height;
	}

	public void setHeight(int height) {
		Size size = sizeSet ? getSize() : getBestSize();
		size.height = height;
		setSize(size);
	}

	public Size getBestSize() {
		int maxWidth = maxSize != null
			? maxSize.width - margin.left - margin.right
			: -1;
		// TODO: verify for which items nativeGetBestSize really works!
		Size size = null;
		double factor = isSmall() ? 0.75 : 1;
		switch (type) {
		case PICTURE_STATIC:
		case PICTURE_CHECKBOX:
		case PICTURE_PUSHBUTTON:
		case PICTURE_RADIOBUTTON:
			Image image = null;
			if (this instanceof ImagePane)
				image = ((ImagePane) this).getImage();
			else if (this instanceof Button)
				image = ((Button) this).getImage();
			if (image != null)
				size = image.getSize();
			else // Default size for image buttons
				size = new Size(32, 24).multiply(factor);
			break;
		case SLIDER: // Default size for slider
			size = new Size(isSmall() ? 120 : 160, 8);
			break;
		case POPUP_LIST:
		case SCROLLING_POPUP_LIST:
			Size textSize = getTextSize(" ", -1, true);
			// Calculate the required size of the popup list based on the
			// space required by the native UI element around the selected text.
			Size addSize = ScriptographerEngine.isMacintosh()
					? new Size(isSmall() ? 32 : 38, 8)
					: new Size(26, 6);
			PopupList list = (PopupList) this;
			if (list.size() > 0) {
				size = new Size(0, 0);
				for (int i = 0, l = list.size(); i < l; i++) {
					ListEntry entry = list.get(i);
					String text = entry.getText();
					Size entrySize = getTextSize(text, maxWidth != -1
							? maxWidth - addSize.width : -1, true);
					size.width = Math.max(size.width, entrySize.width);
					size.height = Math.max(size.height, entrySize.height);
				}
			} else {
				// Empty list, make sure height is at least set for text
				// and it has some width for future content (32 * space)
				size = textSize.multiply(32, 1);
			}
			size = size.add(addSize);
			break;
		default:
			String text = null;
			boolean multiline = false;
			if (this instanceof TextValueItem) {
				text = ((TextValueItem) this).getText();
				multiline = ((TextValueItem) this).isMultiline();
			} else if (this instanceof TextItem) {
				text = ((TextItem) this).getText();
			}
			if (text != null) {
				if (text.equals(""))
					text = " ";
				size = getTextSize(text, maxWidth, !multiline);
				if (size != null) {
					if (this instanceof Button) {
						size.width += size.height * 2;
						size.height += 6;
					} else if (this instanceof TextEditItem) {
						// Ignore the text width for a TextEdit, just use the
						// text height and use a default width across
						// Scriptographer for text edits, based on the height.
						size.width = size.height * 5;
						size.height += 6;
						// Spin Edits seem to need 1px more on Windows...
						if (ScriptographerEngine.isWindows()
								&& this instanceof SpinEdit)
							size.height += 1;
				} else if (this instanceof TextPane) {
						size.width += 4;
						size.height += 4;
					}
				}
			}
		}
		if (size == null) {
			// If it's not a button, use the current size of the object.
			// This is needed e.g. for Spacers, where its current size
			// is the preferred size too.
			size = this instanceof Button
					? new Size(120, 20).multiply(factor)
					: getSize();
		}
		// Add margins
		size.width += margin.left + margin.right;
		size.height += margin.top + margin.bottom;
		if (minSize != null) {
			if (size.width < minSize.width)
				size.width = minSize.width;
			if (size.height < minSize.height)
				size.height = minSize.height;
		}
		if (maxSize != null) {
			if (maxSize.width >= 0 && size.width > maxSize.width)
				size.width = maxSize.width;
			if (maxSize.height >= 0 && size.height > maxSize.height)
				size.height = maxSize.height;
		}
		return size;
	}

	/**
	 * @jshide
	 */
	public void setPreferredSize(int width, int height) {
		prefSize = new Size(width, height);
	}

	public void setPreferredSize(Size size) {
		setPreferredSize(size.width, size.height);
	}

	public Size getPreferredSize() {
		return prefSize != null ? prefSize : getBestSize();
	}

	/**
	 * @jshide
	 */
	public void setMinimumSize(int width, int height) {
		minSize = new Size(width, height);
	}

	public void setMinimumSize(Size size) {
		setMinimumSize(size.width, size.height);
	}

	public Size getMinimumSize() {
		return minSize != null ? minSize : getBestSize();
	}

	/**
	 * @jshide
	 */
	public void setMaximumSize(int width, int height) {
		maxSize = new Size(width, height);
	}

	public void setMaximumSize(Size size) {
		setMaximumSize(size.width, size.height);
	}

	public Size getMaximumSize() {
		return maxSize != null ? maxSize : getSize();
	}

	// top, right, bottom, left
	protected static final Border MARGIN_NONE = new Border(0, 0, 0, 0);

	protected Border getNativeMargin() {
		return MARGIN_NONE;
	}

	public Border getVisualMargin() {
		return (Border) margin.clone();
	}

	public Border getMargin() {
		return margin.subtract(getNativeMargin());
	}

	public void setMargin(int top, int right, int bottom, int left) {
		margin = new Border(top, right, bottom, left).add(getNativeMargin());
		if (nativeBounds != null)
			updateBounds(bounds.x, bounds.y, bounds.width, bounds.height, false);
		// Update the margins in the AWT proxy as well
		updateAWTMargin(margin);
	}

	/* 
	 * Coordinate system transformations
	 * 
	 */
	
	/**
	 * @jshide
	 */
	public native Point localToScreen(int x, int y);

	/**
	 * @jshide
	 */
	public native Point screenToLocal(int x, int y);

	/**
	 * @jshide
	 */
	public native Rectangle localToScreen(int x, int y, int width, int height);

	/**
	 * @jshide
	 */
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
	 * Item display
	 * 
	 */

	public native void invalidate();

	/**
	 * @jshide
	 */
	public native void invalidate(int x, int y, int width, int height);

	public final void invalidate(Rectangle rt) {
		invalidate(rt.x, rt.y, rt.width, rt.height);
	}
	
	public native void update();

	protected native int nativeGetFont();
	
	protected native void nativeSetFont(int font);

	private native void nativeSetBackgroundColor(int color);
	private native int nativeGetBackgroundColor();

	public void setBackgroundColor(DialogColor color) {
		if (color != null)
			nativeSetBackgroundColor(color.value());
	}

	public DialogColor getBackgroundColor() {
		return IntegerEnumUtils.get(DialogColor.class,
				nativeGetBackgroundColor());
	}
	/* 
	 * Cursor accessors
	 * 
	 */
	
	private native int nativeGetCursor();
	
	private native void nativeSetCursor(int cursor);

	public Cursor getCursor() {
		return IntegerEnumUtils.get(Cursor.class, nativeGetCursor());
	}

	public void setCursor(Cursor cursor) {
		if (cursor != null)
			nativeSetCursor(cursor.value);
	}
	
	/*
	 * Tooltips
	 * 
	 */
	
	private native void nativeSetTooltip(String tooltip);
	public native boolean isToolTipEnabled();
	public native void setToolTipEnabled(boolean enabled);

	/**
	 * @jshide
	 */
	public native void showToolTip(int x, int y);

	public final void showToolTip(Point pt) {
		showToolTip(pt.x, pt.y);
	}

	public native void hideToolTip();

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

	protected java.awt.Component getAWTComponent(boolean create) {
		if (component == null && create) {
			if (this instanceof ComponentGroup) {
				component = new AWTComponentGroupContainer();
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
			// Do nothing here...
		}

		public void updateBounds(Rectangle bounds) {
			// Call the setBounds version in super that directly sets the
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
			Item.this.setSize(width, height);
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
	 * The actual AWT class for ComponentGroup sthat does the work of collecting
	 * wrap items or other ComponentGroups and redirecting doLayout calls to its
	 * children.
	 * 
	 * @author lehni
	 */
	class AWTComponentGroupContainer extends AWTContainer {

		public Component getComponent() {
			return Item.this;
		}

		public void updateBounds(Rectangle bounds) {
			// Call the setBounds version in super that directly sets the
			// internal values. setBounds(Rectangle) would call the
			// overridden setBounds(int, int, int, int) which would change the
			// underlying Item.
			super.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		}

		public Dimension getMinimumSize() {
			// If this is a group item such as Frame or ItemGroup, do not
			// use the native items's min size
			if (Item.this instanceof ComponentGroup)
				return super.getMinimumSize();
			Size size = Item.this.getMinimumSize();
			return new Dimension(size.width, size.height);
		}

		public Dimension getMaximumSize() {
			// If this is a group item such as Frame or ItemGroup, do not
			// use the native items's max size
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
			Item.this.setSize(width, height);
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
