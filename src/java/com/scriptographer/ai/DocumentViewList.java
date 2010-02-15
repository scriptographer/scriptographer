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
 * File created on 07.04.2005.
 *
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractReadOnlyList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class DocumentViewList extends AbstractReadOnlyList<DocumentView> {
	
	private Document document;
	
	protected DocumentViewList(Document document) {
		this.document = document;
	}

	public int size() {
		return nativeSize(document.handle);
	}
	
	private static native int nativeSize(int docHandle);
		
	private static native int nativeGet(int docHandle, int index);

	public DocumentView get(int index) {
		return DocumentView.wrapHandle(nativeGet(document.handle, index), document);
	}
}
