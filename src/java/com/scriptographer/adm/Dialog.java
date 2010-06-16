/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 */

package com.scriptographer.adm;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.prefs.Preferences;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scratchdisk.util.StringUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.ai.Color;
import com.scriptographer.sg.Script;

/**
 * @author lehni
 */
public abstract class Dialog extends Component {

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
		STYLE_RESIZING_POPUP_PALETTE = 16,
		STYLE_POPUP_PALETTE = 17,
		STYLE_HIERARCHY_POPUP_PALETTE = 18,
		STYLE_RESIZING_HIERARCH_POPUP_PALETTE = 19,
		STYLE_MODAL_NO_ACTIVATE = 20,
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

	protected ArrayList<Item> items;

	private EnumSet<DialogOption> options;
	// The inside dimensions of the dialog, as used by layout managers and such
	private Size size;
	private Size minSize = null;
	private Size maxSize = null;
	private boolean isResizing = false;
	private boolean isNotifying = false;
	private String title = "";
	private String name = "";
	private boolean visible = false;
	private boolean active = false;

	protected AWTDialogContainer container = null;

	/**
	 * ignoreSizeChange tells onSizeChanged to ignore the event. this is set and
	 * unset around calls to setBounds and setGroupInfo.
	 * 
	 * As of CS3, setting size and or bounds is not immediately effective. When
	 * natively retrieving size through GetLocalRect / GetBoundsRect, the old
	 * size is returned for a while. So do not rely on them being set here. This
	 * was the reason for introducing ignoreSizeChange, as even in the bounds
	 * change event in this case, still the old dimensions are returned (!)
	 */
	private boolean ignoreSizeChange = false;
	// Use two boolean values to monitor the initialized state, to make the
	// distinction between completely initialized (initialized == true) and
	// somewhere during the call of initialize() (uninitialized == false)
	private boolean unitialized = true;
	private boolean initialized = false;
	// Used to see whether the size where specified before the dialog is
	// initialized
	private boolean sizeSet = false;
	// Used to check if the boundaries (min / max size) are to bet set after
	// initialization
	private boolean boundsInitialized = false;

	// For scripts, we cannot always access ScriptRuntime.getTopCallScope(cx)
	// store a reference to the script's preferences object so we can always
	// use it this happens completely transparently, the dialog class does not
	// need to know anything about the fact if it's a script or a java class.
	private Preferences preferences;

	private Script script = null;

	private static ArrayList<Dialog> dialogs = new ArrayList<Dialog>();
	private static HashMap<String, Dialog> dialogsByName =
			new HashMap<String, Dialog>();

	protected Dialog(int style, EnumSet<DialogOption> options) {
		script = ScriptographerEngine.getCurrentScript();
		preferences = ScriptographerEngine.getPreferences(script);
		items = new ArrayList<Item>();
		handle = nativeCreate(name, style, IntegerEnumUtils.getFlags(options));
		// Always set dialogs hidden first. 
		// if the OPTION_HIDDEN pseudo flag is not set, the dialog is then
		// displayed in initialize
		setVisible(false);
		size = nativeGetSize();

		isResizing = style == STYLE_RESIZING_FLOATING ||
			style == STYLE_TABBED_RESIZING_FLOATING ||
			style == STYLE_TABBED_RESIZING_HIERARCHY_FLOATING;

		this.options = options != null ? options.clone()
				: EnumSet.noneOf(DialogOption.class);
		if (handle != 0)
			dialogs.add(this);
	}

	public void setFont(DialogFont font) {
		super.setFont(font);
		for (Item item : items)
			item.setFont(font);
	}

	/**
	 * This is called when the dialog is displayed the first time.
	 * It's usually fired a bit after the constructor exits, or
	 * when setVisible / doModal / setGroupInfo is called.
	 * We fake this through onActivate and a native dialog timer.
	 * Whatever fires first, triggers initialize
	 */
	protected void initialize(boolean setBoundaries, boolean initBounds) {
		// initialize can also be triggered e.g. by setGroupInfo, which needs to
		// be ignored
		if (!ignoreSizeChange) {
			if (unitialized) {
				unitialized = false;
				// if setVisible was called before proper initialization, visible
				// is set but it was not natively executed yet. handle this here
				boolean show = !options.contains(DialogOption.HIDDEN) || visible;
				boolean prefsLoaded = false;
				if (options.contains(DialogOption.REMEMBER_PLACING)) {
					prefsLoaded = loadPreferences(title);
					// Only explicitly show dialog if prefs could not be loaded.
					show = !prefsLoaded;
				}
				if (container != null) {
					if (minSize == null)
						setMinimumSize(new Size(container.getMinimumSize()));
					if (maxSize == null)
						setMaximumSize(new Size(container.getMaximumSize()));
					// If no bounds where specified yet, set the preferred size
					// as defined by the layout
					if (!sizeSet) {
						setSize(getPreferredSize());
					} else {
						// If a container was created, the layout needs to be
						// recalculated now, even if the size has not changed.
						// This is needed as updateSize might not have fired
						// correctly before initialization...
						// This solves the display of uninitialized dialogs
						// when first running Scriptographer.
						container.updateSize(size);
					}
				}
				// Center it on screen now if prefs were not loaded above
				if (!prefsLoaded)
					centerOnScreen();
				initialized = true;
				// Execute callback handler
				onInitialize();
				if (show)
					setVisible(true);
			}
			// setBoundaries is set to false when calling from initializeAll,
			// because it would be too early to set it there. At least on Mac
			// CS3 this causes problems
			if (setBoundaries && isResizing) {
				if (minSize != null)
					nativeSetMinimumSize(minSize.width, minSize.height);
				if (maxSize != null)
					nativeSetMaximumSize(maxSize.width, maxSize.height);
			}
			// Call initBounds on all items at the first time the dialog is 
			// shown. This fixes issues on CS4 and above with wrong item bounds.
			if (initBounds && !boundsInitialized) {
				for (Item item : items)
					item.initBounds();
				boundsInitialized = true;
			}
		}
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public void destroy() {
		if (isNotifying) {
			// If we're in a notification, invoke destroy later to fix  a bug
			// on Windows PC and possible future bugs on Mac.
			invokeLater(new Runnable() {
				public void run() {
					Dialog.this.destroy();
				}
			});
		} else {
			nativeDestroy(handle);
			dialogs.remove(this);
			dialogsByName.remove(name);
			handle = 0;
		}
	}
	
	/**
	 * @jshide
	 */
	public void finalize() {
		if (handle != 0)
			this.destroy();
	}

	protected boolean canRemove(boolean ignoreKeepAlive) {
		return script == null || script.canRemove(ignoreKeepAlive);
	}
	
	/**
	 * @jshide
	 */
	public static void destroyAll(boolean ignoreKeepAlive, boolean force) {
		// Loop backwards since destroy removes from the list
		for (int i = dialogs.size() - 1; i >= 0; i--) {
			Dialog dialog = (Dialog) dialogs.get(i);
			if (force || dialog.canRemove(ignoreKeepAlive))
				dialog.destroy();
		}
	}

	/**
	 * Initalize all is needed on startup, as in that particular case, the
	 * initalize event would not be fired fast enough, resulting in conflicts
	 * with positioning of floating palettes. initalizeAll prevents that
	 * problem. It is fired from {@link ScriptographerEngine.init}
	 * 
	 * @jshide
	 */
	public static void initializeAll() {
		for (Dialog dialog : dialogs) {
			dialog.initialize(false, false);
			// Work around weird issue of items sometimes not appearing properly
			// in layouts after reloading the plug-in by just slightly reshaping
			// the Window, causing the layout to be regenerated again. Simply
			// calling doLayout() does not seem to be enough.
			// This affects CS4 and above on Mac.
			if (dialog.isVisible()) {
				Size size = dialog.getSize();
				size.height--;
				dialog.setSize(size);
			}
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
		Preferences prefs = preferences.node(name);
		// Saving the palette position, tab/dock preference.
		DialogGroupInfo groupInfo = getGroupInfo();
		Rectangle bounds = getBounds();
		prefs.put("group", groupInfo.group != null ? groupInfo.group : "");
		prefs.putInt("positionCode", groupInfo.positionCode);
		prefs.put("bounds", bounds.x + " " + bounds.y + " " +
				bounds.width + " " + bounds.height);
	}

	public boolean loadPreferences(String name) {
		try {
			if (preferences.nodeExists(name)) {
				Preferences prefs = preferences.node(name);

				// Restore the size and location of the dialog
				String[] parts = prefs.get("bounds", "").split("\\s");
				Rectangle bounds;
				if (parts.length == 4) {
					bounds = new Rectangle(Integer.parseInt(parts[0]),
							Integer.parseInt(parts[1]),
							Integer.parseInt(parts[2]),
							Integer.parseInt(parts[3]));
				} else {
					// Pick a default location in case it has never come up
					// before on this machine
					Rectangle defaultBounds = Dialog.getPaletteLayoutBounds();
					bounds = getBounds();
					bounds.setPoint(defaultBounds.x, defaultBounds.y);
				}
				String group = prefs.get("group", "");
				int positionCode = prefs.getInt("positionCode",
						DialogGroupInfo.POSITION_DEFAULT);
				// Restore the position code of the dialog
				setGroupInfo(group, positionCode);
				// Now set the bounds
				BoundsSetter setter = new BoundsSetter(bounds);
				setter.run();
				// Sometimes we need to set bounds again afterwards, as OWL
				// seems to interfere here...
				// This leads to annoying jumping around of the dialog.
				// TODO: See if this can be fixed somehow?
				invokeLater(setter);
				return true;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	private class BoundsSetter implements Runnable {
		Rectangle bounds;

		BoundsSetter(Rectangle bounds) {
			this.bounds = bounds;
		}

		public void run() {
			if (isResizing) {
				setBounds(bounds);
			} else {
				setPosition(bounds.getPoint());
			}
		}
	}

	/*
	 * Callback functions
	 */
	private Callable onDestroy = null;

	public Callable getOnDestroy() {
		return onDestroy;
	}

	public void setOnDestroy(Callable onDestroy) {
		this.onDestroy = onDestroy;
	}

	protected void onDestroy() {
		if (onDestroy != null)
			ScriptographerEngine.invoke(onDestroy, this);
	}

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

	private Callable onActivate = null;

	public Callable getOnActivate() {
		return onActivate;
	}

	public void setOnActivate(Callable onActivate) {
		this.onActivate = onActivate;
	}

	protected void onActivate() {
		if (onActivate != null)
			ScriptographerEngine.invoke(onActivate, this);
	}

	private Callable onDeactivate = null;

	public Callable getOnDeactivate() {
		return onDeactivate;
	}

	public void setOnDeactivate(Callable onDeactivate) {
		this.onDeactivate = onDeactivate;
	}

	protected void onDeactivate() {
		if (onDeactivate != null)
			ScriptographerEngine.invoke(onDeactivate, this);
	}

	private Callable onShow = null;

	public Callable getOnShow() {
		return onShow;
	}

	public void setOnShow(Callable onShow) {
		this.onShow = onShow;
	}

	protected void onShow() {
		if (onShow != null)
			ScriptographerEngine.invoke(onShow, this);
	}

	private Callable onHide = null;

	public Callable getOnHide() {
		return onHide;
	}

	public void setOnHide(Callable onHide) {
		this.onHide = onHide;
	}

	protected void onHide() {
		if (onHide != null)
			ScriptographerEngine.invoke(onHide, this);
	}

	private Callable onMove = null;

	public Callable getOnMove() {
		return onMove;
	}

	public void setOnMove(Callable onMove) {
		this.onMove = onMove;
	}

	protected void onMove() {
		if (onMove != null)
			ScriptographerEngine.invoke(onMove, this);
	}

	private Callable onClose = null;

	public Callable getOnClose() {
		return onClose;
	}

	public void setOnClose(Callable onClose) {
		this.onClose = onClose;
	}

	protected void onClose() {
		if (onClose != null)
			ScriptographerEngine.invoke(onClose, this);
	}

	private Callable onZoom = null;

	public Callable getOnZoom() {
		return onZoom;
	}

	public void setOnZoom(Callable onZoom) {
		this.onZoom = onZoom;
	}

	protected void onZoom() {
		if (onZoom != null)
			ScriptographerEngine.invoke(onZoom, this);
	}

	private Callable onCycle = null;

	public Callable getOnCycle() {
		return onCycle;
	}

	public void setOnCycle(Callable onCycle) {
		this.onCycle = onCycle;
	}

	protected void onCycle() {
		if (onCycle != null)
			ScriptographerEngine.invoke(onCycle, this);
	}

	private Callable onCollapse = null;

	public Callable getOnCollapse() {
		return onCollapse;
	}

	public void setOnCollapse(Callable onCollapse) {
		this.onCollapse = onCollapse;
	}

	protected void onCollapse() {
		if (onCollapse != null)
			ScriptographerEngine.invoke(onCollapse, this);
	}

	private Callable onExpand = null;

	public Callable getOnExpand() {
		return onExpand;
	}

	public void setOnExpand(Callable onExpand) {
		this.onExpand = onExpand;
	}

	protected void onExpand() {
		if (onExpand != null)
			ScriptographerEngine.invoke(onExpand, this);
	}

	// TODO: consider better name!
	private Callable onContextMenuChange = null;

	private boolean fireOnClose = true;

	public Callable getOnContextMenuChange() {
		return onContextMenuChange;
	}

	public void setOnContextMenuChange(Callable onContextMenuChange) {
		this.onContextMenuChange = onContextMenuChange;
	}

	protected void onContextMenuChange() {
		if (onContextMenuChange != null)
			ScriptographerEngine.invoke(onContextMenuChange, this);
	}

	protected void onNotify(Notifier notifier) {
		isNotifying = true;
		try {
			switch (notifier) {
			case INITIALIZE:
				initialize(true, false);
				break;
			case DESTROY:
				if (options.contains(DialogOption.REMEMBER_PLACING))
					savePreferences(title);
				onDestroy();
				break;
			case WINDOW_ACTIVATE:
				// See comment for initialize to understand why this is fired
				// here too
				initialize(true, false);
				active = true;
				onActivate();
				break;
			case WINDOW_DEACTIVATE:
				active = false;
				onDeactivate();
				break;
			case WINDOW_SHOW:
				// See comment for initialize to understand why this is fired
				// here too
				initialize(false, true);
				visible = true;
				fireOnClose = true;
				onShow();
				break;
			case WINDOW_HIDE:
				if (fireOnClose) {
					// Workaround for missing onClose on CS3. This bug was 
					// reported to Adobe too late, hopefully it will be back
					// again in CS4...
					// (NOT. But in CS4, MASK_DOCK_CLOSED is now set, not the
					// other two).
					long code = this.getGroupInfo().positionCode;
					if ((code & DialogGroupInfo.MASK_DOCK_VISIBLE) == 0 ||
						(code & DialogGroupInfo.MASK_TAB_HIDDEN) != 0 ||
						(code & DialogGroupInfo.MASK_DOCK_CLOSED) != 0) {
						fireOnClose = false;
						onClose();
					}
				}
				visible = false;
				onHide();
				break;
			case WINDOW_DRAG_MOVED:
				onMove();
				break;
			case CLOSE_HIT:
				// prevent onClose from being called twice...
				if (fireOnClose)
					onClose();
				break;
			case ZOOM_HIT:
				onZoom();
				break;
			case CYCLE:
				onCycle();
				break;
			case COLLAPSE:
				onCollapse();
				break;
			case EXPAND:
				onExpand();
				break;
			case CONTEXT_MENU_CHANGED:
				onContextMenuChange();
				break;
			}
		} finally {
			isNotifying = false;
		}
	}

	/**
	 * private callback method, to be called from the native environment
	 * It calls onResize
	 */
	private void onSizeChanged(int width, int height, boolean invoke) {
		if (!ignoreSizeChange && size != null) {
			int deltaX = width - size.width;
			int deltaY = height - size.height;
			// On some dialog types on OSX (non tabbed resizing),
			// the new size is not ready in the nSizeChanged handler.
			// Detect this here and use invokeLater to fix it by calling again.
			if (deltaX != 0 || deltaY != 0) {
				updateSize(deltaX, deltaY);
			} else if (invoke) {
				invokeLater(new Runnable() {
					public void run() {
						Size size = nativeGetSize();
						onSizeChanged(size.width, size.height, false);
						// These dialogs also seem to need a repaint
						update();
					}
				});
			}
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

	/**
	 * Returns the native window handle. This is a WindowPtr on the mac and a
	 * HWND on Windows.
	 * 
	 * @jshide
	 */
	public native int getWindowHandle();

	public native void makeOverlay(int handle);

	/**
	 * Dumps the Mac control hierarchy to the given file. For debug purposes
	 * only.
	 * 
	 * @jshide
	 */
	public native void dumpControlHierarchy(File file); 

	/*
	 * Dialog creation/destruction
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

	public native boolean defaultTrack(Tracker tracker);

	public native void defaultDraw(Drawer drawer);

	public native int getTrackMask();

	public native void setTrackMask(int mask);

	/* 
	 * Dialog timer
	 * 
	 */
	/*
	public native ADMTimerRef createTimer(ADMUInt32 inMilliseconds,
				ADMActionMask inAbortMask, ADMDialogTimerProc inTimerProc,
				ADMDialogTimerAbortProc inAbortProc, ADMInt32 inOptions);
	
	public native void abortTimer(ADMTimerRef inTimerID);
	*/

	/* 
	 * Dialog state accessors
	 *  
	 */

	private native boolean nativeIsVisible();
	
	public boolean isVisible() {
		// There are rare occasions where the visible property is still out
		// of sync with the native visibility, especially when launching
		// Scriptographer or Illustrator for the first time. So keep it synced
		// to make sure we're fine.
		if (initialized)
			visible = nativeIsVisible();
		return visible;
	}

	private native void nativeSetVisible(boolean visible);

	public void setVisible(boolean visible) {
		// Do not set visibility natively before the dialog was properly
		// initialized. otherwise we get a crash on certain systems (not sure
		// which ones anymore, but this works fine).
		if (initialized) {
			fireOnClose  = false;
			nativeSetVisible(visible);
			fireOnClose  = true;
		}
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
	 * Dialog bounds accessors
	 *
	 */

	private native Size nativeGetSize();
	
	private native void nativeSetSize(int width, int height);

	private native Rectangle nativeGetBounds();

	private native void nativeSetBounds(int x, int y, int width, int height);

	public Rectangle getBounds() {
		// As kADMWindowDragMovedNotifier does not seem to work, fetch bounds
		// natively.
		return nativeGetBounds();
	}

	public void setBounds(int x, int y, int width, int height) {
		ignoreSizeChange = true;
		Rectangle bounds = nativeGetBounds();
		nativeSetBounds(x, y, width, height);
		updateSize(width - bounds.width, height - bounds.height);
		ignoreSizeChange = false;
		sizeSet = true;
	}

	public void setBounds(Rectangle bounds) {
		setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	public Size getSize() {
		return (Size) size.clone();
	}

	public void setSize(int width, int height) {
		ignoreSizeChange = true;
		nativeSetSize(width, height);
		updateSize(width - size.width, height - size.height);
		ignoreSizeChange = false;
		sizeSet = true;
	}

	public void setSize(Size size) {
		if (size != null)
			setSize(size.width, size.height);
	}
	
	/**
	 * Changes the internal size fields (size / bounds) relatively to their
	 * previous values. As bounds and size do not represent the same Dimensions
	 * (outer / inner), it has to be done relatively. Change of layout and
	 * calling of onResize is handled here too
	 * 
	 * @param deltaX
	 * @param deltaY
	 */
	protected void updateSize(int deltaX, int deltaY) {
		if (deltaX != 0 || deltaY != 0) {
			size.set(size.width + deltaX, size.height + deltaY);
			// If a container was created, the layout needs to be recalculated
			// now:
			if (container != null)
				container.updateSize(size);
			// Call onResize
			try {
				onResize(deltaX, deltaY);
			} catch (Exception e) {
				// TODO: deal with Exception...
				throw new ScriptographerException(e);
			}
		}
	}

	public native Point getPosition();

	public native void setPosition(int x, int y);

	public final void setPosition(Point point) {
		setPosition(point.x, point.y);
	}

	/*
	 * Coordinate system transformations
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
	 * Dialog redraw requests
	 * 
	 */

	public native void invalidate();
	
	public native void invalidate(int x, int y, int width, int height);
	
	public native void update();

	public void invalidate(Rectangle rt) {
		invalidate(rt.x, rt.y, rt.width, rt.height);
	}

	public void redraw() {
		invalidate();
		update();
	}

	/*
	 * Cursor ID accessors
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
	 * Dialog text accessors
	 *
	 */

	protected native int nativeGetFont();
	
	protected native void nativeSetFont(int font);

	public String getTitle() {
		return title;
	}

	private native void nativeSetTitle(String title);

	public void setTitle(String title) {
		this.title = title != null ? title : "";
		nativeSetTitle(title);
		// If the dialog name is not set yet, use the title
		if (name.equals("")) {
			// Append script path to name
			setName(script != null
					? StringUtils.join(ScriptographerEngine.getScriptPath(
							script.getFile(), false), "_") + "_" + title
					: title);
		}
	}

	public String getName() {
		return name;
	}

	private native void nativeSetName(String name);

	public void setName(String name) {
		// If we are changing the name, remove the previous lookup
		if (!this.name.equals(""))
			dialogsByName.remove(this.name);
		this.name = name != null ? name : "";
		// See if there was a dialog with this name already, and if so
		// destroy it first. This allows script to easily replace their
		// own dialogs.
		if (!this.name.equals("")) {
			Dialog other = dialogsByName.get(this.name);
			if (other != null) {
				if (other.canRemove(false)) {
					other.setVisible(false);
					other.destroy();
				} else {
					// Renaming the other dialog, so they don't collide by name
					// natively. This even works when there are more than two
					// dialogs, as other.setName keeps calling setName
					// recursively with appended names too.
					other.setName(this.name + "_");
				}
			}
			dialogsByName.put(this.name, this);
		}
		nativeSetName(name);
	}

	/*
	 * dialog length constraints
	 * 
	 */

	/*
	 * There seems to be a problem on CS3 with setting min / max size
	 * before all layout is initialized. The workaround is to reflect these
	 * properties in the wrapper and then only set them natively when the dialog
	 * is activate.
	 */
	@SuppressWarnings("unused")
	private native Size nativeGetMinimumSize();
	
	private native void nativeSetMinimumSize(int width, int height);

	@SuppressWarnings("unused")
	private native Size nativeGetMaximumSize();
	
	private native void nativeSetMaximumSize(int width, int height);

	public Size getMinimumSize() {
		return minSize != null ? minSize : getSize();
	}

	public void setMinimumSize(int width, int height) {
		minSize = new Size(width, height);
		if (initialized && isResizing)
			nativeSetMinimumSize(width, height);
	}
	
	public void setMinimumSize(Size size) {
		if (size != null)
			setMinimumSize(size.width, size.height);
	}

	public Size getMaximumSize() {
		return maxSize != null ? maxSize : getSize();
	}

	public void setMaximumSize(int width, int height) {
		if (width > Short.MAX_VALUE)
			width = Short.MAX_VALUE;
		if (height > Short.MAX_VALUE)
			height = Short.MAX_VALUE;
		maxSize = new Size(width, height);
		if (initialized && isResizing)
			nativeSetMaximumSize(width, height);
	}

	public void setMaximumSize(Size size) {
		if (size != null)
			setMaximumSize(size.width, size.height);
	}

	public native Size getIncrement();
	
	public native void setIncrement(int hor, int ver);

	public void setIncrement(Size increment) {
		if (increment != null)
			setIncrement(increment.width, increment.height);
	}

	public void setIncrement(Point increment) {
		if (increment != null)
			setIncrement(increment.x, increment.y);
	}

	public Size getPreferredSize() {
		if (container != null) {
			Dimension preferred = container.getPreferredSize();
			// Make sure preferred size does not go bellow minSize
			if (minSize != null) {
				if (preferred.width < minSize.width)
					preferred.width = minSize.width;
				if (preferred.height < minSize.height)
					preferred.height = minSize.height;
			}
			return new Size(preferred);
		}
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
			int handle = getItemHandle(ITEM_MENU);
			// We need to pass false for isChild as we want notifiers installed
			popupMenu = handle != 0 ? new PopupMenu(this, handle, false) : null;
		}
		return popupMenu;
	}

	private Button resizeButton = null;

	public Button getResizeButton() {
		if (resizeButton == null) {
			int handle = getItemHandle(ITEM_RESIZE);
			resizeButton = handle != 0 ? new Button(this, handle, false) : null;
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
	
	private native void nativeSetGroupInfo(String group, int positionCode);
	
	public void setGroupInfo(String group, int positionCode) {
		// ignore size changes since it would happen to early and the
		// new size would not be returned by native ADM from within setGroupInfo.
		// So get the new size after and call onSizeChanged manually.
		ignoreSizeChange = true;
		nativeSetGroupInfo(group, positionCode);
		ignoreSizeChange = false;
		Size size = nativeGetSize();
		onSizeChanged(size.width, size.height, false);
	}

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
		String filter = null;
		// Converts the filters to one long string, separated by \0
		// as needed by the native function.
		if (filters != null) {
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

	public static File fileSave(String message, String[] filters,
			File selectedFile) {
		return fileDialog(message, filters, selectedFile, false);
	}

	public static native File chooseDirectory(String message, File selectedDir);

	public static native Color chooseColor(Color color);

	/**
	 * @jshide
	 */
	public static native Rectangle getPaletteLayoutBounds();

	/**
	 * Returns the screen size for centering of dialogs. Ideally
	 * this should be public and somewhere where it makes sense.
	 */
	protected static native Size getScreenSize();

	public void centerOnScreen() {
		// Visually center dialog on Screen,
		// bit higher up than mathematically centered
		Size screen = Dialog.getScreenSize(), size = this.getSize();
		this.setPosition(
			(screen.width - size.width) / 2,
			(8 * screen.height / 10 - size.height) / 2
		);
	}

	/*
	 * AWT LayoutManager integration:
	 */

	protected java.awt.Component getAWTComponent(boolean create) {
		if (container == null && create)
			container = new AWTDialogContainer();
		return container;
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
	 * AWTContainer wraps an UI Dialog and pretends it is an AWT Container,
	 * in order to take advantage of all the nice LayoutManagers in AWT.
	 * 
	 * This goes hand in hand with the AWTComponent that wraps an IDM Item in a
	 * component.
	 * 
	 * Unfortunately, some LayoutManagers access fields in Container not
	 * visible from the outside, so size information has to be passed up by
	 * super calls.
	 * 
	 * Attention: the UI bounds are the outside of the window, while here we
	 * use the size of the AWT bounds for the inside! Also, for layout the
	 * location of the dialog doesn't matter, so let's only work with size for
	 * simplicity
	 * 
	 * @author lehni
	 */
	class AWTDialogContainer extends AWTContainer {
		public AWTDialogContainer() {
			updateSize(Dialog.this.getSize());
			setInsets(0, 0, 0, 0);
		}

		public Component getComponent() {
			return Dialog.this;
		}

		public void updateSize(Size size) {
			// Call setBounds instead of setSize, otherwise the call would loop
			// back to the overridden setBounds here, as internally, setSize
			// calls setBounds anyway
			super.setBounds(0, 0, size.width, size.height);
			doLayout();
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

		public void setBounds(java.awt.Rectangle r) {
			setSize(r.width, r.height);
		}

		public boolean isVisible() {
			return true;
		}
	}
}
