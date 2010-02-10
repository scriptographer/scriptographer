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
 * File created on Feb 9, 2010.
 *
 * $Id$
 */

package com.scriptographer.sg;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.script.EnumUtils;

/**
 * @author lehni
 *
 */
public class KeyEvent extends Event {

	private int keyCode;
	private char character;
	private KeyEventType type;

	/**
	 * @jshide
	 */
	public KeyEvent(int type, int keyCode, char character, int modifiers) {
		super(modifiers);
		this.type = type == ScriptographerEngine.EVENT_KEY_DOWN
			? KeyEventType.KEY_DOWN : KeyEventType.KEY_UP;
		this.keyCode = keyCode;
		this.character = character;
	}

	public KeyEventType getType() {
		return type;
	}

	public int getKeyCode() {
		return keyCode;
	}

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
