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

/**
 * @author lehni
 *
 */
public class EventModifiers {
	// Bit flags based on Mac modifiers that are also used by AI / ADM
	// See AIEvent.h AIEventModifersValue
	protected final static int SHIFT = 1 << 9;
	protected final static int CONTROL = 1 << 12;
	protected final static int OPTION = 1 << 11;
	protected final static int META = 1 << 8;
	// Match both Mac and ADM Caps Lock
	protected final static int CAPS_LOCK = (1 << 10) | (1 << 3);

	private int modifiers;

	protected EventModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	public boolean getShift() {
		return (modifiers & SHIFT) != 0;
	}

	public boolean getControl() {
		return (modifiers & CONTROL) != 0;
	}

	public boolean getOption() {
		return (modifiers & OPTION) != 0;
	}

	public boolean getMeta() {
		return (modifiers & META) != 0;
	}

	public boolean getCapsLock() {
		return (modifiers & CAPS_LOCK) != 0;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("{ shift: ").append(getShift());
		buf.append(", control: ").append(getControl());
		buf.append(", option: ").append(getOption());
		buf.append(", meta: ").append(getMeta());
		buf.append(", capsLock: ").append(getCapsLock());
		buf.append(" }");
		return buf.toString();
	}
}
