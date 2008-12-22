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
 * File created on 21.12.2004.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scriptographer.ScriptographerEngine; 
import com.scriptographer.ScriptographerException;
import com.scriptographer.ui.Image;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;
import com.scratchdisk.util.IntMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author lehni
 */
public class Tool extends NativeObject {
	// AIToolOptions
	public static final int
/*
 *	This option is always on, since we're changing cursors
 * 		OPTION_TRACK_CURSOR = 1 << 0,
 */
/** Set to disable automatic scrolling. When off (the default), the Illustrator window
	scrolls when a tool reaches the edge. For tools that manipulate artwork,
	autoscroll is useful. Set this to turn autoscroll off for a tool that
	draws to the screen directly, like the built-in Brush tool. */
		OPTION_NO_AUTO_SCROLL = 1 << 1,
/**	Set to buffer the drag selectors and messages and send all of them
	to the tool at once. Useful if a tool is calculation intensive.  The effect
	is no longer real-time, but has a smoother final output.
	When off (the default), the tool processes drag selectors and returns frequently,
	resulting in near real-time feedback. If there are intensive calculations
	during the drag selector, the tool could miss drag notifications, resulting in rougher
	tracking.  */
		OPTION_BUFFERED_DRAGGING = 1 << 2,
/** Set to maintain the edit context when this tool is selected. For art objects,
	keeps all current points and handles selected. For text, keeps the insertion
	point in the current location. Set this option for navigational tools like
	the Zoom and Scroll tools. */
		OPTION_MAINTAIN_EDIT_CONTEXT = 1 << 3,
/** Set to maintain the text edit context when the tool is selected,
	if \c #kToolMaintainEditContextOption is also set. */
		OPTION_IS_TEXT_TOOL = 1 << 4,
/** Set to receive \c #kSelectorAIToolDecreaseDiameter and
	\c #kSelectorAIToolIncreaseDiameter. Use if the tool needs to change
	diameter when either '[' or ']' is pressed. */
		OPTION_CHANGE_DIAMETER = 1 << 5;
	
	// TODO: implement a way to set cursors?
	private int cursor = 128;

	/**
	 * tools maps tool handles to their wrappers.
	 */
	private static IntMap<Tool> tools = new IntMap<Tool>();
	private static ArrayList<Tool> unusedTools = null;


	private float distanceThreshold;
	
	private Scope scope;
	private Event event = new Event();

	private boolean firstMove = true;

	private Image icon = null;

	private String name;

	public Tool(String name, int options, Tool groupTool, Tool toolsetTool) {
		this.name = name;

		ArrayList<Tool> unusedTools = getUnusedTools();

		// Now see first whether there is an unusedEffect already that fits this
		// description
		int index = unusedTools.indexOf(this);
		if (index >= 0) {
			// Found one, let's reuse it's handle and remove the old effect from
			// the list:
			Tool tool = unusedTools.get(index);
			handle = tool.handle;
			tool.handle = 0;
			unusedTools.remove(index);
		} else {
			// No previously existing effect found, create a new one:
			handle = nativeCreate(name,
					options,
					groupTool != null ? groupTool.handle : 0,
					toolsetTool != null ? toolsetTool.handle : 0);
		}

		if (handle == 0)
			throw new ScriptographerException("Unable to create Tool.");

		tools.put(handle, this);
	}

	public Tool(String name, int options, Tool groupTool) {
		this(name, options, groupTool, null);
	}

	public Tool(String name, int options) {
		this(name, options, null, null);
	}

	public Tool(String name) {
		this(name, 0, null, null);
	}

	/**
	 * @param title
	 * @return
	 */
	private native int nativeCreate(String name, int options, int groupHandle, int toolsetHandle);

	protected Tool(int handle, String name) {
		super(handle);
		// See resourceIds.h:
		this.name = name;
	}
	
	public void compileScript(File file) throws ScriptException, IOException {
		onInit = null;
		onOptions = null;
		onSelect = null;
		onDeselect = null;
		onReselect = null;
		onMouseDown = null;
		onMouseUp = null;
		onMouseDrag = null;
		onMouseMove = null;
		// Tell onMouseMove to initialize event.delta and event.count
		firstMove = true;
		event = new Event();
		setDistanceThreshold(0);
		setEventInterval(-1);
		ScriptEngine engine = ScriptEngine.getEngineByFile(file);
		if (engine == null)
			throw new ScriptException("Unable to find script engine for " + file);
		// Execute in the tool's scope so setIdleEventInterval can be called
		scope = engine.getScope(this);
		ScriptographerEngine.execute(file, scope);
		if (scope != null) {
			try {
				onInit();
			} catch (ScriptException e) {
				// Rethrow
				throw e;
			} catch (Exception e) {
				// cannot happen with scripts
			}
		}
	}

	public native boolean hasPressure();
	
	// Interval time in milliseconds
	public native int getEventInterval();
	
	public native void setEventInterval(int interval);

	public native String getTitle();

	public native void setTitle(String title);

	public native String getTooltip();

	public native void setTooltip(String text);

	public native int getOptions();

	public native void setOptions(int options);

	public Image getImage() {
		return icon;
	}

	private native void nativeSetImage(int iconHandle);

	public void setImage(Image image) {
		nativeSetImage(image != null ? image.createIconHandle() : 0);
		this.icon = image;
	}

	/**
	 * @deprecated use Tool#setEventInterval instead.
	 */
	public void setIdleEventInterval(int interval) {
		setEventInterval(interval);
	}

	public float getDistanceThreshold() {
		return distanceThreshold;
	}

	/**
	 * @param threshold
	 */
	public void setDistanceThreshold(float threshold) {
		distanceThreshold = threshold;
	}

	private Callable onInit;

	public Callable getOnInit() {
		return onInit;
	}

	public void setOnInit(Callable onInit) {
		this.onInit = onInit;
	}
	
	protected void onInit() throws Exception {
		if (scope != null && onInit != null)
			ScriptographerEngine.invoke(onInit, this);
	}

	/*
	 * TODO: onOptions should be called onEditOptions, or both onOptions,
	 * but at least the same.
	 */
	private Callable onOptions;

	public Callable getOnOptions() {
		return onOptions;
	}

	public void setOnOptions(Callable onOptions) {
		this.onOptions = onOptions;
	}

	protected void onOptions() throws Exception {
		if (scope != null && onOptions != null)
			ScriptographerEngine.invoke(onOptions, this);
	}

	private Callable onSelect;

	public Callable getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(Callable onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() throws Exception {
		if (scope != null && onSelect != null)
			ScriptographerEngine.invoke(onSelect, this);
	}
	
	private Callable onDeselect;

	public Callable getOnDeselect() {
		return onDeselect;
	}

	public void setOnDeselect(Callable onDeselect) {
		this.onDeselect = onDeselect;
	}
	
	protected void onDeselect() throws Exception {
		if (scope != null && onDeselect != null)
			ScriptographerEngine.invoke(onDeselect, this);
	}

	private Callable onReselect;

	public Callable getOnReselect() {
		return onReselect;
	}

	public void setOnReselect(Callable onReselect) {
		this.onReselect = onReselect;
	}

	protected void onReselect() throws Exception {
		if (scope != null && onReselect != null)
			ScriptographerEngine.invoke(onReselect, this);
	}

	private Callable onMouseDown;

	public Callable getOnMouseDown() {
		return onMouseDown;
	}

	public void setOnMouseDown(Callable onMouseDown) {
		this.onMouseDown = onMouseDown;
	}
	
	protected void onMouseDown(Event event) throws Exception {
		if (scope != null && onMouseDown != null)
			ScriptographerEngine.invoke(onMouseDown, this, event);
	}

	private Callable onMouseDrag;

	public Callable getOnMouseDrag() {
		return onMouseDrag;
	}

	public void setOnMouseDrag(Callable onMouseDrag) {
		this.onMouseDrag = onMouseDrag;
	}
	
	protected void onMouseDrag(Event event) throws Exception {
		if (scope != null && onMouseDrag != null)
			ScriptographerEngine.invoke(onMouseDrag, this, event);
	}

	private Callable onMouseMove;

	public Callable getOnMouseMove() {
		return onMouseMove;
	}

	public void setOnMouseMove(Callable onMouseMove) {
		this.onMouseMove = onMouseMove;
	}
	
	protected void onMouseMove(Event event) throws Exception {
		// Make sure the first move event initializes both delta and count.
		if (scope != null && onMouseMove != null)
			ScriptographerEngine.invoke(onMouseMove, this, event);
	}

	private Callable onMouseUp;

	public Callable getOnMouseUp() {
		return onMouseUp;
	}

	public void setOnMouseUp(Callable onMouseUp) {
		this.onMouseUp = onMouseUp;
	}
		
	protected void onMouseUp(Event event) throws Exception {
		if (scope != null && onMouseUp != null)
			ScriptographerEngine.invoke(onMouseUp, this, event);
	}

	private static final int EVENT_EDIT_OPTIONS = 0;
	private static final int EVENT_TRACK_CURSOR = 1;
	private static final int EVENT_MOUSE_DOWN = 2;
	private static final int EVENT_MOUSE_DRAG = 3;
	private static final int EVENT_MOUSE_UP = 4;
	private static final int EVENT_SELECT = 5;
	private static final int EVENT_DESELECT = 6;
	private static final int EVENT_RESELECT = 7;
	// TODO: not implemented yet:
	/*
	private static final int EVENT_DECREASE_DIAMETER = 8;
	private static final int EVENT_INCREASE_DIAMETER = 9;
	*/

	private final static String[] eventTypes = {
		"AI Edit Options",
		"AI Track Cursor",
		"AI Mouse Down",
		"AI Mouse Drag",
		"AI Mouse Up",
		"AI Select",
		"AI Deselect",
		"AI Reselect",
		// TODO: not implemented yet:
		"AI Decrease Diameter",
		"AI Increase Diameter"
	};
	// Hashmap for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap<String, Integer> events = new HashMap<String, Integer>();

	static {
		for (int i = 0; i < eventTypes.length; i++)
			events.put(eventTypes[i], i);
	}

	/**
	 * To be called from the native environment. Returns the cursor
	 * id to be set, if any.
	 */
	@SuppressWarnings("unused")
	private static int onHandleEvent(int handle, String selector, float x,
			float y, int pressure) throws Exception {
		Tool tool = getTool(handle);
		if (tool != null) {
			Integer event = (Integer) events.get(selector); 
			if (event != null) {
				switch(event.intValue()) {
					case EVENT_EDIT_OPTIONS:
						tool.onOptions();
						break;
					case EVENT_MOUSE_DOWN:
						tool.event.setValues(x, y, pressure, 0, true, true);
						tool.onMouseDown(tool.event);
						break;
					case EVENT_MOUSE_DRAG:
						if (tool.event.setValues(x, y, pressure, tool.distanceThreshold, false, false))
							tool.onMouseDrag(tool.event);
						break;
					case EVENT_MOUSE_UP:
						tool.event.setValues(x, y, pressure, 0, false, false);
						try {
							tool.onMouseUp(tool.event);
						} finally {
							// Start with new values for EVENT_TRACK_CURSOR
							tool.event.setValues(x, y, pressure, 0, true, false);
							tool.firstMove = true;
						}
						break;
					case EVENT_TRACK_CURSOR:
						try {
							if (tool.event.setValues(x, y, pressure, tool.distanceThreshold, tool.firstMove, false))
								tool.onMouseMove(tool.event);
						} finally {
							tool.firstMove = false;
						}
						// Tell the native side to update the cursor
						return tool.cursor;
					case EVENT_SELECT:
						tool.onSelect();
						break;
					case EVENT_DESELECT:
						tool.onDeselect();
						break;
					case EVENT_RESELECT:
						tool.onReselect();
						break;
				}
			}
		}
		return 0;
	}

	private static Tool getTool(int handle) {
		return tools.get(handle);
	}

	public boolean equals(Object obj) {
		if (obj instanceof Tool) {
			Tool tool = (Tool) obj;
			return name.equals(tool.name);
		}
		return false;
	}

	private static ArrayList<Tool> getUnusedTools() {
		if (unusedTools == null)
			unusedTools = nativeGetTools();
		return unusedTools;
	}

	private static native ArrayList<Tool> nativeGetTools();
}
