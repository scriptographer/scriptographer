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
 * File created on 02.01.2005.
 *
 * $RCSfile: CallbackHandler.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/07 13:35:07 $
 */

package com.scriptographer.adm;

import org.mozilla.javascript.Function;

import com.scriptographer.js.Unsealed;
import com.scriptographer.js.WrappableObject;
import com.scriptographer.js.FunctionHelper;

/**
 * The NotificationListener just recieves notifictaions from native code.
 * It can be a Dialog, Item or ListEntry
 * 
 * It implements Wrappable and Unsealed in order to provide the JS environment
 * a means of setting callback functions and calling these from Java.
 * 
 * @author Lehni
 */
public abstract class CallbackHandler extends WrappableObject implements Unsealed {
	private Tracker tracker = new Tracker();
 	private Drawer drawer = new Drawer();
	
	private Function onTrack = null;
	private Function onDraw = null;
	private Function onResize = null;
	
	private Object[] zeroArgs = new Object[0];
	private Object[] oneArg = new Object[1];
	private Object[] twoArgs = new Object[2];

	/*
	 * Use an actiavtion mechanism for the expensive callback routines (the ones that
	 * get called often). These are only activated if the user actually sets a callback
	 * functions.
	 *
	 * Abstract declarations for native functions that enable and disable the native
	 * track and draw proc procedures:
	 */
	private boolean trackCallbackEnabled = false;
	private boolean drawCallbackEnabled = false;

	abstract protected void nativeSetTrackCallbackEnabled(boolean enabled);
	abstract protected void nativeSetDrawCallbackEnabled(boolean enabled);

	public void setTrackCallbackEnabled(boolean enabled) {
		nativeSetTrackCallbackEnabled(enabled);
		trackCallbackEnabled = enabled;
	}

	public boolean isTrackCallbackEnabled() {
		return trackCallbackEnabled;
	}

	public void setDrawCallbackEnabled(boolean enabled) {
		nativeSetDrawCallbackEnabled(enabled);
		drawCallbackEnabled = enabled;
	}

	public boolean isDrawCallbackEnabled() {
		return drawCallbackEnabled;
	}

	public void setOnTrack(Function func) {
		nativeSetTrackCallbackEnabled(func != null);
		onTrack = func;
	}

	public Function getOnTrack() {
		return onTrack;
	}

	protected void onTrack(Tracker tracker, ListEntry entry) throws Exception {
		if (wrapper != null && onTrack != null) {
			Object[] params;
			if (entry == null) {
				oneArg[0] = tracker;
				params = oneArg;
			} else {
				twoArgs[0] = tracker;
				twoArgs[1] = entry;
				params = twoArgs;
			}
			FunctionHelper.callFunction(wrapper, onTrack, params);
		}
	}

	public void setOnDraw(Function func) {
		nativeSetDrawCallbackEnabled(func != null);
		onDraw = func;
	}

	public Function getOnDraw() {
		return onTrack;
	}

	protected void onDraw(Drawer drawer, ListEntry entry) throws Exception {
		if (wrapper != null && onDraw != null) {
			Object[] params;
			if (entry == null) {
				oneArg[0] = drawer;
				params = oneArg;
			} else {
				twoArgs[0] = drawer;
				twoArgs[1] = entry;
				params = twoArgs;
			}
			FunctionHelper.callFunction(wrapper, onDraw, params);
		}
	}

	public void setOnResize(Function onResize) {
		this.onResize = onResize;
	}

	public Function getOnResize() {
		return onResize;
	}

	protected void onResize(int dx, int dy) throws Exception {
		if (wrapper != null && onResize != null) {
			twoArgs[0] = new Integer(dx);
			twoArgs[1] = new Integer(dy);
			FunctionHelper.callFunction(wrapper, onResize, twoArgs);
		}
	}

	protected void onNotify(int notifier, ListEntry entry) throws Exception {
		if (wrapper != null) {
			Object[] params;
			if (entry == null) {
				params = zeroArgs;
			} else {
				oneArg[0] = entry;
				params = oneArg;
			}
			switch (notifier) {
				case Notifier.NOTIFIER_CLOSE_HIT:
					FunctionHelper.callFunction(wrapper, "onClose", params);
					break;
				case Notifier.NOTIFIER_ZOOM_HIT:
					FunctionHelper.callFunction(wrapper, "onZoom", params);
					break;
				case Notifier.NOTIFIER_CYCLE:
					FunctionHelper.callFunction(wrapper, "onCycle", params);
					break;
				case Notifier.NOTIFIER_COLLAPSE:
					FunctionHelper.callFunction(wrapper, "onCollapse", params);
					break;
				case Notifier.NOTIFIER_EXPAND:
					FunctionHelper.callFunction(wrapper, "onExpand", params);
					break;
				case Notifier.NOTIFIER_CONTEXT_MENU_CHANGED:
					FunctionHelper.callFunction(wrapper, "onContextMenuChange", params);
					break;
				case Notifier.NOTIFIER_WINDOW_SHOW:
					FunctionHelper.callFunction(wrapper, "onShow", params);
					break;
				case Notifier.NOTIFIER_WINDOW_HIDE:
					FunctionHelper.callFunction(wrapper, "onHide", params);
					break;
				case Notifier.NOTIFIER_WINDOW_DRAG_MOVED:
					FunctionHelper.callFunction(wrapper, "onMove", params);
					break;
				case Notifier.NOTIFIER_WINDOW_ACTIVATE:
					FunctionHelper.callFunction(wrapper, "onActivate", params);
					break;
				case Notifier.NOTIFIER_WINDOW_DEACTIVATE:
					FunctionHelper.callFunction(wrapper, "onDeactivate", params);
					break;
				case Notifier.NOTIFIER_NUMBER_OUT_OF_BOUNDS:
					FunctionHelper.callFunction(wrapper, "onNumberOutOfBounds", params);
					break;
				case Notifier.NOTIFIER_USER_CHANGED:
					FunctionHelper.callFunction(wrapper, "onChange", params);
					break;
				case Notifier.NOTIFIER_INTERMEDIATE_CHANGED:
					FunctionHelper.callFunction(wrapper, "onPreChange", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_CUT:
					FunctionHelper.callFunction(wrapper, "onPreCut", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_CUT:
					FunctionHelper.callFunction(wrapper, "onCut", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_COPY:
					FunctionHelper.callFunction(wrapper, "onPreCopy", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_COPY:
					FunctionHelper.callFunction(wrapper, "onCopy", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_PASTE:
					FunctionHelper.callFunction(wrapper, "onPrePaste", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_PASTE:
					FunctionHelper.callFunction(wrapper, "onPaste", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_CLEAR:
					FunctionHelper.callFunction(wrapper, "onPreClear", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_CLEAR:
					FunctionHelper.callFunction(wrapper, "onClear", params);
					break;
				case Notifier.NOTIFIER_PRE_TEXT_SELECTION_CHANGED:
					FunctionHelper.callFunction(wrapper, "onPreSelectionChange", params);
					break;
				case Notifier.NOTIFIER_TEXT_SELECTION_CHANGED:
					FunctionHelper.callFunction(wrapper, "onSelectionChange", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_REDO:
					FunctionHelper.callFunction(wrapper, "onPreRedo", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_REDO:
					FunctionHelper.callFunction(wrapper, "onRedo", params);
					break;
				case Notifier.NOTIFIER_PRE_CLIPBOARD_UNDO:
					FunctionHelper.callFunction(wrapper, "onPreUndo", params);
					break;
				case Notifier.NOTIFIER_POST_CLIPBOARD_UNDO:
					FunctionHelper.callFunction(wrapper, "onUndo", params);
					break;
			}
		}
	}

	protected final void onNotify(String notifier, ListEntry entry) throws Exception {
//		System.out.println(this + " " + notifier);
		onNotify(Notifier.lookup(notifier), entry);
	}

	protected void onDestroy(ListEntry entry) throws Exception {
		if (wrapper != null) {
			if (entry != null) {
				oneArg[0] = entry;
				FunctionHelper.callFunction(wrapper, "onDestroy", oneArg);
			} else {
				FunctionHelper.callFunction(wrapper, "onDestroy");
			}
		}
	}
}
