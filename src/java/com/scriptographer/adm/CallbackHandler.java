/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Revision: 1.9 $
 * $Date: 2006/12/11 18:50:24 $
 */

package com.scriptographer.adm;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;

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

/**
 * Subclasses the NotificationHandler and adds functionality for
 * defining track and draw callbacks (NotificationHandler can handle
 * these but not set them).
 * It also adds the onResize handler as both Item and Dialog need it,
 * and these are the only subclasses of CallbackHandler 
 */
abstract class CallbackHandler extends NotificationHandler {
	/*
	 * Use an actiavtion mechanism for the expensive callback routines (the ones that
	 * get called often). These are only activated if the user actually sets a callback
	 * functions.
	 *
	 * Abstract declarations for native functions that enable and disable the native
	 * track and draw proc procedures:
	 */
	private boolean trackCallback = false;
	private boolean drawCallback = false;

	abstract protected void nativeSetTrackCallback(boolean enabled);
	abstract protected void nativeSetDrawCallback(boolean enabled);

	public void setTrackCallback(boolean enabled) {
		nativeSetTrackCallback(enabled);
		trackCallback = enabled;
	}

	public boolean getTrackCallback() {
		return trackCallback;
	}

	public void setDrawCallback(boolean enabled) {
		nativeSetDrawCallback(enabled);
		drawCallback = enabled;
	}

	public boolean getDrawCallback() {
		return drawCallback;
	}
	
	abstract public void setTrackMask(int mask);
	abstract public int getTrackMask();

 	protected Function onTrack = null;
	protected Function onDraw = null;

	public void setOnTrack(Function func) {
		setTrackCallback(func != null);
		onTrack = func;
	}

	public Function getOnTrack() {
		return onTrack;
	}

	protected boolean onTrack(Tracker tracker) throws Exception {
		if (wrapper != null && onTrack != null) {
			Object result = FunctionHelper.callFunction(wrapper, onTrack, new Object[] { tracker });
			if (result != null)
				return ScriptRuntime.toBoolean(result);
		}
		return true;
	}

	public void setOnDraw(Function func) {
		setDrawCallback(func != null);
		onDraw = func;
	}

	public Function getOnDraw() {
		return onTrack;
	}

	protected void onDraw(Drawer drawer) throws Exception {
		if (wrapper != null && onDraw != null) {
			FunctionHelper.callFunction(wrapper, onDraw, new Object[] { drawer });
		}
	}
	
	protected Function onResize = null;

	public void setOnResize(Function func) {
		onResize = func;
	}

	public Function getOnResize() {
		return onResize;
	}

	protected void onResize(int dx, int dy) throws Exception {
		if (wrapper != null && onResize != null) {
			FunctionHelper.callFunction(wrapper, onResize, new Object[] { new Integer(dx), new Integer(dy) });
		}
	}
}
