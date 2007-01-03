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
 * File created on 07.04.2005.
 *
 * $RCSfile: DocumentViewList.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2007/01/03 15:10:16 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.AbstractReadOnlyList;

public class DocumentViewList extends AbstractReadOnlyList {
	
	private Document document;
	
	protected DocumentViewList(Document document) {
		this.document = document;
	}

	public int getLength() {
		return nativeGetLength(document.handle);
	}
	
	private static native int nativeGetLength(int docHandle);
		
	private static native int nativeGet(int docHandle, int index);

	public Object get(int index) {
		return DocumentView.wrapHandle(nativeGet(document.handle, index));
	}

	public DocumentView getView(int index) {
		return (DocumentView) get(index);
	}
}