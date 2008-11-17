/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 23.02.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import java.util.ArrayList;

import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntMap;
import com.scratchdisk.util.SoftIntMap;
import com.scriptographer.ui.Drawer;

/**
 * @author lehni
 */
public class Annotator extends NativeObject {
	private boolean active;

	private static IntMap<Annotator> annotators = new IntMap<Annotator>();
	private static ArrayList<Annotator> unusedAnnotators = null;
	// this is list of drawers that map viewports to created ADM Drawer objects:
	private static SoftIntMap<Drawer> drawers = new SoftIntMap<Drawer>();
	private static int counter = 0;
	
	public Annotator() {
		// now see first whether there is an unusedEffect already:
		ArrayList unusedAnnotators = getUnusedAnnotators();
		
		int index = unusedAnnotators.size() - 1;
		if (index >= 0) {
			Annotator annotator = (Annotator) unusedAnnotators.get(index);
			// found one, let's reuse it's handle and remove the old timer from
			// the list:
			handle = annotator.handle;
			annotator.handle = 0;
			unusedAnnotators.remove(index);
		} else {
			handle = nativeCreate("Scriptographer Annotator " + (counter++));
		}		

		if (handle == 0)
			throw new ScriptographerException("Unable to create Annotator.");
		
		active = false;
		
		annotators.put(handle, this);
	}
	
	/**
	 * Called from the native environment.
	 */
	protected Annotator(int handle) {
		super(handle);
	}
	
	private native int nativeCreate(String name);
	
	/**
	 * @param active if <code>true</code>, activates the annotator, otherwise
	 *        deactivates it
	 */
	public void setActive(boolean active) {
		if (nativeSetActive(handle, active)) {
			this.active = active;
		}
	}

	public boolean isActive() {
		return active;
	}
	
	private native boolean nativeSetActive(int handle, boolean active);
	
	public void invalidate(int x, int y, int width, int height) {
		nativeInvalidate(handle, x, y, width, height);
	}
	
	public void invalidate(Rectangle rect) {
		// TODO: implement DocumentView and pass handle to it!
		nativeInvalidate(handle, (int) rect.x, (int) rect.y,
				(int) rect.width, (int) rect.height);
	}
	
	private native void nativeInvalidate(int viewHandle, int x, int y,
			int width, int height);
	
	public void dispose() {
		// see whether we're still linked:
		if (annotators.get(handle) == this) {
			// if so remove it and put it to the list of unused timers, for later
			// recycling
			annotators.remove(handle);
			getUnusedAnnotators().add(this);
		}
	}
	
	public static void disposeAll() {
		// As remove() modifies the map, using an iterator is not possible here:
		Object[] annotators = Annotator.annotators.values().toArray();
		for (int i = 0; i < annotators.length; i++)
			((Annotator) annotators[i]).dispose();
		
		// also clean up the port drawers:
		Object[] drawers = Annotator.drawers.values().toArray();
		for (int i = 0; i < drawers.length; i++)
			((Drawer) drawers[i]).dispose();
	}
	
	private static ArrayList<Annotator> getUnusedAnnotators() {
		if (unusedAnnotators == null)
			unusedAnnotators = nativeGetAnnotators();
		return unusedAnnotators;
	}

	private static native ArrayList<Annotator> nativeGetAnnotators();

	private Callable onDraw = null;

	public void setOnDraw(Callable onDraw) {
		this.onDraw = onDraw;
		this.setActive(onDraw != null);
	}
	
	public Callable getOnDraw() {
		return onDraw;
	}

	protected void onDraw(Drawer drawer, DocumentView view) throws Exception {
		if (onDraw != null)
			ScriptographerEngine.invoke(onDraw, this, drawer, view);
	}

	private Callable onInvalidate = null;

	public void setOnInvalidate(Callable onInvalidate) {
		this.onInvalidate = onInvalidate;
	}
	
	public Callable getOnInvalidate() {
		return onInvalidate;
	}

	protected void onInvalidate() throws Exception {
		if (onInvalidate != null)
			ScriptographerEngine.invoke(onInvalidate, this);
	}

	/**
	 * To be called from the native environment:
	 */
	@SuppressWarnings("unused")
	private static void onDraw(int handle, int portHandle, int viewHandle, int docHandle)
			throws Exception {
		Annotator annotator = getAnnotator(handle);
		if (annotator != null) {
			annotator.onDraw(createDrawer(portHandle),
					DocumentView.wrapHandle(viewHandle, Document.wrapHandle(docHandle)));
		}
	}
	
	@SuppressWarnings("unused")
	private static void onInvalidate(int handle) throws Exception {
		Annotator annotator = getAnnotator(handle);
		if (annotator != null) {
			annotator.onInvalidate();
		}
	}

	private static Annotator getAnnotator(int handle) {
		return (Annotator) annotators.get(handle);
	}

	/**
	 * Returns a Drawer for the passed portHandle.
	 * The drawers are cashed and reused for the same port.
	 * 
	 * @param portHandle
	 * @return
	 */
	private static Drawer createDrawer(int portHandle) {
		Drawer drawer = (Drawer) drawers.get(portHandle);
		if (drawer == null) {
			drawer = nativeCreateDrawer(portHandle);
			drawers.put(portHandle, drawer);
		}
		return drawer;
	}
	
	private static native Drawer nativeCreateDrawer(int portHandle);

	protected void finalize() {
		dispose();
	}
}
