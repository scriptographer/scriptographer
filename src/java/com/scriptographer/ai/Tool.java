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
 * File created on 21.12.2004.
 *
 * $RCSfile: Tool.java,v $
 * $Author: lehni $
 * $Revision: 1.11 $
 * $Date: 2007/01/03 15:09:41 $
 */

package com.scriptographer.ai;

import org.mozilla.javascript.*;

import com.scriptographer.*;
import com.scriptographer.js.FunctionHelper;
import com.scriptographer.util.IntMap;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class Tool extends AIObject {
	private int index;

	private static IntMap tools = null;

	protected Tool(int handle, int index) {
		super(handle);
		this.index = index;
	}
	
	private Scriptable scope;
	private Function mouseUpFunc;
	private Function mouseDragFunc;
	private Function mouseMoveFunc;
	private Function mouseDownFunc;
	
	private Event event = new Event();
	private Object[] eventArgs = new Object[] { event };
	
	public void setScript(File file) {
		scope = ScriptographerEngine.executeFile(file, null);
		// execute in the tool's scope so setIdleInterval can be called
		scope.setParentScope((Scriptable) ScriptographerEngine.javaToJS(this));
		if (scope != null) {
			setIdleEventInterval(-1);
			mouseUpFunc = FunctionHelper.getFunction(scope, "onMouseUp");
			mouseDragFunc = FunctionHelper.getFunction(scope, "onMouseDrag");
			mouseMoveFunc = FunctionHelper.getFunction(scope, "onMouseMove");
			mouseDownFunc = FunctionHelper.getFunction(scope, "onMouseDown");
			FunctionHelper.callFunction(scope, "onInit");
		}
	}

	private static IntMap getTools() {
		if (tools == null)
			tools = nativeGetTools();
		return tools;
	}

	/**
	 * Returns all tools that have been created by this plugin.
	 * This is necessary because the java part of the plugin may be reloaded.
	 * The plugin needs to be capable of reestablish the connections between the wrappers
	 * and the real objects.
	 *
	 * @return
	 */
	private static native IntMap nativeGetTools();

	public native boolean hasPressure();
	
	// interval time in milliseconds
	public native int getIdleEventInterval();
	
	public native void setIdleEventInterval(int interval);

	protected void onEditOptions() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onOptions");
		}
	}

	protected void onSelect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onSelect");
		}
	}
	
	protected void onDeselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onDeselect");
		}
	}
	
	protected void onReselect() throws Exception {
		if (scope != null) {
			FunctionHelper.callFunction(scope, "onReselect");
		}
	}
	
	protected void onMouseDown(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDownFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDownFunc, eventArgs);
		}
	}
	
	protected void onMouseDrag(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDragFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseDragFunc, eventArgs);
		}
	}
	
	protected void onMouseMove(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseDragFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseMoveFunc, eventArgs);
		}
	}
	
	protected void onMouseUp(float x, float y, int pressure) throws Exception {
		if (scope != null && mouseUpFunc != null) {
			event.setValues(x, y, pressure);
			FunctionHelper.callFunction(scope, mouseUpFunc, eventArgs);
		}
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
	// hashmap for conversation to unique ids that can be compared with ==
	// instead of .equals
	private static HashMap events = new HashMap();

	static {
		for (int i = 0; i < eventTypes.length; i++)
			events.put(eventTypes[i], new Integer(i));
	}

	/**
	 * To be called from the native environment:
	 */
	private static void onHandleEvent(int handle, String selector, float x,
			float y, int pressure) throws Exception {
		Tool tool = getToolByHandle(handle);
		if (tool != null) {
			Integer event = (Integer) events.get(selector); 
			if (event != null) {
				switch(event.intValue()) {
					case EVENT_EDIT_OPTIONS:
						tool.onEditOptions();
						break;
					case EVENT_TRACK_CURSOR:
						tool.onMouseMove(x, y, pressure);
						break;
					case EVENT_MOUSE_DOWN:
						tool.onMouseDown(x, y, pressure);
						break;
					case EVENT_MOUSE_DRAG:
						tool.onMouseDrag(x, y, pressure);
						break;
					case EVENT_MOUSE_UP:
						tool.onMouseUp(x, y, pressure);
						break;
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
