/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 02.01.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

import java.awt.Point;

/**
 * @author lehni
 */
public class Tracker extends NativeObject {
	// ADMMouseState
	public final static int 
		MOUSE_NORMAL = 0,
		MOUSE_CAPTURED = 1,
		MOUSE_UNCAPTURED = 2;

	// ADMAction:
	// -----------------------------------------------------------------------------
	// Tracker event codes
	
	// ADM General									Macintosh Specific							Windows Specific	
	// -----------------------------------------	---------------------------------------	---------------------------------------------

	public final static int 
		ACTION_MOUSE_MOVE			= 1,			// It is better to use the specific move down and up cases
		ACTION_MOUSE_MOVED_DOWN		= 1,
		
		ACTION_BUTTON_DOWN			= 2,														ACTION_WIN_LEFT_BUTTON_DOWN				= 2,
																								ACTION_WIN_MIDDLE_BUTTON_DOWN			= 3,
																								ACTION_WIN_RIGHT_BUTTON_DOWN			= 4,
		ACTION_SHIFT_KEY_DOWN		= 5,
		ACTION_MENU_KEY_DOWN		= 6,			ACTION_MAC_COMMAND_KEY_DOWN	= 6,			ACTION_WIN_CONTROL_KEY_DOWN				= 6,
		ACTION_MOD_KEY_DOWN			= 7,			ACTION_MAC_OPTION_KEY_DOWN	= 7,			ACTION_WIN_ALT_KEY_DOWN					= 7,
													ACTION_MAC_CONTROL_KEY_DOWN	= 8,	
		ACTION_SPACE_KEY_DOWN		= 9,	
		ACTION_TAB_KEY_DOWN			= 10,
		ACTION_ENTER				= 11,

		ACTION_MOUSE_MOVED_UP		= -1,
		ACTION_BUTTON_UP			= -2,														ACTION_WIN_LEFT_BUTTON_UP				= -2,
																								ACTION_WIN_MIDDLE_BUTTON_UP				= -3,
																								ACTION_WIN_RIGHT_BUTTON_UP				= -4,
		ACTION_SHIFT_KEY_UP			= -5,
		ACTION_MENU_KEY_UP			= -6,			ACTION_MAC_COMMAND_KEY_UP	= -6,			ACTION_WIN_CONTROL_KEY_UP				= -6,
		ACTION_MOD_KEY_UP			= -7,			ACTION_MAC_OPTION_KEY_UP	= -7,			ACTION_WIN_ALT_KEY_UP					= -7,
													ACTION_MAC_CONTROL_KEY_U	= -8,	
		ACTION_SPACE_KEY_UP			= -9,	
		ACTION_TAB_KEY_UP			= -10,
		ACTION_LEAVE				= -11,
		ACTION_UNCAPTURED_BUTTON_UP	= -12,														ACTION_WIN_UNCAPTURED_LEFT_BUTTON_UP			= -12,
																								ACTION_WIN_UNCAPTURED_MIDDLE_BUTTON_UP	= -13,
																								ACTION_WIN_UNCAPTURED_RIGHT_BUTTON_UP		= -14,
		ACTION_KEY_STROKE			= -15,

		ACTION_DUMMY				= 0x7FFFFFFF;

	// ADMActionMask:
	// -----------------------------------------------------------------------------
	// Tracker event masks
	
	// ADM General									Macintosh Specific							Windows Specific	
	// -----------------------------------------  	-----------------------------------------	-----------------------------------------
	
	public final static int 
		MASK_NULL					= 0x00000000,
	
		MASK_UNCAPTURED_ACTION		= 0x00000001,
	
		MASK_MOUSE_MOVED_DOWN		= 0x00000002,
		
		MASK_BUTTON_DOWN			= 0x00000004,												MASK_WIN_LEFT_BUTTON_DOWN	= 0x00000004,
																								MASK_WIN_MIDDLE_BUTTON_DOWN	= 0x00000008,
																								MASK_WIN_RIGHT_BUTTON_DOWN	= 0x00000010,
		MASK_SHIFT_KEY_DOWN			= 0x00000020,
		MASK_MENU_KEY_DOWN			= 0x00000040,	MASK_MAC_COMMAND_KEY_DOWN	= 0x00000040,	MASK_WIN_CONTROL_KEY_DOWN	= 0x00000040,
		MASK_MOD_KEY_DOWN			= 0x00000080,	MASK_MAC_OPTION_KEY_DOWN	= 0x00000080,	MASK_WIN_ALT_KEY_DOWN		= 0x00000080,
													MASK_MAC_CONTROL_KEY_DOWN	= 0x00000100,	
		MASK_SPACE_KEY_DOWN			= 0x00000200,	
		MASK_TAB_KEY_DOWN			= 0x00000400,
		MASK_ENTER					= 0x00000800,
	
		MASK_MOUSE_MOVED_UP			= 0x00020000,
		MASK_BUTTON_UP				= 0x00040000,												MASK_WIN_LEFT_BUTTON_UP		= 0x00040000,
																								MASK_WIN_MIDDLE_BUTTON_UP	= 0x00080000,
																								MASK_WIN_RIGHT_BUTTON_UP	= 0x00100000,
		MASK_SHIFT_KEY_UP			= 0x00200000,
		MASK_MENU_KEY_UP			= 0x00400000,	MASK_MAC_COMMAND_KEY_UP		= 0x00400000,	MASK_WIN_CONTROL_KEY_UP		= 0x00400000,
		MASK_MOD_KEY_UP				= 0x00800000,	MASK_MAC_OPTION_KEY_UP		= 0x00800000,	MASK_WIN_ALT_KEY_UP			= 0x00800000,
													MASK_MAC_CONTROL_KEY_UP		= 0x01000000,	
		MASK_SPACE_KEY_UP			= 0x02000000,	
		MASK_TAB_KEY_UP				= 0x04000000,
		MASK_LEAVE					= 0x08000000,
		MASK_UNCAPTURED_BUTTON_UP	= 0x10000000,	/* Applies to all UncapturedButtonUpActions on Windows */
		
		MASK_KEY_STROKE				= 0x80000000,
		
		MASK_ALL_ACTIONS			= 0xFFFFFFFF;

	//	ADMModifiers
	//	 -----------------------------------------------------------------------------
	//	 Tracker modifier key masks
	
	//	 ADM General										Macintosh Specific								Windows Specific	
	//	 -----------------------------------------		--------------------------------------------	------------------------------------------------

	public final static int 
		MODIFIER_NULL				= 0x00000000,
		
		MODIFIER_CLICK				= 0x00000004,													MODIFIER_WIN_LEFT_BUTTON_DOWN		= 0x00000004,
																										MODIFIER_WIN_MIDDLE_BUTTON_DOWN		= 0x00000008,
																										MODIFIER_WIN_RIGHT_BUTTON_DOWN		= 0x00000010,
		MODIFIER_SHIFT_KEY_DOWN		= 0x00000020,
		MODIFIER_MENU_KEY_DOWN		= 0x00000040,	MODIFIER_MAC_COMMAND_KEY_DOWN	= 0x00000040,	MODIFIER_WIN_CONTROL_KEY_DOWN		= 0x00000040,
		MODIFIER_MOD_KEY_DOWN		= 0x00000080,	MODIFIER_MAC_OPTION_KEY_DOWN	= 0x00000080,	MODIFIER_WIN_ALT_KEY_DOWN			= 0x00000080,
													MODIFIER_MAC_CONTROL_KEY_DOWN	= 0x00000100,	
		MODIFIER_SPACE_KEY_DOWN		= 0x00000200,	
		MODIFIER_TAB_KEY_DOWN		= 0x00000400,
		MODIFIER_DOUBLE_CLICK		= 0x00000800,

		MODIFIER_CAPS_LOCK_ON		= 0x00001000,
		
		MODIFIER_TRIPLE_CLICK		= 0x00002000,
	    
	    MODIFIER_CONTEXT_MENU_CLICK	= 0x00004000,
	    
		MODIFIER_DUMMY				= 0xFFFFFFFF;

	// -----------------------------------------------------------------------------
	// Virtual keys
	
	public final static char 
		KEY_UNKNOWN				= 0x0000,
		KEY_CANCEL				= 0x0001,
		KEY_ENTER				= 0x0003,
		KEY_HOME				= 0x0004,
		KEY_END					= 0x0005,
		KEY_PAGE_UP				= 0x0006,
		KEY_PAGE_DOWN			= 0x0007,
		KEY_BACKSPACE			= 0x0008,
		KEY_TAB					= 0x0009,
		KEY_INSERT				= 0x000A,
		KEY_RETURN				= 0x000D,
		KEY_F1					= 0x000E,
		KEY_F2					= 0x000F,
		KEY_F3					= 0x0010,
		KEY_F4					= 0x0011,
		KEY_F5					= 0x0012,
		KEY_F6					= 0x0013,
		KEY_F7					= 0x0014,
		KEY_F8					= 0x0015,
		KEY_F9					= 0x0016,
		KEY_F10					= 0x0017,
		KEY_F11					= 0x0018,
		KEY_F12					= 0x0019,
		KEY_CLEAR				= 0x001A,
		KEY_ESCAPE				= 0x001B,
		KEY_LEFT				= 0x001C,
		KEY_RIGHT				= 0x001D,
		KEY_UP					= 0x001E,
		KEY_DOWN				= 0x001F,
		KEY_SPACE				= 0x0020,
		
		// Virtual keys from 0x0020 through the slash key (/) are their ASCII equivalents
		KEY_APOSTROPHE			= 0x0027,	// '
		KEY_COMMA				= 0x002C,	// ,
		KEY_MINUS				= 0x002D,	// -
		KEY_PERIOD				= 0x002E,	// .
		KEY_SLASH				= 0x002F,	// /
	
		// kADM0Key - kADM9Key are the same as ASCII '0' thru '9' (0x30 - 0x39)
		
		KEY_SEMICOLON			= 0x003B,	// ;
		KEY_EQUAL				= 0x003D,	// =
	
		// KEY_A - KEY_Z are the same as ASCII 'A' thru 'Z' (0x41 - 0x5A)
	
		KEY_LEFT_SQR_BRACKET	= 0x005B,	// [
		KEY_RIGHT_SQR_BRACKET	= 0x005D,	// ]
		KEY_BACK_SLASH			= 0x005C,	// "\"
	
		KEY_DELETE				= 0x007F,
	
		// key pad keys
		KEY_KP_0				= 0x00E0,
		KEY_KP_1				= 0x00E1,
		KEY_KP_2				= 0x00E2,
		KEY_KP_3				= 0x00E3,
		KEY_KP_4				= 0x00E4,
		KEY_KP_5				= 0x00E5,
		KEY_KP_6				= 0x00E6,
		KEY_KP_7				= 0x00E7,
		KEY_KP_8				= 0x00E8,
		KEY_KP_9 				= 0x00E9,
		KEY_KP__EQUAL 			= 0x00EA,
		KEY_KP__MULTIPLY		= 0x00EB,
		KEY_KP__MINUS 			= 0x00EC,
		KEY_KP__PLUS 			= 0x00ED,
		KEY_KP__DIVIDE		 	= 0x00EE,
		KEY_KP__DECIMAL			= 0x00EF,
		
		// kADMDoubleByteChar indicates that we have a double-byte character.
		// This occurs only if the kADMTrackerGetsDoubleByteInput host option is set.
		KEY_DOUBLE_BYTE_CHAR	= 0x8000,
	
		KEY_DUMMY				= 0xFFFF;
	
	
	private int action;
	private int modifiers;
	private Point point = new Point();
	private int mouseState;
	private char virtualKey;
	private char character;
	private long time;
	
	/**
	 * onTrack is called from the native code. for performance reasons, each
	 * listener has its own tracker object, which is then filled and passed as
	 * the tracker to the onTrack function (of course this could be left awawy
	 * but it's more clear for people who only work with onTrack). The advantage
	 * of this is only one call from the native code per notification, no
	 * creation of new Tracker objects on each notification, and nevertheless a
	 * clean interface on the TrackerListener side. The disadvantage maybe no
	 * thread safety, but that's not an issue with ADM right now.
	 * 
	 * @throws Exception
	 */
	protected boolean onTrack(NotificationHandler handler, int handle,
			int action, int modifiers, int px, int py, int mouseState,
			char virtualKey, char character, long time) throws Exception {
		this.handle = handle;
		this.action = action;
		this.modifiers = modifiers;
		this.point.setLocation(px, py);
		this.mouseState = mouseState;
		this.virtualKey = virtualKey;
		this.character = character;
		this.time = time;
		return handler.onTrack(this);
	}
	
	public Point getPoint() {
		return point;
	}
	
	public int getAction() {
		return action;
	}
	
	public int getModifiers() {
		return modifiers;
	}
	
	public static native int getCurrentModifiers();
	
	public boolean testAction(int action) {
		return this.action == action;
	}
	
	public boolean testModifier(int modifier) {
		return (this.modifiers & modifier) != 0;
	}
	
	public static boolean testCurrentModifier(int modifier) {
		return (getCurrentModifiers() & modifier) != 0;
	}
	
	public long getTime() {
		return time;
	}

	public native void abort();

	public native void releaseMouseCapture();
	
	public char getVirtualKey() {
		return virtualKey;
	}
	
	public char getCharacter() {
		return character;
	}

	public int getMouseState() {
		return mouseState;
	}
}
