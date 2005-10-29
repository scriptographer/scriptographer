/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2004-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 28.10.2005.
 * 
 * $RCSfile: TextRanges.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/10/29 10:18:38 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.ExtendedArrayList;
import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.ReadOnlyList;

public class TextRanges extends AIObject implements ReadOnlyList {
	
	public TextRanges(int handle) {
		super(handle);
	}

	public native void removeAll();

	public native int getLength();

	public native Object get(int index);
	
	protected native void finalize();

	public boolean isEmpty() {
		return getLength() == 0;
	}
	
	public ExtendedList subList(int fromIndex, int toIndex) {
		ExtendedArrayList list = new ExtendedArrayList(toIndex - fromIndex);
		for (int i = fromIndex; i < toIndex; i++)
			list.add(get(i));
		return list;
	}
}
