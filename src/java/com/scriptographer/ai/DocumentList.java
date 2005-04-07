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
 * File created on  16.02.2005.
 *
 * $RCSfile: DocumentList.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/04/07 20:12:55 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.AbstractReadOnlyList;

public class DocumentList extends AbstractReadOnlyList {
	private DocumentList() {
	}

	public native int size();
	
	private static native int nativeGetActiveDocument(); 
	
	public static Document getActiveDocument() {
		return Document.wrapHandle(nativeGetActiveDocument());
	}
	
	private static native int nativeGetDocument(int index);

	public Object get(int index) {
		return Document.wrapHandle(nativeGetDocument(index));
	}

	public Document getDocument(int index) {
		return (Document) get(index);
	}

	private static DocumentList documentList;

	public static DocumentList getInstance() {
		if (documentList == null)
			documentList = new DocumentList();

		return documentList;
	}
}
