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
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:35:06 $
 */

package com.scriptographer.adm;

import com.scriptographer.js.ArgumentReader;
import com.scriptographer.js.FunctionHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class Dialog extends CallbackHandler {
	// Dialog options (for Create() call)
	public final static int 
		OPTION_TABBED_DIALOG_SHOWS_CYCLE = (1 << 0),
		//	 When creating tabbed dialog with a cycle button on the tab.
	
		OPTION_PASS_MOUSEDOWNEVENT = (1 << 1),
		//	 To allow modal dialogs pass mouse down events through to
		//	 the user dialog tracker on the application side.
	
		OPTION_IGNORE_KEYPAD_ENTER = (1 << 3),
		//	 Keypad 'enter' key does not activate default item.
	
		OPTION_DIALOGITEMS_HIDDEN = (1 << 4),
		//	 Reduce flicker by creating items hidden.

		OPTION_FORCE_ROMAN = (1 << 5),
		//	 Forces for all items within dialog, except as overridden.
		
		OPTION_TRACK_ENTER_BEFORE_OK = (1 << 6),
		//	 Track the enter keys (carriage return and keypad enter) before the
		//	 dialog treats the event as equivalent to pressing the OK button --
		//	 and prevent that behavior if the tracker returns true. Note that by
		//	 default, the enter keys cause text item trackers to commit their text
		//	 and return true, so this option normally prevents the "OK" behavior
		//	 when enter is pressed within a text item.
		//	 (This option currently relevant only on Mac platform.)
		
		OPTION_MODAL_DIALOG_HAS_SYSTEM_CONTROLS = (1 << 7),
		//	 0 by default. If set, ADM Modal dialogs on Windows will have a 
		//	 close box on the top right hand corner. Also there is a host
		//	 option that a user can use if all dialogs in the application
		//	 need that behavior. 
		//	 dagashe:09/29/00:added for Acrobat 5.0 bug #382265
		
		OPTION_POPUP_DIALOG_AS_FLOATING = (1 << 8);
		//	If this option is set for a dialog of style kADMPopupControldialogStyle
		//	then ADM will create the dialog of kFloatingwindowclass. This option
		//	is currently used only on MacOSX.

	//	Dialog styles (for Create() call).
	public final static int
		STYLE_MODAL = 0,
		STYLE_ALERT = 1,
		STYLE_FLOATING = 2,
		STYLE_TABBED_FLOATING = 3,
		STYLE_RESIZING_FLOATING = 4,
		STYLE_TABBED_RESIZING_FLOATING = 5,
		STYLE_POPUP = 6,
		STYLE_NOCLOSE_FLOATING = 7,
		STYLE_SYSTEM_ALERT = 8,
		STYLE_POPUP_CONTROL = 9,
		STYLE_RESIZING_MODAL = 10,
		STYLE_LEFTSIDED_FLOATING = 11,
		STYLE_LEFTSIDED_NOCLOSE_FLOATING = 12,
		STYLE_NOTITLE_DOCK_FLOATING = 13,
		STYLE_TABBED_HIERARCHY_FLOATING = 14,
		STYLE_TABBED_RESIZING_HIERARCHY_FLOATING = 15,
		STYLE_HOST_DEFINED = 65536;
	
	// 
	public final static int
		ITEM_UNIQUE = 0,
		ITEM_FIRST = -1,
		ITEM_LAST = -2,
		ITEM_DEFAULT = -3,
		ITEM_CANCEL = -4,
		ITEM_MENU = -5,
		ITEM_RESIZE = -6,
		ITEM_PRIVATE_UNIQUE = -7,
		ITEM_FIRST_UNUSED_PRIVATE = -8;
	
	// 
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
	
	/**
	 * used for storing the ADMDialogRef in this object
	 */
	private int dialogRef = 0;
	
	private String name;
	private ArrayList items;
	private boolean doesModal = false;
	
	private int style;
	private int options;

	private Dimension size;
	private Rectangle bounds;
	private String title;
	
	protected AWTContainer container = null;
	
	private Function onCreate = null;
	
	private static ArrayList dialogs = new ArrayList();

	/**
	 * Setting both length and bounds doesn't make sense. This private constructor is used
	 * bellow by the different other versions...
	 */
	private Dialog(int style, String title, Dimension size, Rectangle bounds, int options, Function onCreate) {
		// TODO: use an naming scheme that produce unique names that are each time the same (e.g. md5 hash of script name + title?)
		this.name = "Scriptographer_Dialog_" + dialogs.size();
		this.title = title;
		this.size = size != null ? new Dimension(size) : null;
		this.bounds = bounds != null ? new Rectangle(bounds) : null;
		this.style = style;
		this.options = options;		
		this.onCreate = onCreate;

		items = new ArrayList();
		
		// if onCreate is not set, we can create the dialog immediatelly, as the wrapper doesn't need to be set before that:
		if (onCreate == null)
			create();
	}
	
	/**
	 * Public constructors, for creating Dialogs from java...
	 */
	public Dialog(int style, String title, Dimension size, int options, Function onCreate) {
		this(style, title, size, null, options, onCreate);
	}

	public Dialog(int style, String title, Rectangle bounds, int options, Function onCreate) {
		this(style, title, null, bounds, options, onCreate);
	}

	public Dialog(int style, String title, Dimension size, int options) {
		this(style, title, size, null, options, null);
	}

	public Dialog(int style, String title, Rectangle bounds, int options) {
		this(style, title, null, bounds, options, null);
	}
	
	/**
	 * Override the default constructor behavior, so that wrapper can be set before the dialog is created.
	 * This is needed for the onCreate call within the constructor!
	 */
	public static Scriptable jsConstructor(Context context, Object[] args, Function ctorObj, boolean inNewExpr) 
	throws Exception {
		ArgumentReader reader = new ArgumentReader(args);
		Number styleObj = reader.readNumber();
		if (styleObj == null) throw new IllegalArgumentException("Please specify a dialog style");
		int style = styleObj.intValue();
		// title?
		String title = reader.readString();
		// bounds, length?
		Dimension size = null;
		Rectangle bounds = null;

		Point2D sizePoint = reader.readPoint();
		if (sizePoint != null) {
			size = new Dimension((int)sizePoint.getX(), (int)sizePoint.getY());
		} else {
			// bounds?
			Rectangle2D boundsRect = sizePoint != null ? null : reader.readRectangle();
			if (boundsRect != null) {
				bounds = new Rectangle((int)boundsRect.getX(), (int)boundsRect.getY(), (int)boundsRect.getWidth(), (int) boundsRect.getHeight());
			} else {
				throw new IllegalArgumentException("Either length or bounds need to be specified!");
			}
		}

		// options?
		Number optionsObj = reader.readNumber();
		int options = optionsObj != null ? optionsObj.intValue() : 0;
		// onCreate?
		Function onCreate = reader.readFunction();
		Dialog dialog = new Dialog(style, title, size, bounds, options, onCreate);
		
		Scriptable obj = context.getWrapFactory().wrapAsJavaObject(context, ctorObj.getParentScope(), dialog, null);

		dialog.setWrapper(obj); // already do this now as it is needed in dialog.create()!
		dialog.create();

		return obj;
	}
	
	private void create() {
		nativeCreate(name, style, options); // sets dialogRef, as it is used in onCreate already, which is called from within nativeCreate!
		
		if (dialogRef != 0) {
			dialogs.add(this);
		}
	}
	
	public void destroy() {
		nativeDestroy();
		dialogRef = 0;
		dialogs.remove(this);
	}

	public static void destroyAll() {
		for (int i = dialogs.size() - 1; i >= 0; i--) {
			Dialog dialog = (Dialog)dialogs.get(i);
			dialogs.remove(i);
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
	
	public void doLayout() {
		getContainer().doLayout();
	}

	/**
	 * onCreate is directly called from the dialog's initProc, and it calls the javascript
	 * maxVersion of it which was passed to the Constructor. onCreate is the place for more
	 * initialization and creation of interface elements, before the dialog is finally drawn
	 */
	protected void onCreate() throws Exception {
		// if length, bounds, or title was set in the constructor, finally set them here,
		// otherwise sync them with the internal segmentValues:
		if  (size != null) {
			nativeSetSize(size.width, size.height);
		} else {
			size = nativeGetSize();
		}
		
		if (bounds != null) {
			nativeSetBounds(bounds.x, bounds.y, bounds.width, bounds.height);
		} else {
			bounds = nativeGetBounds();
		}
		
		if (title != null) {
			nativeSetTitle(title);
		} else {
			title = nativeGetTitle();
		}
		
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, onCreate);
		
		// if a container was created, calculate the layout now:
		if (container != null) {
			setMinimumSize(container.getMinimumSize());
			setSize(container.getPreferredSize());
			// TODO: This seems to crash the whole thing:
			// setMaximumSize(container.getMaximumSize());
			container.doLayout();
		}
	}

	protected void onResize(int dx, int dy) throws Exception {
		// if a contianer was created, the layout needs to be recalculated now:
		if (container != null) {
			container.updateSize(size);
			container.doLayout();
		}
		super.onResize(dx, dy);
	}

	/*
	 * Wrapper stuff:
	 */
	
	/* TODO: Check these:
	 * - item accessors....
	 * - timer stuff
	 * - createNestedItem(...);
	 * - beginAdjustingFocusOrder, doneAdjustingFocusOrder
	 */

	/*
	 * dialog creation/destruction
	 * 
	 */

	private native void nativeCreate(String name, int dialogStyle, int options);
	private native void nativeDestroy();

	/*
	 * Handler activation / deactivation
	 */
	protected native void nativeSetTrackCallbackEnabled(boolean enabled);
	protected native void nativeSetDrawCallbackEnabled(boolean enabled);

	/*
	 * modal dialogs
	 *
	 */
	
	public native Item doModal(boolean popup);
	public native void endModal();
	
	public Item doModal() {
		return doModal(false);
	}
	
	public Item doPopupModal() {
		return doModal(true);
	}
		
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
		if (bounds.x != x || bounds.y != y) {
			bounds.setRect(x, y, width, height);
			nativeSetBounds(x, y, width, height);
			if (bounds.width != width || bounds.height != height) {
				// also updatePoint the internal length field:
				size = nativeGetSize();
				if (container != null) {
					container.updateSize(size);
				}
			}
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

	private native String nativeGetTitle();
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

	/* 
	 * item accessors
	 * 
	 */

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
