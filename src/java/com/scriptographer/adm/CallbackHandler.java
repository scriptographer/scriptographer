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
 * $Revision: 1.3 $
 * $Date: 2005/03/10 22:48:43 $
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
	
 	protected Function onTrack = null;
	protected Function onDraw = null;
	protected Function onResize = null;
	
	protected static Object[] zeroArgs = new Object[0];
	protected Object[] oneArg = new Object[1];
	protected Object[] twoArgs = new Object[2];

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
		setTrackCallbackEnabled(func != null);
		onTrack = func;
	}

	public Function getOnTrack() {
		return onTrack;
	}

	protected void onTrack(Tracker tracker) throws Exception {
		if (wrapper != null && onTrack != null) {
			oneArg[0] = tracker;
			FunctionHelper.callFunction(wrapper, onTrack, oneArg);
		}
	}

	public void setOnDraw(Function func) {
		setDrawCallbackEnabled(func != null);
		onDraw = func;
	}

	public Function getOnDraw() {
		return onTrack;
	}

	protected void onDraw(Drawer drawer) throws Exception {
		if (wrapper != null && onDraw != null) {
			oneArg[0] = drawer;
			FunctionHelper.callFunction(wrapper, onDraw, oneArg);
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
	
	protected abstract void onNotify(int notifier, ListEntry entry) throws Exception;

	protected final void onNotify(String notifier, ListEntry entry) throws Exception {
//		System.out.println(this + " " + notifier);
		onNotify(Notifier.lookup(notifier), entry);
	}

	protected void onDestroy() throws Exception {
		callFunction("onDestroy");
	}
	
	protected void callFunction(String name) throws Exception {
		if (wrapper != null)
			FunctionHelper.callFunction(wrapper, name, zeroArgs);
	}
	
	protected void callFunction(String name, Object param1) throws Exception {
		if (wrapper != null) {
			oneArg[0] = param1;
			FunctionHelper.callFunction(wrapper, name, oneArg);
		}
	}
	
	protected void callFunction(String name, Object param1, Object param2) throws Exception {
		if (wrapper != null) {
			twoArgs[0] = param1;
			twoArgs[1] = param2;
			FunctionHelper.callFunction(wrapper, name, twoArgs);
		}
	}
}
