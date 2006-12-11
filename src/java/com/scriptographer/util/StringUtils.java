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
 * File created on 08.12.2006.
 * 
 * $RCSfile: StringUtils.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/12/11 18:55:44 $
 */

package com.scriptographer.util;

public class StringUtils {
	private StringUtils() {
	}

	public static String replace(String str, String find, String replace) {
		int pos = str.indexOf(find);
		if (pos == -1)
			return str;

		int next = 0;
		StringBuffer buf = new StringBuffer(str.length() + replace.length());
		do {
			buf.append(str.substring(next, pos));
			buf.append(replace);
			next = pos + find.length();
		} while ((pos = str.indexOf(find, next)) != -1);

		if (next < str.length())
			buf.append(str.substring(next, str.length()));

		return buf.toString();
	}
}
