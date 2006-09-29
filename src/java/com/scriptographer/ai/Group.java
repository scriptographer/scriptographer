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
 * File created on 03.12.2004.
 *
 * $RCSfile: Group.java,v $
 * $Author: lehni $
 * $Revision: 1.7 $
 * $Date: 2006/09/29 22:35:26 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.ExtendedList;
import com.scriptographer.util.Lists;

public class Group extends Art {
	protected Group(long handle, Document document) {
		super(handle, document);
	}

	protected Group(Document document) {
		super(TYPE_GROUP, document);
	}
	
	protected Group(Document document, ExtendedList children) {
		this(document);
		for (int i = 0; i < children.getLength(); i++) {
			Object obj = children.get(i);
			if (obj instanceof Art)
				this.appendChild((Art) obj);
		}
	}
	
	protected Group(Document document, Art[] children) {
		this(document, Lists.asList(children));
	}

	/**
	 * Creates a group object
	 */
	public Group() {
		super(TYPE_GROUP, null);
	}
	
	public Group(ExtendedList children) {
		this(null, children);
	}

	public Group(Art[] children) {
		this(null, children);
	}
	
	public native boolean isClipped();
	public native void setClipped(boolean clipped);
}