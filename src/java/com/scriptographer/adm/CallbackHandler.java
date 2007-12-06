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
 * File created on 02.01.2005.
 *
 * $Id:CallbackHandler.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.adm;

import com.scriptographer.ScriptographerEngine; 
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.ConversionUtils;

/**
 * Subclasses the NotificationHandler and adds functionality for
 * defining track and draw callbacks (NotificationHandler can handle
 * these but not set them).
 * It also adds the onResize handler as both Item and Dialog need it,
 * and these are the only subclasses of CallbackHandler 
 *
 * @author lehni
 */
abstract class CallbackHandler extends NotificationHandler {
	/*
	 * Use an actiavtion mechanism for the expensive callback routines (the ones
	 * that get called often). These are only activated if the user actually
	 * sets a callback functions.
	 * 
	 * Abstract declarations for native functions that enable and disable the
	 * native track and draw proc procedures:
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

 	protected Callable onTrack = null;
	protected Callable onDraw = null;

	public void setOnTrack(Callable func) {
		setTrackCallback(func != null);
		onTrack = func;
	}

	public Callable getOnTrack() {
		return onTrack;
	}

	protected boolean onTrack(Tracker tracker) throws Exception {
		// Retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onTrack = this.getOnTrack();
		if (onTrack != null) {
			Object result = ScriptographerEngine.invoke(onTrack, this,
					new Object[] { tracker });
			if (result != null)
				return ConversionUtils.toBoolean(result);
		}
		return true;
	}

	public void setOnDraw(Callable func) {
		setDrawCallback(func != null);
		onDraw = func;
	}

	public Callable getOnDraw() {
		return onDraw;
	}

	protected void onDraw(Drawer drawer) throws Exception {
		// Retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onDraw = this.getOnDraw();
		if (onDraw != null)
			ScriptographerEngine.invoke(onDraw, this,
					new Object[] { drawer });
	}
	
	protected Callable onResize = null;

	public void setOnResize(Callable func) {
		onResize = func;
	}

	public Callable getOnResize() {
		return onResize;
	}

	protected void onResize(int dx, int dy) throws Exception {
		// Retrieve through getter so it can be overriden by subclasses,
		// e.g. HierarchyList
		Callable onResize = this.getOnResize();
		if (onResize != null)
			ScriptographerEngine.invoke(onResize, this,
					new Object[] { new Integer(dx), new Integer(dy) });
	}
}
