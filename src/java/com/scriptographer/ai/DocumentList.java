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
 * $Revision: 1.1 $
 * $Date: 2005/02/23 22:01:00 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.AbstractReadOnlyList;

import java.util.WeakHashMap;

public class DocumentList extends AbstractReadOnlyList {
	private DocumentList() {
	}

	public native int getLength();
	private native int getDocumentHandle(int index);

	// use a WeakHashMap to keep track of already wrapped documents:
	private static WeakHashMap documents = new WeakHashMap();

	public Object get(int index) {
		int handle = getDocumentHandle(index);
		if (handle == 0)
			return null;
		Object key = new Integer(index);
		Document doc = (Document) documents.get(key);
		if (doc == null) {
			doc = new Document(handle);
			documents.put(key, doc);
		}
		return doc;
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
