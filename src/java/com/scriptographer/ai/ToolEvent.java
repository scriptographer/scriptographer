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

import com.scriptographer.script.EnumUtils;

/**
 * The ToolEvent object is received by the {@link Tool}'s mouse event handlers
 * {@link Tool#getOnMouseDown()}, {@link Tool#getOnMouseDrag()},
 * {@link Tool#getOnMouseMove()} and {@link Tool#getOnMouseUp()}. The ToolEvent
 * object is the only parameter passed to these functions and contains
 * information about the mouse event.
 * 
 * Sample code:
 * <code>
 * function onMouseUp(event) {
 * 	// the position of the mouse when it is released
 * 	print(event.point);
 * }
 * </code>
 * 
 * @author lehni
 */
public class ToolEvent {
	private Tool tool;
	private ToolEventType type;

	protected ToolEvent(Tool tool) {
		this.tool = tool;
	}

	protected void update(ToolEventType type) {
		this.type = type;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer(16);
		buf.append("{ type: ").append(EnumUtils.getScriptName(type)); 
		buf.append(", point: ").append(tool.point.toString());
		buf.append(", pressure: ").append(tool.pressure);
		buf.append(" }");
		return buf.toString();
	}

	/**
	 * The position of the mouse in document coordinates when the event was
	 * fired.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDrag(event) {
	 * 	// the position of the mouse when it is dragged
	 * 	print(event.point);
	 * }
	 * 
	 * function onMouseUp(event) {
	 * 	// the position of the mouse when it is released
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public Point getPoint() {
		return new Point(tool.point);
	}

	/**
	 * The position of the mouse in document coordinates when the previous
	 * event was fired.
	 */
	public Point getLastPoint() {
		return tool.lastPoint != null ? new Point(tool.lastPoint) : null;
	}

	/**
	 * The position of the mouse in document coordinates when the mouse button
	 * was last clicked.
	 */
	public Point getDownPoint() {
		return tool.downPoint != null ? new Point(tool.downPoint) : null;
	}

	/**
	 * The point in the middle between {@link #getLastPoint()} and
	 * {@link #getPoint()}. This is a useful position to use when creating
	 * artwork based on the moving direction of the mouse, as returned by
	 * {@link #getDelta()}.
	 */
	public Point getMiddlePoint() {
		if (tool.lastPoint != null)
			return tool.point.add(tool.lastPoint).divide(2);
		return null;
	}

	/**
	 * The difference between the current position and the last position of the
	 * mouse when the event was fired. In case of the mouse-up event, the
	 * difference to the mouse-down position is returned.
	 */
	public Point getDelta() {
		if (tool.lastPoint != null)
			return tool.point.subtract(tool.lastPoint);
		return null;
	}

	/**
	 * The amount of force being applied by a pressure-sensitive input device,
	 * such as a graphic tablet.
	 * 
	 * @return the pressure as a value between 0 and 1
	 */
	public double getPressure() {
		return tool.pressure;
	}

	/**
	 * The number of times the mouse event was fired.
	 * 
	 * Sample code:
	 * <code>
	 * function onMouseDrag(event) {
	 * 	// the amount of times the onMouseDrag event was fired
	 * 	// since the last onMouseDown event
	 * 	print(event.count);
	 * }
	 * 
	 * function onMouseUp(event) {
	 * 	// the amount of times the onMouseUp event was fired
	 * 	// since the tool was activated 
	 * 	print(event.point);
	 * }
	 * </code>
	 */
	public int getCount() {
		switch (type) {
		case MOUSE_DOWN:
		case MOUSE_UP:
			// Return downCount for both mouse down and up, since
			// the count is the same.
			return tool.downCount;
		default:
			return tool.count;
		}
	}

	public ToolEventType getType() {
		return type;
	}

	// TODO: Consider adding these, present since CS2
	/*
	 * For graphic tablets, tangential pressure on the finger wheel of the
	 * airbrush tool.
	 */
	// AIToolPressure stylusWheel;
	/*
	 * How the tool is angled, also called altitude or elevation.
	 */
	// AIToolAngle tilt;
	/*
	 * The direction of tilt, measured clockwise in degrees around the Z axis,
	 * also called azimuth,
	 */
	// AIToolAngle bearing;
	/*
	 * Rotation of the tool, measured clockwise in degrees around the tool's
	 * barrel.
	 */
	// AIToolAngle rotation;
}
