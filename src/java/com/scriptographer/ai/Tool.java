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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntMap;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.ui.Image;

/**
 * @author lehni
 */
public class Tool extends NativeObject {
	// TODO: implement a way to set cursors?
	private int cursor = 128;

	/**
	 * tools maps tool handles to their wrappers.
	 */
	private static IntMap<Tool> tools = new IntMap<Tool>();
	private static ArrayList<Tool> unusedTools = null;


	private float distanceThreshold;
	
	private MouseEvent event = new MouseEvent();

	private boolean firstMove = true;

	private Image image = null;
	private Image rolloverImage = null;

	private String name;

	/**
	 * @jshide
	 */
	public Tool(String name, EnumSet<ToolOption> options, Tool groupTool, Tool toolsetTool) {
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
					IntegerEnumUtils.getFlags(options),
					groupTool != null ? groupTool.handle : 0,
					toolsetTool != null ? toolsetTool.handle : 0);
		}

		if (handle == 0)
			throw new ScriptographerException("Unable to create Tool.");

		tools.put(handle, this);
	}

	public Tool(String name, EnumSet<ToolOption> options, Tool groupTool) {
		this(name, options, groupTool, null);
	}

	public Tool(String name, EnumSet<ToolOption> options) {
		this(name, options, null, null);
	}

	public Tool(String name) {
		this(name, null, null, null);
	}

	/**
	 * @param title
	 */
	private native int nativeCreate(String name, int options, int groupHandle, int toolsetHandle);

	protected Tool(int handle, String name) {
		super(handle);
		// See resourceIds.h:
		this.name = name;
	}

	/**
	 * Resets the tool's settings, so a new tool can be assigned to it
	 * 
	 * @jshide
	 */
	public void reset() {
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
		event = new MouseEvent();
		setDistanceThreshold(0);
		setEventInterval(-1);
	}

	/**
	 * Checks whether the input device of the user has a pressure feature (i.e. a
	 * drawing tablet)
	 */
	public native boolean hasPressure();

	/**
	 * Sets the fixed time delay between each call to the {@link #onMouseDrag}
	 * event. Setting this to an interval means the {@link #onMouseDrag} event
	 * is called repeatedly after the initial {@link #onMouseDown} until the
	 * user releases the mouse.
	 * 
	 * Sample code: <code>
	 * // Fire the onMouseDrag event once a second,
	 * // while the mouse button is down
	 * tool.eventInterval = 1000;
	 * </code>
	 * 
	 * @return the interval time in milliseconds
	 */
	public native int getEventInterval();
	
	public native void setEventInterval(int interval);
	
	/**
	 * @deprecated use Tool#setEventInterval instead.
	 * 
	 * @jshide
	 */
	public void setIdleEventInterval(int interval) {
		setEventInterval(interval);
	}

	/**
	 * The minimum distance the mouse has to drag before firing the onMouseDrag
	 * event, since the last onMouseDrag event.
	 * 
	 * Sample code:
	 * <code>
	 * // Fire the onMouseDrag event after the user has dragged
	 * // more then 5 points from the last onMouseDrag event:
	 * tool.distanceThreshold = 5;
	 * </code>
	 * 
	 * @param threshold
	 */
	public float getDistanceThreshold() {
		return distanceThreshold;
	}

	public void setDistanceThreshold(float threshold) {
		distanceThreshold = threshold;
	}

	private Callable onMouseDown;

	/**
	 * The function to be called when the mouse button is pushed down. The
	 * function receives a {@link MouseEvent} object which contains information
	 * about the mouse event.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDown(event) {
	 * 	// the position of the mouse in document coordinates:
	 * 	print(event.point);
	 * }
	 * </code>
	 * 
	 * {@grouptitle Mouse Event Handlers}
	 */
	public Callable getOnMouseDown() {
		return onMouseDown;
	}

	public void setOnMouseDown(Callable onMouseDown) {
		this.onMouseDown = onMouseDown;
	}
	
	protected void onMouseDown(MouseEvent event) throws Exception {
		if (onMouseDown != null)
			ScriptographerEngine.invoke(onMouseDown, this, event);
	}

	private Callable onMouseDrag;

	/**
	 * The function to be called when the mouse position changes while the mouse
	 * is being dragged. The function receives a {@link MouseEvent} object which
	 * contains information about the mouse event.
	 * 
	 * This function can also be called periodically while the mouse doesn't
	 * move by setting the {@link getEventInterval()}
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDrag(event) {
	 * 	// the position of the mouse in document coordinates
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public Callable getOnMouseDrag() {
		return onMouseDrag;
	}

	public void setOnMouseDrag(Callable onMouseDrag) {
		this.onMouseDrag = onMouseDrag;
	}
	
	protected void onMouseDrag(MouseEvent event) throws Exception {
		if (onMouseDrag != null)
			ScriptographerEngine.invoke(onMouseDrag, this, event);
	}

	private Callable onMouseMove;

	/**
	 * The function to be called when the tool is selected and the mouse moves
	 * within the document. The function receives a {@link MouseEvent} object
	 * which contains information about the mouse event.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseMove(event) {
	 * 	// the position of the mouse in document coordinates
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public Callable getOnMouseMove() {
		return onMouseMove;
	}

	public void setOnMouseMove(Callable onMouseMove) {
		this.onMouseMove = onMouseMove;
	}
	
	protected void onMouseMove(MouseEvent event) throws Exception {
		// Make sure the first move event initializes both delta and count.
		if (onMouseMove != null)
			ScriptographerEngine.invoke(onMouseMove, this, event);
	}

	private Callable onMouseUp;

	/**
	 * The function to be called when the mouse button is released. The function
	 * receives a {@link MouseEvent} object which contains information about the
	 * mouse event.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseUp(event) {
	 * 	// the position of the mouse in document coordinates
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public Callable getOnMouseUp() {
		return onMouseUp;
	}

	public void setOnMouseUp(Callable onMouseUp) {
		this.onMouseUp = onMouseUp;
	}
		
	protected void onMouseUp(MouseEvent event) throws Exception {
		if (onMouseUp != null)
			ScriptographerEngine.invoke(onMouseUp, this, event);
	}
	
	/**
	 * @jshide
	 */
	public native String getTitle();

	/**
	 * @jshide
	 */
	public native void setTitle(String title);

	/**
	 * The tooltip as seen when you hold the cursor over the tool button.
	 * {@grouptitle Tool Button Styling}
	 */
	public native String getTooltip();

	public native void setTooltip(String text);

	private native int nativeGetOptions();

	private native void nativeSetOptions(int options);

	/**
	 * @jshide
	 */
	public EnumSet<ToolOption> getOptions() {
		return IntegerEnumUtils.getSet(ToolOption.class, nativeGetOptions());
	}

	/**
	 * @jshide
	 */
	public void setOptions(EnumSet<ToolOption> options) {
		nativeSetOptions(IntegerEnumUtils.getFlags(options));
	}

	public Image getImage() {
		return image;
	}

	private native void nativeSetImage(int iconHandle);

	public void setImage(Image image) {
		nativeSetImage(image != null ? image.createIconHandle() : 0);
		this.image = image;
	}

	public Image getRolloverImage() {
		return rolloverImage;
	}

	private native void nativeSetRolloverImage(int iconHandle);

	public void setRolloverImage(Image image) {
		nativeSetRolloverImage(image != null ? image.createIconHandle() : 0);
		this.rolloverImage = image;
	}

	/*
	 * TODO: onOptions should be called onEditOptions, or both onOptions,
	 * but at least the same.
	 */
	private Callable onOptions;

	/**
	 * The function to be called when the tool button is double clicked. This is
	 * often used to present the users with a dialog containing options that
	 * they can set.
	 * 
	 * {@grouptitle Tool Button Event Handlers}
	 */
	public Callable getOnOptions() {
		return onOptions;
	}

	public void setOnOptions(Callable onOptions) {
		this.onOptions = onOptions;
	}

	protected void onOptions() throws Exception {
		if (onOptions != null)
			ScriptographerEngine.invoke(onOptions, this);
	}

	private Callable onSelect;

	/**
	 * The function to be called when the tool button is selected.
	 */
	public Callable getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(Callable onSelect) {
		this.onSelect = onSelect;
	}

	protected void onSelect() throws Exception {
		if (onSelect != null)
			ScriptographerEngine.invoke(onSelect, this);
	}
	
	private Callable onDeselect;

	/**
	 * The function to be called when the tool button is deselected.
	 */
	public Callable getOnDeselect() {
		return onDeselect;
	}

	public void setOnDeselect(Callable onDeselect) {
		this.onDeselect = onDeselect;
	}
	
	protected void onDeselect() throws Exception {
		if (onDeselect != null)
			ScriptographerEngine.invoke(onDeselect, this);
	}

	private Callable onReselect;

	/**
	 * The function to be called when the tool button has been reselected.
	 */
	public Callable getOnReselect() {
		return onReselect;
	}

	public void setOnReselect(Callable onReselect) {
		this.onReselect = onReselect;
	}

	protected void onReselect() throws Exception {
		if (onReselect != null)
			ScriptographerEngine.invoke(onReselect, this);
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
