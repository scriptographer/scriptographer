/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 11.01.2005.
 *
 * $RCSfile: LayerList.java,v $
 * $Author: lehni $
 * $Revision: 1.3 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.StringIndexList;
import com.scriptographer.util.AbstractReadOnlyList;

public class LayerList extends AbstractReadOnlyList implements StringIndexList {
	Document document;

	protected LayerList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);
	private static native Object nativeGet(int docHandle, int index);
	private static native Object nativeGet(int docHandle, String name);

	public int size() {
		return nativeSize(document.documentHandle);
	}

	public Object get(int index) {
		return nativeGet(document.documentHandle, index);
	}

	public Object get(String name) {
		return nativeGet(document.documentHandle, name);
	}

	public Layer getLayer(int index) {
		return (Layer) get(index);
	}

	public Layer getLayer(String name) {
		return (Layer) get(name);
	}
}
