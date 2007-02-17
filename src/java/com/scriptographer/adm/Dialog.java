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

import com.scriptographer.ScriptographerEngine;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * @author lehni
 */
public abstract class Dialog extends CallbackHandler {
	
	public native int createPlatformControl();
	public native void dumpControlHierarchy(File file);

	// Dialog options (for Create() call)
	public final static int OPTION_NONE = 0;

	// Default ADM options:

	/**
	 * Keypad 'enter' key does not activate default item.
	 */
	public final static int OPTION_IGNORE_KEYPAD_ENTER = 1 << 3;

	/**
	 * Reduce flicker by creating items hidden.
	 */
	public final static int OPTION_ITEMS_HIDDEN = 1 << 4;

	/**
	 * Forces for all items within dialog, except as overridden.
	 */
	public final static int OPTION_FORCE_ROMAN = 1 << 5;

	/**
	 * Track the enter keys carriage return and keypad enter before the
	 * dialog treats the event as equivalent to pressing the OK button --
	 * and prevent that behavior if the tracker returns true. Note that by
	 * default, the enter keys cause text item trackers to commit their text
	 * and return true, so this option normally prevents the "OK" behavior
	 * when enter is pressed within a text item.
	 * This option currently relevant only on Mac platform.
	 */
	public final static int OPTION_ENTER_BEFORE_OK = 1 << 6;

	// pseudo options, to simulate the various window styles (above 1 << 16)

	/**
	 * Create the dialog hidden
	 */
	public final static int OPTION_HIDDEN = 1 << 17;

	/**
	 * Remember placing of the dialog by automatically storing its state in
	 * the preference file. For each script, a sub-node is created in the
	 * preferences. The dialog's title needs to be unique within one such node,
	 * as it is used to store the dialog's state.
	 */
	public final static int OPTION_REMEMBER_PLACING = 1 << 18;

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

	private int options;
	// the outside dimensions of the dialog, including borders and titlebars
	private Rectangle bounds;
	// the inside dimensions of the dialog, as used by layout managers and such
	private Dimension size;
	private Dimension minSize;
	private Dimension maxSize;
	private String title = "";
	private boolean visible = true;
	private boolean active = false;

	protected AWTContainer container = null;

	/**
	 * ignoreSizeChange tells onSizeChanged to ignore the event. this is set and
	 * unset around calls to setBounds and setGroupInfo.
	 * 
	 * As of CS3, setting size and or bounds is not immediatelly effective. When
	 * natively retrieving size through GetLocalRect / GetBoundsRect, the old
	 * size is returned for a while. So do not rely on them being set here. This
	 * was the reason for introducing ignoreSizeChange, as even in the bounds
	 * change event in this case, still the old dimensions are returned (!)
	 */
	private boolean ignoreSizeChange = false;
	// use two boolean values to monitor the initalized state,
	// to make the distinction between completely initialized (initialized ==
	// true) and somwhere during the call of initialize() (unitialized == false)
	private boolean unitialized = true;
	private boolean initialized = false;
	// used to see wether the size where specified before the dialog is
	// initialized
	private boolean sizeSet = false;
	// used to check if the boundaries (min / max size) are to bet set after
	// initialization
	private boolean boundariesSet = true;

	// for scripts, we cannot allways access ScriptRuntime.getTopCallScope(cx)
	// store a reference to the script's preferences object so we can allways
	// use it this happens completely transparently, the dialog class does not
	// need to know anything about the fact if it's a script or a java class.
	private Preferences preferences;

	private static ArrayList dialogs = new ArrayList();

	protected Dialog(int style, int options) {
		preferences = ScriptographerEngine.getPreferences(true);
		items = new ArrayList();
		// create a unique name for this session:
		String name = "Scriptographer Dialog " + dialogs.size();
		// filter out the pseudo styles from the options:
		// (max. real bitis 16, and the mask is (1 << (max + 1)) - 1
		handle = nativeCreate(name, style, options & ((1 << 17) - 1));
		bounds = nativeGetBounds();
		size = nativeGetSize();
		minSize = nativeGetMinimumSize();
		maxSize = nativeGetMaximumSize();
		this.options = options;
		if (handle != 0)
			dialogs.add(this);
		// allways set dialogs hidden first. 
		// if the OPTION_HIDDEN pseudo flag is not set, the dialog is then
		// displayed in initialize
		setVisible(false);
	}

	/**
	 * This is called when the dialog is displayed the first time.
	 * It's usually fired a bit after the constructor exits, or
	 * when setVisible / doModal / setGroupInfo is called.
	 * We fake this through onActivate and a native dialog timer.
	 * Whatevery fires first, triggers initialize
	 * @throws Exception 
	 */
	private void initialize(boolean setBoundaries) throws Exception {
		// initialize can also be triggered e.g. by setGroupInfo, which needs to
		// be ignored
		if (!ignoreSizeChange) {
			if (unitialized) {
				unitialized = false;
				// if setVisible was called before proper initialization, visible
				// is set but it was not nativelly executed yet. handle this here
				boolean show = (options & OPTION_HIDDEN) == 0 || visible;
				if ((options & OPTION_REMEMBER_PLACING) != 0)
					show = !loadPreferences(title);
				if (container != null) {
					setMinimumSize(container.getMinimumSize());
					setMaximumSize(container.getMaximumSize());
					// if no bounds where specified yet, set the preferred size
					// as defined by the layout
					if (!sizeSet)
						setSize(container.getPreferredSize());
				}
				initialized = true;
				if (show)
					setVisible(true);
				// execute callback handler
				onInitialize();
			}
			// setBoundaries is set to false when calling from initializeAll,
			// because it would be to early to set it there. At least on Mac CS3
			// this causes problems
			if (setBoundaries && !boundariesSet) {
				nativeSetMinimumSize(minSize.width, minSize.height);
				nativeSetMaximumSize(maxSize.width, maxSize.height);
				boundariesSet = true;
			}
		}
	}
	
	public boolean isInitialized() {
		return initialized;
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
			Dialog dialog = (Dialog) dialogs.get(i);
			dialog.destroy();
		}
	}

	/**
	 * Initalize all is needed on startup, as in that particular case, the
	 * initalize event would not be fired fast enough, resulting in conflicts
	 * with positioning of floating palettes. initalizeAll prevents that
	 * problem. It is fired from {@link ScriptographerEngine.init}
	 * 
	 * @throws Exception
	 */
	public static void initializeAll() throws Exception {
		for (int i = 0; i < dialogs.size(); i++) {
			Dialog dialog = (Dialog) dialogs.get(i);
			dialog.initialize(false);
		}
	}

	public boolean removeItem(Item item) {
		if (items.remove(item)) {
			item.destroy();
			return true;
		}
		return false;
	}

	public void savePreferences(String name) throws BackingStoreException {
		Preferences prefs = preferences.node(name);
		// saving the palette position, tab/dock preference.
		DialogGroupInfo groupInfo = getGroupInfo();
		Rectangle bounds = getBounds();
		prefs.put("group", groupInfo.group != null ? groupInfo.group : "");
		prefs.putInt("positionCode", groupInfo.positionCode);
		prefs.put("bounds", bounds.x + " " + bounds.y + " " +
				bounds.width + " " + bounds.height);
	}

	public boolean loadPreferences(String name) throws BackingStoreException {
		if (preferences.nodeExists(name)) {
			Preferences prefs = preferences.node(name);

			// restore the size and location of the dialog
			String[] parts = prefs.get("bounds", "").split("\\s");
			Rectangle bounds;
			if (parts.length == 4) {
				bounds = new Rectangle(Integer.parseInt(parts[0]),
						Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]));
			} else {
				// Pick a default location in case it has never come up before
				// on this machine
				Rectangle defaultBounds = Dialog.getPaletteLayoutBounds();
				bounds = getBounds();
				bounds.setLocation(defaultBounds.x, defaultBounds.y);
			}
			setBounds(bounds);

			String group = prefs.get("group", "");
			int positionCode = prefs.getInt("positionCode",
					DialogGroupInfo.POSITION_DEFAULT);
			// restore the position code of the dialog
			ignoreSizeChange = true;
			setGroupInfo(group, positionCode);
			ignoreSizeChange = false;
			return true;
		}
		return false;
	}

	/*
	 * Callback functions
	 */

	protected void onDestroy() throws Exception {
//		 TODO: Wrapper: callFunction("onDestroy");
	}

	protected void onInitialize() throws Exception {
//		 TODO: Wrapper: callFunction("onInitialize");
	}

	protected void onActivate() throws Exception {
//		 TODO: Wrapper: callFunction("onActivate");
	}

	protected void onDeactivate() throws Exception {
//		 TODO: Wrapper: callFunction("onDeactivate");
	}

	protected void onShow() throws Exception {
//		 TODO: Wrapper: callFunction("onShow");
	}

	protected void onHide() throws Exception {
//		 TODO: Wrapper: callFunction("onHide");
	}

	protected void onMove() throws Exception {
//		 TODO: Wrapper: callFunction("onMove");
	}

	protected void onClose() throws Exception {
//		 TODO: Wrapper: callFunction("onClose");
	}

	protected void onZoom() throws Exception {
//		 TODO: Wrapper: callFunction("onZoom");
	}

	protected void onCycle() throws Exception {
//		 TODO: Wrapper: callFunction("onCycle");
	}

	protected void onCollapse() throws Exception {
//		 TODO: Wrapper: callFunction("onCollapse");
	}

	protected void onExpand() throws Exception {
//		 TODO: Wrapper: callFunction("onExpand");
	}

	protected void onContextMenuChange() throws Exception {
//		 TODO: Wrapper: callFunction("onContextMenuChange");
	}

	protected void onNotify(int notifier) throws Exception {
		switch (notifier) {
		case Notifier.NOTIFIER_WINDOW_INITIALIZE:
			initialize(true);
			break;
		case Notifier.NOTIFIER_DESTROY:
			if ((options & OPTION_REMEMBER_PLACING) != 0)
				savePreferences(title);
			onDestroy();
			break;
		case Notifier.NOTIFIER_WINDOW_ACTIVATE:
			// see comment for initialize to unerstand why this is fired here too
			initialize(true);
			active = true;
			onActivate();
			break;
		case Notifier.NOTIFIER_WINDOW_DEACTIVATE:
			active = false;
			onDeactivate();
			break;
		case Notifier.NOTIFIER_WINDOW_SHOW:
			// see comment for initialize to unerstand why this is fired here
			// as well
			initialize(true);
			visible = true;
			onShow();
			break;
		case Notifier.NOTIFIER_WINDOW_HIDE:
			visible = false;
			onHide();
			break;
		case Notifier.NOTIFIER_WINDOW_DRAG_MOVED:
			onMove();
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
		}
	}

	/**
	 * private callback method, to be called from the native environemnt
	 * It calls onResize
	 */
	private void onSizeChanged(int width, int height) {
		if (!ignoreSizeChange && size != null)
			updateSize(width - size.width, height - size.height);
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
	
	public boolean isVisible() {
		return visible;
	}

	private native void nativeSetVisible(boolean visible);
	
	public void setVisible(boolean visible) {
		// do not set visibility natively before the dialog was properly
		// initialized. otherwise we get a crash.
		if (initialized)
			nativeSetVisible(visible);
		this.visible = visible;
	}

	public native boolean isEnabled();
	
	public native void setEnabled(boolean enabled);

	public boolean isActive() {
		return active;
	}
	
	public native void nativeSetActive(boolean active);
	
	public void setActive(boolean active) {
		nativeSetActive(active);
		this.active = active;
	}
	/* 
	 * dialog bounds accessors
	 *
	 */

	private native Dimension nativeGetSize();
	
	private native void nativeSetSize(int width, int height);

	private native Rectangle nativeGetBounds();

	private native void nativeSetBounds(int x, int y, int width, int height);

	public Rectangle getBounds() {
		// As kADMWindowDragMovedNotifier does not seem to work, allways
		// fetch bounds natively.
		// If kADMWindowDragMovedNotifier was working, the reflected value could
		// be kept up to date...
		bounds = nativeGetBounds();
		return bounds;
	}

	public void setBounds(int x, int y, int width, int height) {
		ignoreSizeChange = true;
		nativeSetBounds(x, y, width, height);
		updateSize(width - bounds.width, height - bounds.height);
		ignoreSizeChange = false;
		sizeSet = true;
	}

	public void setBounds(Rectangle2D bounds) {
		setBounds((int) bounds.getX(), (int) bounds.getY(),
				(int) bounds.getWidth(), (int) bounds.getHeight());
	}

	public Dimension getSize() {
		return size.getSize();
	}

	public void setSize(int width, int height) {
		ignoreSizeChange = true;
		nativeSetSize(width, height);
		updateSize(width - size.width, height - size.height);
		ignoreSizeChange = false;
		sizeSet = true;
	}

	public void setSize(Dimension size) {
		setSize(size.width, size.height);
	}

	public void setSize(Point2D size) {
		setSize((int) size.getX(), (int) size.getY());
	}
	
	/**
	 * Changes the internal size fields (size / bounds) relatively to their
	 * previous values. As bounds and size do not represent the same dimensions
	 * (outer / inner), it has to be done relatively. Change of layout and
	 * calling of onResize is handled here too
	 * 
	 * @param deltaX
	 * @param deltaY
	 */
	protected void updateSize(int deltaX, int deltaY) {
		if (deltaX != 0 || deltaY != 0) {
			size.setSize(size.width + deltaX, size.height + deltaY);
			bounds.setSize(bounds.width + deltaX, bounds.height + deltaY);
			// if a contianer was created, the layout needs to be recalculated now:
			if (container != null)
				container.updateSize(size);
			// calll onResize
			// TODO: deal with Exception...
			try {
				onResize(deltaX, deltaY);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public native Point getLocation();

	public native void setLocation(int x, int y);

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
	 * dialog redraw requests
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

	/*
	 * There seems to be a problem on CS3 with setting minimum / maximum size
	 * before all layout is initialized. The workaround is to reflect these
	 * properties in the wrapper and then only set them natively when the dialog
	 * is activate.
	 */
	private native Dimension nativeGetMinimumSize();
	
	private native void nativeSetMinimumSize(int width, int height);

	private native Dimension nativeGetMaximumSize();
	
	private native void nativeSetMaximumSize(int width, int height);

	public void setMinimumSize(int width, int height) {
		minSize = new Dimension(width, height);
		if (initialized) {
			nativeSetMinimumSize(width, height);
		} else {
			boundariesSet = false;
		}
	}
	
	public void setMinimumSize(Dimension size) {
		if (size != null)
			setMinimumSize(size.width, size.height);
	}

	public void setMinimumSize(Point2D size) {
		if (size != null)
			setMinimumSize((int) size.getX(), (int) size.getY());
	}

	public void setMaximumSize(int width, int height) {
		if (width > Short.MAX_VALUE)
			width = Short.MAX_VALUE;
		if (height > Short.MAX_VALUE)
			height = Short.MAX_VALUE;
		maxSize = new Dimension(width, height);
		if (initialized) {
			nativeSetMaximumSize(width, height);
		} else {
			boundariesSet = false;
		}
	}

	public void setMaximumSize(Dimension size) {
		if (size != null)
			setMaximumSize(size.width, size.height);
	}

	public void setMaximumSize(Point2D size) {
		if (size != null)
			setMaximumSize((int) size.getX(), (int) size.getY());
	}

	public native Dimension getIncrement();
	
	public native void setIncrement(int hor, int ver);

	public void setIncrement(Dimension increment) {
		if (increment != null)
			setIncrement(increment.width, increment.height);
	}

	public void setIncrement(Point2D increment) {
		if (increment != null)
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

	protected native int getItemHandle(int itemID);

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

	private static native File nativeFileDialog(String message, String filter,
			File directory, String filename, boolean open);

	private static File fileDialog(String message, String[] filters,
			File selectedFile, boolean open) {
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
		File directory;
		String filename;
		if (selectedFile == null) {
			directory = null;
			filename = null;
		} else if (selectedFile.isDirectory()) {
			directory = selectedFile;
			filename = "";
		} else {
			directory = selectedFile.getParentFile();
			filename = selectedFile.getName();
		}
		return nativeFileDialog(message, filter, directory, filename, open);
	}

	public static File fileOpen(String message, String[] filters,
			File selectedFile) {
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

	public static native File chooseDirectory(String message, File selectedDir);

	public static File chooseDirectory(String message) {
		return chooseDirectory(message, null);
	}

	public static File chooseDirectory() {
		return chooseDirectory(null, null);
	}

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

	public static Object[] prompt(String title, Map[] items) {
		return PromptDialog.prompt(title, items);
	}

	/*
	 * This function is only here for the JS environment. From java, use
	 * PromptDialog.prompt directly!
	 */
	/*
	public static Object[] prompt(String title, NativeArray items) {
		return PromptDialog.prompt(title, FunctionHelper.convertToArray(items));
	}
	*/
	/**
	 * AWTContainer wrapps an ADM Dialog and prentends it is an AWT Container,
	 * in order to take advantage of all the nice LayoutManagers in AWT.
	 * 
	 * This goes hand in hand with the AWTComponent that wrapps an IDM Item in a
	 * component.
	 * 
	 * Unfortunatelly, some LayoutManagers access fields in Container not
	 * visible from the outside, so size information has to be passed up by
	 * super calls.
	 * 
	 * Attention: the ADM bounds are the outside of the window, while here we
	 * use the size of the AWT bounds for the inside! Also, for layout the
	 * location of the dialog doesn't matter, so let's only work with size for
	 * simplicity
	 */
	class AWTContainer extends Container {
		Insets insets;

		public AWTContainer() {
			updateSize(Dialog.this.getSize());
			setInsets(0, 0, 0, 0);
		}

		public void updateSize(Dimension size) {
			// call setBounds instead of setSize
			// otherwise the call would loop back to the overridden
			// setBounds here, as internally, setSize calls setBounds anyway
			super.setBounds(0, 0, size.width, size.height);
			doLayout();
		}

		public void setInsets(int left, int top, int right, int bottom) {
			insets = new Insets(top, left, bottom, right);
		}

		public Insets getInsets() {
			return insets;
		}

		public void setSize(int width, int height) {
			super.setSize(width, height);
			Dialog.this.setSize(width, height);
		}

		public void setSize(Dimension d) {
			setSize(d.width, d.height);
		}

		public void setBounds(int x, int y, int width, int height) {
			setSize(width, height);
		}

		public void setBounds(Rectangle r) {
			setSize(r.width, r.height);
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
