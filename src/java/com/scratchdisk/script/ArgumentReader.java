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
 * File created on Feb 11, 2008.
 *
 * $Id$
 */

package com.scratchdisk.script;

import com.scratchdisk.util.ConversionUtils;

/**
 * @author lehni
 *
 */
public abstract class ArgumentReader {

	protected abstract Object readNext(String name);
	
	public boolean readBoolean(String name) {
		return ConversionUtils.toBoolean(readNext(name));
	}

	public double readDouble(String name) {
		return ConversionUtils.toDouble(readNext(name));
	}

	public float readFloat(String name) {
		return ConversionUtils.toFloat(readNext(name));
	}

	public int readInt(String name) {
		return ConversionUtils.toInt(readNext(name));
	}

	public String readString(String name) {
		return ConversionUtils.toString(readNext(name));
	}

	public Object readObject(String name) {
		return readNext(name);
	}
}
