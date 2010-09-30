/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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
 * File created on Mar 1, 2010.
 */

package com.scriptographer.ai;

import com.scratchdisk.script.Callable;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.sg.Script;

/**
 * @author lehni
 *
 * @jshide
 */
public class ToolHandler extends NativeObject {

	private Double minDistance;
	private Double maxDistance;
	private boolean firstMove;
	protected Point point;
	protected Point downPoint;
	protected Point lastPoint;
	protected int count;
	protected int downCount;
	protected double pressure;

	protected ToolHandler(int handle) {
		super(handle);
	}

	public ToolHandler() {
	}

	/**
	 * Initializes the tool's settings, so a new tool can be assigned to it
	 * 
	 * @jshide
	 */
	public void initialize() {
		minDistance = null;
		maxDistance = null;
		firstMove = true;
		downPoint = null;
		lastPoint = null;
		count = 0;
		downCount = 0;
		onEditOptions = null;
		onSelect = null;
		onDeselect = null;
		onReselect = null;
		onMouseDown = null;
		onMouseUp = null;
		onMouseDrag = null;
		onMouseMove = null;
		script = null;
	}

	/**
	 * The minimum distance the mouse has to drag before firing the onMouseDrag
	 * event, since the last onMouseDrag event.
	 * 
	 * Sample code:
	 * <code>
	 * // Fire the onMouseDrag event after the user has dragged
	 * // more then 5 points from the last onMouseDrag event:
	 * tool.minDistance = 5;
	 * </code>
	 * 
	 * @param threshold
	 */
	public Double getMinDistance() {
		return minDistance;
	}

	public void setMinDistance(Double minDistance) {
		this.minDistance = minDistance;
		if (minDistance != null && maxDistance != null
				&& minDistance > maxDistance)
			maxDistance = minDistance;
	}

	public Double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
		if (minDistance != null && maxDistance != null
				&& maxDistance < minDistance)
			minDistance = maxDistance;
	}

	public Double getFixedDistance() {
		if (minDistance != null && minDistance.equals(maxDistance))
			return minDistance;
		return null;
	}

	public void setFixedDistance(Double distance) {
		minDistance = distance;
		maxDistance = distance;
	}

	/**
	 * @deprecated
	 */
	public double getDistanceThreshold() {
		return minDistance != null ? minDistance : 0;
	}

	/**
	 * @deprecated
	 */
	public void setDistanceThreshold(double threshold) {
		minDistance = threshold;
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
	
	protected void onMouseDown(ToolEvent event) {
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
	 * move by setting the {@link Tool#getEventInterval()}
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
	
	protected void onMouseDrag(ToolEvent event) {
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
	
	protected void onMouseMove(ToolEvent event) {
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
		
	protected void onMouseUp(ToolEvent event) {
		if (onMouseUp != null)
			ScriptographerEngine.invoke(onMouseUp, this, event);
	}

	/*
	 * TODO: onOptions should be called onEditOptions, or both onOptions,
	 * but at least the same.
	 */
	private Callable onEditOptions;

	/**
	 * {@grouptitle Tool Button Event Handlers}
	 * 
	 * The function to be called when the tool button is double clicked. This is
	 * often used to present the users with a dialog containing options that
	 * they can set.
	 */
	public Callable getOnEditOptions() {
		return onEditOptions;
	}

	public void setOnEditOptions(Callable onOptions) {
		this.onEditOptions = onOptions;
	}

	protected void onEditOptions() {
		if (onEditOptions != null)
			ScriptographerEngine.invoke(onEditOptions, this);
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

	protected void onSelect() {
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
	
	protected void onDeselect() {
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

	protected void onReselect() {
		if (onReselect != null)
			ScriptographerEngine.invoke(onReselect, this);
	}

	private boolean updateEvent(ToolEventType type, Point pt, int pressure,
			Double minDistance, Double maxDistance, boolean start,
			boolean needsChange, boolean matchMaxDistance) {
		if (!start) {
			if (minDistance != null || maxDistance != null) {
				double minDist = minDistance != null ? minDistance : 0;
				Point vector = pt.subtract(point);
				double distance = vector.getLength();
				if (distance < minDist)
					return false;
				// Produce a new point on the way to pt if pt is further away
				// than maxDistance
				double maxDist = maxDistance != null ? maxDistance : 0;
				if (maxDist != 0) {
					if (distance > maxDist)
						pt = point.add(vector.normalize(maxDist));
					else if (matchMaxDistance)
						return false;
				}
			}
			if (needsChange && pt.equals(point))
				return false;
		}
		lastPoint = point;
		point = pt;
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

	public void onHandleEvent(ToolEventType type, Point pt, int pressure,
			int modifiers) {
		try {
			switch (type) {
			case MOUSE_DOWN:
				updateEvent(type, pt, pressure, null, null, true, false, false);
				onMouseDown(new ToolEvent(this, type, modifiers));
				break;
			case MOUSE_DRAG:
				// In order for idleInterval drag events to work, we need to
				// not check the first call for a change of position. 
				// Subsequent calls required by min/maxDistance functionality
				// will require it, otherwise this might loop endlessly.
				boolean needsChange = false;
				// If the mouse is moving faster than maxDistance, do not
				// produce events for what is left after the first event is
				// generated in case it is shorter than maxDistance, as this
				// would produce weird results. matchMaxDistance controls this.
				boolean matchMaxDistance = false;
				while (updateEvent(type, pt, pressure, minDistance,
						maxDistance, false, needsChange, matchMaxDistance)) {
					try {
						onMouseDrag(new ToolEvent(this, type, modifiers));
					} catch (Exception e) {
						ScriptographerEngine.reportError(e);
					}
					needsChange = true;
					matchMaxDistance = true;
				}
				break;
			case MOUSE_UP:
				// If the last mouse drag happened in a different place, call
				// mouse drag first, then mouse up.
				if ((point.x != pt.x || point.y != pt.y) && updateEvent(
						ToolEventType.MOUSE_DRAG, pt, pressure,
						minDistance, maxDistance, false, false, false)) {
					try {
						onMouseDrag(new ToolEvent(this, type, modifiers));
					} catch (Exception e) {
						ScriptographerEngine.reportError(e);
					}
				}
				updateEvent(type, pt, pressure, null, maxDistance, false,
						false, false);
				try {
					onMouseUp(new ToolEvent(this, type, modifiers));
				} catch (Exception e) {
					ScriptographerEngine.reportError(e);
				}
				// Start with new values for TRACK_CURSOR
				updateEvent(type, pt, pressure, null, null, true, false, false);
				firstMove = true;
				break;
			case MOUSE_MOVE:
				while (updateEvent(type, pt, pressure, minDistance,
						maxDistance, firstMove, true, false)) {
					try {
						onMouseMove(new ToolEvent(this, type, modifiers));
					} catch (Exception e) {
						ScriptographerEngine.reportError(e);
					}
					firstMove = false;
				}
				break;
			case EDIT_OPTIONS:
				onEditOptions();
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
	}

	public void onHandleEvent(ToolEventType type, Point pt) {
		onHandleEvent(type, pt, 128, 0);
	}

	protected Script script;

	public Script getScript() {
		return script;
	}

	public void setScript(Script script) {
		this.script = script;
		// Let the script know it's there for a tool.
		script.setToolHandler(this);
	}
}
