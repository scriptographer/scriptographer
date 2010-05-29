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
 * File created on Feb 9, 2010.
 */

package com.scriptographer.ui;

import com.scratchdisk.util.IntegerEnumUtils;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.script.EnumUtils;
import com.scriptographer.sg.Event;

/**
 * The KeyEvent object is received by the key event handlers
 * {@code onKeyDown(event)} and {@code onKeyUp(event)}. The KeyEvent
 * object is the only parameter passed to these functions and contains
 * information about the key event.
 * 
 * Sample code:
 * <code>
 * function onKeyDown(event) {
 * 	// The character of the key that was pressed
 * 	print(event.character);
 * }
 * </code>
 * 
 * @author lehni
 */
public class KeyEvent extends Event {

	private KeyCode keyCode;
	private char character;
	private KeyEventType type;

	/**
	 * @jshide
	 */
	public KeyEvent(int type, int keyCode, char character, int modifiers) {
		super(modifiers);
		this.type = type == ScriptographerEngine.EVENT_KEY_DOWN
			? KeyEventType.KEY_DOWN : KeyEventType.KEY_UP;
		this.keyCode = IntegerEnumUtils.get(KeyCode.class, keyCode);
		this.character = character;
	}

	/**
	 * Specifies the type of key event.
	 * 
	 * @return the type of key event.
	 */
	public KeyEventType getType() {
		return type;
	}

	/**
	 * Specifies the virtual key code of the key that caused this key event.
	 * 
	 * @return the virtual key code.
	 */
	public KeyCode getKeyCode() {
		return keyCode;
	}

	/**
	 * Specifies the {@code String} character of the key that caused this
	 * key event.
	 * 
	 * @return the key character.
	 */
	public char getCharacter() {
		return character;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ type: ").append(EnumUtils.getScriptName(type));
		buf.append(", keyCode: ").append(keyCode);
		buf.append(", character: ").append(character);
		buf.append(", modifiers: ").append(getModifiers());
		buf.append(" }");
		return buf.toString();
	}
}
