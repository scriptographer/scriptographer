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
 * File created on Feb 12, 2008.
 */

package com.scratchdisk.script;

/**
 * A StringArgumentReader can read from passed strings. It extends
 * ArrayArgumentReader, therefore also return true for isArray.
 * So make sure you check for isString first if that special case
 * needs handling.
 * 
 * @author lehni
 *
 */
public class StringArgumentReader extends ArrayArgumentReader {

	private String[] parts;

	public StringArgumentReader(Converter converter, String string) {
		super(converter);
		parts = string.split("\\s");
	}

	protected Object readNext(String name) {
		return index < parts.length ? parts[index++] : null;
	}

	public boolean isString() {
		return true;
	}
}
