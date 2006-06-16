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
 * $RCSfile: Dialog.java,v $
 * $Author: lehni $
 * $Revision: 1.10 $
 * $Date: 2006/06/16 16:18:29 $
 */

package com.scriptographer.adm;

import org.mozilla.javascript.NativeArray;

import com.scriptographer.js.FunctionHelper;
import com.scriptographer.js.Unsealed;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

public abstract class Dialog extends CallbackHandler implements Unsealed {
	// Dialog options (for Create() call)
	public final static int
		OPTION_NONE = 0,

		// Default ADM options:

		OPTION_IGNORE_KEYPAD_ENTER = 1 << 3,
		//	 Keypad 'enter' key does not activate default item.

		OPTION_ITEMS_HIDDEN = 1 << 4,
		//	 Reduce flicker by creating items hidden.

		OPTION_FORCE_ROMAN = 1 << 5,
		//	 Forces for all items within dialog, except as overridden.

		OPTION_ENTER_BEFORE_OK = 1 << 6,
		//	 Track the enter keys carriage return and keypad enter before the
		//	 dialog treats the event as equivalent to pressing the OK button --
		//	 and prevent that behavior if the tracker returns true. Note that by
		//	 default, the enter keys cause text item trackers to commit their text
		//	 and return true, so this option normally prevents the "OK" behavior
		//	 when enter is pressed within a text item.
		//	 This option currently relevant only on Mac platform.

		// Pseudo options, to simulate window styles:
		OPTION_RESIZING = 1 << 10;

	//	Dialog styles (for Create() call).
	protected final static int
		STYLE_MODAL = 0, // wrapped
		STYLE_ALERT = 1, // wrapped
		STYLE_FLOATING = 2, // wrapped
		STYLE_TABBED_FLOATING = 3, // wrapped
		STYLE_RESIZING_FLOATING = 4, // wrapped
		STYLE_TABBED_RESIZING_FLOATING = 5, // wrapped
		STYLE_POPUP = 6, // wrapped
		STYLE_NOCLOSE_FLOATING = 7, // wrapped
		STYLE_SYSTEM_ALERT = 8, // wrapped
		STYLE_POPUP_CONTROL = 9, // wrapped
		STYLE_RESIZING_MODAL = 10, // wrapped
		STYLE_LEFTSIDED_FLOATING = 11, // wrapped
		STYLE_LEFTSIDED_NOCLOSE_FLOATING = 12, // wrapped
		STYLE_NOTITLE_DOCK_FLOATING = 13, // TODO: wrap this?
		STYLE_TABBED_HIERARCHY_FLOATING = 14,
		STYLE_TABBED_RESIZING_HIERARCHY_FLOATING = 15,
		STYLE_HOST_DEFINED = 65536;

	// 
	protected final static int
		ITEM_UNIQUE = 0,
		ITEM_FIRST = -1,
		ITEM_LAST = -2,
		ITEM_DEFAULT = -3,
		ITEM_CANCEL = -4,
		ITEM_MENU = -5,
		ITEM_RESIZE = -6,
		ITEM_PRIVATE_UNIQUE = -7,
		ITEM_FIRST_UNUSED_PRIVATE = -8;

	// TODO: Think about where to move all the ADM constants
	// (Dialog.CURSOR_, Dialog.FONT_, Drawer.COLOR_
	public final static int
		CURSOR_IBEAM = -1,
		CURSOR_CROSS = -2,
		CURSOR_WAIT = -3,
		CURSOR_ARROW = -4,
		CURSOR_CANCEL = -5,
		CURSOR_FINGER = -6,
		CURSOR_FIST = -7,
		CURSOR_FISTPLUS = -8,
		CURSOR_HOSTCONTROLS = -9;

	// ADMFont
	public final static int
		FONT_DEFAULT = 0,
		FONT_DIALOG = 1,
		FONT_PALETTE = 2,
		FONT_ITALIC_DIALOG = 3,
		FONT_ITALIC_PALETTE = 4,
		FONT_BOLD_DIALOG = 5,
		FONT_BOLD_PALETTE = 6,
		FONT_BOLD_ITALIC_DIALOG = 7,
		FONT_BOLD_ITALIC_PALETTE = 8,
		FONT_FIXEDWIDTH = 9,
		FONT_ITALIC_FIXEDWIDTH = 10,
		FONT_BOLD_FIXEDWIDTH = 11,
		FONT_BOLD_ITALIC_FIXEDWIDTH = 12;

	private ArrayList items;

	// reflections of native fields:
	private int style;
	private int options;
	private Dimension size = null;
	private Rectangle bounds = null;
	private String title = "";

	protected AWTContainer container = null;

	private static ArrayList dialogs = new ArrayList();

	protected Dialog(int style, int options) {
		this.style = style;
		this.options = options;
		items = new ArrayList();
		// create a unique name for this session:
		String name = "Scriptographer Dialog " + dialogs.size();
		handle = nativeCreate(name, style, options);

		if (handle != 0)
			dialogs.add(this);
	}

	public void destroy() {
		nativeDestroy(handle);
		handle = 0;
		dialogs.remove(this);
	}

	public void finalize() {
		if (handle != 0)
			this.destroy();
	}

	public static void destroyAll() {
		for (int i = dialogs.size() - 1; i >= 0; i--) {
			Dialog dialog = (Dialog)dialogs.get(i);
			dialog.destroy();
		}
	}

	public boolean removeItem(Item item) {
		if (items.remove(item)) {
			item.destroy();
			return true;
		}
		return false;
	}

	public void savePreferences(String name) {
		Preferences prefs = Preferences.userNodeForPackage(Dialog.class).node(name);
		// saving the palette position, tab/dock preference.
		DialogGroupInfo groupInfo = getGroupInfo();
		Rectangle bounds = getBounds();
		prefs.put("group", groupInfo.group);
		prefs.putInt("positionCode", groupInfo.positionCode);
		prefs.put("location", bounds.x + " " + bounds.y);
		prefs.put("size", bounds.width + " " + bounds.height);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public boolean loadPreferences(String name) {
		Preferences prefs = Preferences.userNodeForPackage(Dialog.class);
		try {
			if (prefs.nodeExists(name)) {
				prefs = prefs.node(name);

				String group = prefs.get("group", "");
				int positionCode = prefs.getInt("positionCode", DialogGroupInfo.POSITION_DEFAULT);

				Rectangle bounds = getBounds();
				String locStr = prefs.get("location", null);
				Point location;
				if (locStr == null) {
					// Pick a default location in case it has never come up before on this machine
					Rectangle dimensions = Dialog.getPaletteLayoutBounds();
					location = new Point(
						dimensions.x + dimensions.width - bounds.width,
						dimensions.y + dimensions.height - bounds.height
					);
				} else {
					int pos = locStr.indexOf(" ");
					location = new Point(
						Integer.parseInt(locStr.substring(0, pos)),
						Integer.parseInt(locStr.substring(pos + 1))
					);
				}

				// Get the last known location out of the Prefs file
				String sizeStr = prefs.get("size", null);
				Dimension size;
				if (sizeStr == null) {
					size = bounds.getSize();
				} else {
					int pos = sizeStr.indexOf(" ");
					size = new Dimension(
						Integer.parseInt(sizeStr.substring(0, pos)),
						Integer.parseInt(sizeStr.substring(pos + 1))
					);
				}

				// restore the size and location of the dialog
				setBounds(new Rectangle(location, size));
				// restore the position code of the dialog
				setGroupInfo(group, positionCode);
				return true;
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
		 * Callback functions
		 */

	protected void onResize(int dx, int dy) throws Exception {
		// if a contianer was created, the layout needs to be recalculated now:
		if (container != null) {
			container.updateSize(size);
			container.doLayout();
		}
		super.onResize(dx, dy);
	}

	protected void onDestroy() throws Exception {
		callFunction("onDestroy");
	}

	protected void onClose() throws Exception {
		callFunction("onClose");
	}

	protected void onZoom() throws Exception {
		callFunction("onZoom");
	}

	protected void onCycle() throws Exception {
		callFunction("onCycle");
	}

	protected void onCollapse() throws Exception {
		callFunction("onCollapse");
	}

	protected void onExpand() throws Exception {
		callFunction("onExpand");
	}

	protected void onContextMenuChange() throws Exception {
		callFunction("onContextMenuChange");
	}

	protected void onShow() throws Exception {
		callFunction("onShow");
	}

	protected void onHide() throws Exception {
		callFunction("onHide");
	}

	protected void onMove() throws Exception {
		callFunction("onMove");
	}

	protected void onActivate() throws Exception {
		callFunction("onActivate");
	}

	protected void onDeactivate() throws Exception {
		callFunction("onDeactivate");
	}

	protected void onNotify(int notifier) throws Exception {
		switch (notifier) {
			case Notifier.NOTIFIER_DESTROY:
				onDestroy();
				break;
			case Notifier.NOTIFIER_CLOSE_HIT:
				onClose();
				break;
			case Notifier.NOTIFIER_ZOOM_HIT:
				onZoom();
				break;
			case Notifier.NOTIFIER_CYCLE:
				onCycle();
				break;
			case Notifier.NOTIFIER_COLLAPSE:
				onCollapse();
				break;
			case Notifier.NOTIFIER_EXPAND:
				onExpand();
				break;
			case Notifier.NOTIFIER_CONTEXT_MENU_CHANGED:
				onContextMenuChange();
				break;
			case Notifier.NOTIFIER_WINDOW_SHOW:
				onShow();
				break;
			case Notifier.NOTIFIER_WINDOW_HIDE:
				onHide();
				break;
			case Notifier.NOTIFIER_WINDOW_DRAG_MOVED:
				onMove();
				break;
			case Notifier.NOTIFIER_WINDOW_ACTIVATE:
				onActivate();
				break;
			case Notifier.NOTIFIER_WINDOW_DEACTIVATE:
				onDeactivate();
				break;
		}
	}

	/*
	 * AWT LayoutManager integration:
	 */

	protected AWTContainer getContainer() {
		if (container == null)
			container = new AWTContainer();
		return container;
	}

	public void setLayout(LayoutManager mgr) {
		getContainer().setLayout(mgr);
	}

	public void setInsets(int left, int top, int right, int bottom) {
		getContainer().setInsets(left, top, right, bottom);
	}

	public void addToLayout(Item item, Object constraints) {
		getContainer().add(item.getComponent(), constraints);
	}

	public void addToLayout(Item item) {
		addToLayout(item, null);
	}

	public void addToLayout(ItemContainer container, Object constraints) {
		getContainer().add(container.getComponent(), constraints);
	}

	public void addToLayout(ItemContainer layout) {
		addToLayout(layout, null);
	}

	/**
	 * doLayout recalculates the layout, but does not change the dialog's size
	 *
	 */
	public void doLayout() {
		if (container != null)
			container.doLayout();
	}

	/**
	 * autoLayout is supposed to be called only once per dialog, after the initialization
	 * if layout managers are involved. It set's the window's minimum- and preferred size
	 * and calls doLayout.
	 */
	public void autoLayout() {
		if (container != null) {
			setMinimumSize(container.getMinimumSize());
			setSize(container.getPreferredSize());
			// TODO: This seems to crash the whole thing:
			// setMaximumSize(container.getMaximumSize());
			container.doLayout();
		}
	}

	/*
	 * Wrapper stuff:
	 */

	/* TODO: Check these:
		 * - timer stuff
		 * - createNestedItem(...);
		 * - beginAdjustingFocusOrder, doneAdjustingFocusOrder
		 */

	/*
	 * dialog creation/destruction
	 * 
	 */

	/**
	 * sets size and bounds
	 */
	private native int nativeCreate(String name, int dialogStyle, int options);
	private native void nativeDestroy(int dialogRef);

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackCallback(boolean enabled);
	protected native void nativeSetDrawCallback(boolean enabled);

	public native int getTrackMask();
	public native void setTrackMask(int mask);

	/* 
	 * dialog timer
	 * 
	 */
	/*
	public native ADMTimerRef createTimer(ADMUInt32 inMilliseconds,
				ADMActionMask inAbortMask, ADMDialogTimerProc inTimerProc,
				ADMDialogTimerAbortProc inAbortProc, ADMInt32 inOptions);
	
	public native void abortTimer(ADMTimerRef inTimerID);
	*/

	/* 
	 * dialog state accessors
	 *  
	 */

	public native boolean isVisible();
	public native void setVisible(boolean visible);

	public native boolean isEnabled();
	public native void setEnabled(boolean enabled);

	public native boolean isActive();
	public native void setActive(boolean active);

	/* 
		 * dialog bounds accessors
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
			if (container != null) {
				container.updateSize(size);
			}
		}
	}

	public void setSize(Dimension size) {
		setSize(size.width, size.height);
	}

	public void setSize(Point2D size) {
		setSize((int) size.getX(), (int) size.getY());
	}

	public Rectangle getBounds() {
		// TODO: until kADMWindowDragMovedNotifier is not resolved, allways fetch it here
		// if kADMWindowDragMovedNotifier is working, it could be set there...
		bounds = nativeGetBounds();
		return new Rectangle(bounds);
	}

	public void setBounds(int x, int y, int width, int height) {
		bounds.setRect(x, y, width, height);
		nativeSetBounds(x, y, width, height);
		Dimension oldSize = size;
		size = nativeGetSize();
		if (container != null && !size.equals(oldSize)) {
			container.updateSize(size);
		}
	}

	public void setBounds(Rectangle2D bounds) {
		setBounds((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());
	}

	public void setLocation(Point2D loc) {
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
	 * dialog redraw requests
	 * 
	 */

	public native void invalidate();
	public native void invalidate(int x, int y, int width, int height);
	public native void update();

	public void invalidate(Rectangle2D rt) {
		invalidate((int) rt.getX(), (int) rt.getY(), (int) rt.getWidth(), (int) rt.getHeight());
	}

	/*
	 * cursor ID accessors
	 * 
	 */

	public native int getCursor();
	public native void setCursor(int cursor);

	/* 
		 * dialog text accessors
		 *
		 */

	private native void nativeSetTitle(String title);

	public native int getFont();
	public native void setFont(int font);

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		nativeSetTitle(title);
	}

	/* 
	 * dialog length constraints
	 * 
	 */

	public native Dimension getMinimumSize();
	public native void setMinimumSize(int width, int height);

	public native Dimension getMaximumSize();
	public native void setMaximumSize(int width, int height);

	public native Dimension getIncrement();
	public native void setIncrement(int hor, int ver);

	public void setMinimumSize(Dimension size) {
		setMinimumSize(size.width, size.height);
	}

	public void setMinimumSize(Point2D size) {
		setMinimumSize((int) size.getX(), (int) size.getY());
	}

	public void setMaximumSize(Dimension size) {
		setMaximumSize(size.width, size.height);
	}

	public void setMaximumSize(Point2D size) {
		setMaximumSize((int) size.getX(), (int) size.getY());
	}

	public void setIncrement(Dimension increment) {
		setIncrement(increment.width, increment.height);
	}

	public void setIncrement(Point2D increment) {
		setIncrement((int) increment.getX(), (int) increment.getY());
	}

	public Dimension getPreferredSize() {
		if (container != null)
			return container.getPreferredSize();
		return null;
	}

	/* 
	 * item accessors
	 * 
	 */

	protected native long getItemHandle(int itemID);

	private PopupMenu popupMenu = null;

	public PopupMenu getPopupMenu() {
		if (popupMenu == null) {
			long handle = getItemHandle(ITEM_MENU);
			popupMenu = handle != 0 ? new PopupMenu(this, handle) : null;
		}
		return popupMenu;
	}

	private Button resizeButton = null;

	public Button getResizeButton() {
		if (resizeButton == null) {
			long handle = getItemHandle(ITEM_RESIZE);
			resizeButton = handle != 0 ? new Button(this, handle) : null;
		}
		return resizeButton;
	}

	/* 
	 * default/cancel items
	 * 
	 */

	public native Item getDefaultItem();
	public native void setDefaultItem(Item item);

	public native Item getCancelItem();
	public native void setCancelItem(Item item);

	/* 
	 * dialog state accessors	
	 * 
	 */

	public native boolean isCollapsed();

	public native boolean isUpdateEnabled();
	public native void setUpdateEnabled(boolean updateEnabled);

	public native boolean isForcedOnScreen();
	public native void setForcedOnScreen(boolean forcedOnScreen);

	/*
		 * dialog group functions
		 *
		 */

	public native DialogGroupInfo getGroupInfo();
	public native void setGroupInfo(String group, int positionCode);
	public void setGroupInfo(DialogGroupInfo info) {
		setGroupInfo(info.group, info.positionCode);
	}

	/*
		 * Support for various standard dialogs:
		 */

	private static File fileDialog(String message, String[] filters, File selectedFile, boolean open) {
		String filter;
		// Converts the filters to one long string, seperated by \0
		// as needed by the native function.
		if (filters == null) {
			filter = "";
		} else {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < filters.length; i++) {
				buf.append(filters[i]);
				buf.append('\0');
			}
			buf.append('\0');
			filter = buf.toString();
		}
		String directory, filename;
		if (selectedFile == null) {
			directory = filename = null;
		} else if (selectedFile.isDirectory()) {
			directory = selectedFile.getPath();
			filename = "";
		} else {
			directory = selectedFile.getParent();
			filename = selectedFile.getName();
		}
		String path = nativeFileDialog(message, filter, directory, filename, open);
		return path != null ? new File(path) : null;
	}

	private static native String nativeFileDialog(String message, String filter, String directory, String filename, boolean open);

	/**
	 * 
	 * @param message
	 * @param filters
	 * @param selectedFile
	 * @return
	 */
	public static File fileOpen(String message, String[] filters, File selectedFile) {
		return fileDialog(message, filters, selectedFile, true);
	}

	public static File fileOpen(String message, String[] filters) {
		return fileOpen(message, filters, null);
	}

	public static File fileOpen(String message) {
		return fileOpen(message, null, null);
	}

	public static File fileOpen() {
		return fileOpen(null, null, null);
	}

	/**
	 * 
	 * @param message
	 * @param filters
	 * @param selectedFile
	 * @return
	 */
	public static File fileSave(String message, String[] filters, File selectedFile) {
		return fileDialog(message, filters, selectedFile, false);
	}

	public static File fileSave(String message, String[] filters) {
		return fileSave(message, filters, null);
	}

	public static File fileSave(String message) {
		return fileSave(message, null, null);
	}

	public static File fileSave() {
		return fileSave(null, null, null);
	}

	/**
	 * 
	 * @param message
	 * @param selectedDir
	 * @return
	 */
	public static native File chooseDirectory(String message, File selectedDir);

	public static File chooseDirectory(String message) {
		return chooseDirectory(message, null);
	}

	public static File chooseDirectory() {
		return chooseDirectory(null, null);
	}

	/**
	 * 
	 * @param where
	 * @param color
	 * @return
	 */
	public static native Color chooseColor(Point where, Color color);

	public static Color chooseColor(Color color) {
		return chooseColor(null, color);
	}

	public static Color chooseColor() {
		return chooseColor(null, null);
	}

	public static native Rectangle getPaletteLayoutBounds();

	public static native void alert(String message);

	public static native boolean confirm(String message);

	public static Object[] prompt(String title, PromptItem[] items) {
		return PromptDialog.prompt(title, items);
	}

	public static Object[] prompt(String title, Object[] items) {
		return PromptDialog.prompt(title, items);
	}

	/*
	 * This function is only here for the JS environment. From java, use
	 * PromptDialog.prompt directly!
	 */
	public static Object[] prompt(String title, NativeArray items) {
		return PromptDialog.prompt(title, FunctionHelper.convertToArray(items));
	}
	/**
	 * AWTContainer wrapps an ADM Dialog and prentends it is an AWT Container, in
	 * order to take advantage of all the nice LayoutManagers in AWT.
	 *
	 * This goes hand in hand with the AWTComponent that wrapps an IDM Item in a
	 * component.
	 *
	 * Unfortunatelly, some LayoutManagers access fields in Container not visible
	 * from the outside, so length information has to be passed up by super. calls.
	 *
	 * Attention: the ADM bounds are the outside of the window, while here we treat
	 * the length of the AWT bounds for the inside!
	 * Also, for layout the location of the dialog doesn't matter, so let's only
	 * work with length for simplicity
	 */
	class AWTContainer extends Container {
		Insets insets;

		public AWTContainer() {
			updateSize(Dialog.this.getSize());
			setInsets(0, 0, 0, 0);
		}

		public void updateSize(Dimension size) {
			super.setSize(size.width, size.height);
		}

		public void setInsets(int left, int top, int right, int bottom) {
			insets = new Insets(top, left, bottom, right);
		}

		public Insets getInsets() {
			return insets;
		}

		public void setBounds(int x, int y, int width, int height) {
			super.setBounds(x, y, width, height);
			Dialog.this.setSize(width, height);
		}

		public void setBounds(Rectangle r) {
			setBounds(r.x, r.y, r.width, r.height);
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			Dialog.this.setSize(width, height);
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void doLayout() {
			super.doLayout();
			// now walk through all the items and do their layout as well:
			Component[] components = getComponents();
			for (int i = 0; i < components.length; i++)
				components[i].doLayout();
		}

		public boolean isVisible() {
			return true;
		}
	}
}
