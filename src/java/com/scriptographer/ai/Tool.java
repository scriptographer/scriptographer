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
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;
import com.scratchdisk.util.IntMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author lehni
 */
public class Tool extends NativeObject {
	private int index;
	private int cursor;

	private static IntMap<Tool> tools = null;

	private float distanceThreshold;
	
	private Scope scope;
	private Event event = new Event();

	private boolean firstMove = true;

	protected Tool(int handle, int index) {
		super(handle);
		this.index = index;
		// See resourceIds.h:
		this.cursor = index + 128;
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
		if (engine != null) {
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
	}

	private static IntMap<Tool> getTools() {
		if (tools == null)
			tools = nativeGetTools();
		return tools;
	}

	/**
	 * Returns all tools that have been created by this plugin. This is
	 * necessary because the java part of the plugin may be reloaded. The plugin
	 * needs to be capable of reestablish the connections between the wrappers
	 * and the real objects.
	 * 
	 * @return
	 */
	private static native IntMap<Tool> nativeGetTools();

	public native boolean hasPressure();
	
	// Interval time in milliseconds
	public native int getEventInterval();
	
	public native void setEventInterval(int interval);

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

	private final static String[] eventTypes = {
		"AI Edit Options",
		"AI Track Cursor",
		"AI Mouse Down",
		"AI Mouse Drag",
		"AI Mouse Up",
		"AI Select",
		"AI Deselect",
		"AI Reselect"
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
		Tool tool = getToolByHandle(handle);
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

	public static Tool getTool(int index) {
		for (Iterator iterator = getTools().values().iterator();
			iterator.hasNext();) {
			Tool tool = (Tool) iterator.next();
			if (tool.index == index)
				return tool;
		}
		return null;
	}

	private static Tool getToolByHandle(int handle) {
		return (Tool) getTools().get(handle);
	}
}
