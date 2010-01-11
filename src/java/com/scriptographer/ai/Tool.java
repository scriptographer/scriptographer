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

import com.scratchdisk.script.Callable;
import com.scratchdisk.util.IntMap;
import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.ScriptographerException;
import com.scriptographer.ui.Image;

/**
 * The Tool object refers to the Scriptographer tool in the Illustrator tool
 * palette and can be accessed through the global {@code tool} variable. All its
 * properties are also available in the global scope.
 * 
 * The global {@code tool} variable only exists in scripts that contain mouse
 * handler functions ({@link #getOnMouseDown()}, {@link #getOnMouseDrag()},
 * {@link #getOnMouseUp()}), which are automatically associated with the tool
 * button on execution.
 * 
 * Sample code:
 * <code>
 * var path;
 * 
 * // Only execute onMouseDrag when the mouse
 * // has moved at least 10 points:
 * tool.distanceThreshold = 10;
 * 
 * function onMouseDown(event) {
 * 	// Create a new path every time the mouse is clicked
 * 	path = new Path();
 * }
 * 
 * function onMouseDrag(event) {
 * 	// Add a point to the path every time the mouse is dragged
 * 	path.lineTo(event.point);
 * }
 * </code>
 * 
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

	private boolean firstMove;
	protected Point point;
	protected Point downPoint;
	protected Point lastPoint;
	protected int count;
	protected int downCount;
	protected double pressure;

	private Image image = null;
	private Image rolloverImage = null;

	private String name;

	/**
	 * @jshide
	 */
	public Tool(String name, Image image, EnumSet<ToolOption> options, Tool groupTool,
			Tool toolsetTool) {
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
			setImage(image);
		} else {
			// No previously existing effect found, create a new one:
			handle = nativeCreate(name,
					image != null ? image.createIconHandle() : 0,
					IntegerEnumUtils.getFlags(options),
					groupTool != null ? groupTool.handle : 0,
					toolsetTool != null ? toolsetTool.handle : 0);
			this.image = image;
		}

		if (handle == 0)
			throw new ScriptographerException("Unable to create Tool.");

		initialize();

		tools.put(handle, this);
	}

	public Tool(String name, Image image, EnumSet<ToolOption> options, Tool groupTool) {
		this(name, image, options, groupTool, null);
	}

	public Tool(String name, Image image, EnumSet<ToolOption> options) {
		this(name, image, options, null, null);
	}

	public Tool(String name, Image image) {
		this(name, image, null, null, null);
	}

	public Tool(String name) {
		this(name, null, null, null, null);
	}

	private native int nativeCreate(String name, int iconHandle, int options,
			int groupHandle, int toolsetHandle);

	protected Tool(int handle, String name) {
		super(handle);
		// See resourceIds.h:
		this.name = name;
	}

	/**
	 * Initializes the tool's settings, so a new tool can be assigned to it
	 * 
	 * @jshide
	 */
	public void initialize() {
		onOptions = null;
		onSelect = null;
		onDeselect = null;
		onReselect = null;
		onMouseDown = null;
		onMouseUp = null;
		onMouseDrag = null;
		onMouseMove = null;
		firstMove = true;
		downPoint = null;
		lastPoint = null;
		count = 0;
		downCount = 0;
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
	 * {@grouptitle Mouse Event Handlers}
	 * 
	 * The function to be called when the mouse button is pushed down. The
	 * function receives a {@link ToolEvent} object which contains information
	 * about the mouse event.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDown(event) {
	 * 	// the position of the mouse in document coordinates:
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public Callable getOnMouseDown() {
		return onMouseDown;
	}

	public void setOnMouseDown(Callable onMouseDown) {
		this.onMouseDown = onMouseDown;
	}
	
	protected void onMouseDown(ToolEvent event) throws Exception {
		if (onMouseDown != null)
			ScriptographerEngine.invoke(onMouseDown, this, event);
	}

	private Callable onMouseDrag;

	/**
	 * The function to be called when the mouse position changes while the mouse
	 * is being dragged. The function receives a {@link ToolEvent} object which
	 * contains information about the mouse event.
	 * 
	 * This function can also be called periodically while the mouse doesn't
	 * move by setting the {@link #getEventInterval()}
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
	
	protected void onMouseDrag(ToolEvent event) throws Exception {
		if (onMouseDrag != null)
			ScriptographerEngine.invoke(onMouseDrag, this, event);
	}

	private Callable onMouseMove;

	/**
	 * The function to be called when the tool is selected and the mouse moves
	 * within the document. The function receives a {@link ToolEvent} object
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
	
	protected void onMouseMove(ToolEvent event) throws Exception {
		// Make sure the first move event initializes both delta and count.
		if (onMouseMove != null)
			ScriptographerEngine.invoke(onMouseMove, this, event);
	}

	private Callable onMouseUp;

	/**
	 * The function to be called when the mouse button is released. The function
	 * receives a {@link ToolEvent} object which contains information about the
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
		
	protected void onMouseUp(ToolEvent event) throws Exception {
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
	 * {@grouptitle Tool Button Styling}
	 * 
	 * The tooltip as seen when you hold the cursor over the tool button.
	 */
	public native String getTooltip();

	public native void setTooltip(String text);

	private native int nativeGetOptions();

	private native void nativeSetOptions(int options);

	/**
	 * @jshide
	 */
	public native boolean getSelected();

	/**
	 * @jshide
	 */
	public native void setSelected(boolean selected);

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
	 * {@grouptitle Tool Button Event Handlers}
	 * 
	 * The function to be called when the tool button is double clicked. This is
	 * often used to present the users with a dialog containing options that
	 * they can set.
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

	private boolean updateEvent(ToolEventType type, double x, double y, int pressure,
			float threshold, boolean start) {
		if (start || threshold == 0 || point.getDistance(x, y) >= threshold) {
			lastPoint = point;
			point = new Point(x, y);
			switch (type) {
			case MOUSE_DOWN:
				lastPoint = downPoint;
				downPoint = point;
				downCount++;
				break;
			case MOUSE_UP:
				// Mouse up events return the down point for last point,
				// so delta is spanning over the whole drag.
				lastPoint = downPoint;
				break;
			}
			if (start) {
				count = 0;
			} else {
				count++;
			}
			this.pressure = pressure / 255.0;
			return true;
		}
		return false;
	}

	private int onHandleEvent(ToolEventType type, double x, double y, int pressure) {
		try {
			switch (type) {
			case MOUSE_DOWN:
				updateEvent(type, x, y, pressure, 0, true);
				onMouseDown(new ToolEvent(this, type));
				break;
			case MOUSE_DRAG:
				if (updateEvent(type, x, y, pressure, distanceThreshold, false))
					onMouseDrag(new ToolEvent(this, type));
				break;
			case MOUSE_UP:
				// If the last mouse drag happened in a different place, call
				// mouse drag first, then mouse up.
				if ((point.x != x || point.y != y)
						&& updateEvent(ToolEventType.MOUSE_DRAG, x, y, pressure,
								distanceThreshold, false)) {
					try {
						onMouseDrag(new ToolEvent(this, type));
					} catch (Exception e) {
						ScriptographerEngine.reportError(e);
					}
				}
				updateEvent(type, x, y, pressure, 0, false);
				try {
					onMouseUp(new ToolEvent(this, type));
				} catch (Exception e) {
					ScriptographerEngine.reportError(e);
				}
				// Start with new values for TRACK_CURSOR
				updateEvent(type, x, y, pressure, 0, true);
				firstMove = true;
				break;
			case MOUSE_MOVE:
				try {
					if (updateEvent(type, x, y, pressure, distanceThreshold, firstMove))
						onMouseMove(new ToolEvent(this, type));
				} finally {
					firstMove = false;
				}
				// Tell the native side to update the cursor
				return cursor;
			case EDIT_OPTIONS:
				onOptions();
				break;
			case SELECT:
				onSelect();
				break;
			case DESELECT:
				onDeselect();
				break;
			case RESELECT:
				onReselect();
				break;
			}
		} catch (Exception e) {
			ScriptographerEngine.reportError(e);
		}
		return 0;
	}

	/**
	 * To be called from the native environment. Returns the cursor
	 * id to be set, if any.
	 */
	@SuppressWarnings("unused")
	private static int onHandleEvent(int handle, String selector, float x,
			float y, int pressure) throws Exception {
		Tool tool = getTool(handle);
		ToolEventType type = ToolEventType.get(selector); 
		if (tool != null && type != null)
			return tool.onHandleEvent(type, x, y, pressure);
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
