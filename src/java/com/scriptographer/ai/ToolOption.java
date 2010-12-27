/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Jun 20, 2009.
 */

package com.scriptographer.ai;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 * AIToolOptions
 */
public enum ToolOption implements IntegerEnum {
	/*
	 * This option is always on, since we're changing cursors
	 */	
	// TRACK_CURSOR(1 << 0),
	/**
	 * Set to disable automatic scrolling. When off (the default), the Illustrator window
	 * scrolls when a tool reaches the edge. For tools that manipulate artwork,
	 * auto-scroll is useful. Set this to turn auto-scroll off for a tool that
	 * draws to the screen directly, like the built-in Brush tool.
	 */
	NO_AUTO_SCROLL(1 << 1),
	/**
	 * Set to buffer the drag selectors and messages and send all of them
	 * to the tool at once. Useful if a tool is calculation intensive.  The effect
	 * is no longer real-time, but has a smoother final output.
	 * When off (the default), the tool processes drag selectors and returns frequently,
	 * resulting in near real-time feedback. If there are intensive calculations
	 * during the drag selector, the tool could miss drag notifications, resulting in rougher
	 * tracking.
	 */
	BUFFERED_DRAGGING(1 << 2),
	/** 
	 * Set to maintain the edit context when this tool is selected. For art objects,
	 * keeps all current points and handles selected. For text, keeps the insertion
	 * point in the current location. Set this option for navigational tools like
	 * the Zoom and Scroll tools.
	 */
	MAINTAIN_EDIT_CONTEXT(1 << 3),
	/**
	 * Set to maintain the text edit context when the tool is selected,
	 * if #kToolMaintainEditContextOption is also set.
	 */
	TEXT_TOOL(1 << 4),
	/**
	 * Set to receive \c #kSelectorAIToolDecreaseDiameter and
	 * #kSelectorAIToolIncreaseDiameter. Use if the tool needs to change
	 * diameter when either '[' or ']' is pressed.
	 */
	CHANGE_DIAMETER(1 << 5);

	protected int value;

	private ToolOption(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}
}
