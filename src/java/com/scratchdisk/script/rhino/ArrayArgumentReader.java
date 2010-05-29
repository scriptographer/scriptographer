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
 * File created on Feb 11, 2008.
 */

package com.scratchdisk.script.rhino;

import org.mozilla.javascript.NativeArray;

import com.scratchdisk.script.Converter;

/**
 * @author lehni
 *
 */
public class ArrayArgumentReader extends com.scratchdisk.script.ArrayArgumentReader {

	protected NativeArray array;

	public ArrayArgumentReader(Converter converter, NativeArray array) {
		super(converter);
		this.array = array;
	}

	protected Object readNext(String name) {
		return index < array.getLength() ? array.get(index++, array) : null;
	}

	public int size() {
		return (int) array.getLength();
	}
}
