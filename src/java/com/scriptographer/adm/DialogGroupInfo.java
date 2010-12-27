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
 * File created on 11.03.2005.
 */

package com.scriptographer.adm;

/**
 * @author lehni
 * 
 * @jshide
 */
public class DialogGroupInfo {
	// dialog position code masks
	public static final int
		POSITION_DOCK				= 0x000000ff,
		POSITION_TAB				= 0x0000ff00,
		POSITION_FRONTTAB			= 0x00010000,
		POSITION_ZOOM				= 0x00020000,
		POSITION_DOCKVISIBLE		= 0x00040000,
		POSITION_DEFAULT			= POSITION_FRONTTAB | POSITION_ZOOM;

	// dialog position code masks
	public static final int
		/** Dialog group position mask. Provides access to the dock position byte. */
		MASK_DOCK 					= 0x000000ff,

		/** Dialog group position mask. Provides access to the tab position byte. */
		MASK_TAB					= 0x0000ff00,

		/** Dialog group position mask. Provides access to the front-tab position bit flag. */
		MASK_FRONTAB				= 0x00010000,

		/** Dialog group position mask. Provides access to the zoom position bit flag. */
		MASK_ZOOM					= 0x00020000,

		/** Dialog group position mask. Provides access to the dock-visible position bit flag. */
		MASK_DOCK_VISIBLE			= 0x00040000,
	
		/** Dialog group position mask. Provides access to the frame-docked window pane index byte. */
		MASK_FRAMEDOCK_INDEX		= 0x00780000,

		/** Dialog group position mask. Provides access to the frame-docked window pane location byte. */
		MASK_FRAMEDOCK_LOCATION		= 0x01800000,

		/** Dialog group position mask. Provides access to the frame-docked window pane state. */
		MASK_FRAMEDOCK_PANESTATE	= 0x06000000,

		/** Dialog group position mask. Provides access to the frame-docked open drawer. */
		MASK_DRAWER					= 0x08000000,

		/** Tab/iconic mode icon hidden bit mask. */
		MASK_TAB_HIDDEN				= 0x10000000,

		/** Dock closed bit mask. */
		MASK_DOCK_CLOSED			= 0x40000000;

	protected String group;
	protected int positionCode;
	
	public DialogGroupInfo(String group, int positionCode) {
		this.group = group;
		this.positionCode = positionCode;
	}
	
	public String getGroup() {
		return group;
	}
	
	public int getPositionCode() {
		return positionCode;
	}
	
	// TODO: add isDockVisible, isFrontTab, ...
}
