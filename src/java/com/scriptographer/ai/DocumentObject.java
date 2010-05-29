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
 * File created on Jul 21, 2008.
 */

package com.scriptographer.ai;

/**
 * A base class for all classes that are associated with a document.
 * 
 * @author lehni
 * 
 * @jshide
 */
public class DocumentObject extends NativeObject {

	protected Document document;

	/**
	 * Constructor for wrapping of existing or new document objects.
	 */
	protected DocumentObject(int handle, Document document) {
		super(handle);
		// Pass null (or docHandle == 0) for the working document.
		// This is save since nativeCreate activates the right document
		// through Document_active / Item_getInsertionPoint
		this.document = document == null
				? Document.getWorkingDocument()
				: document;
	}

	protected DocumentObject(int handle) {
		this(handle, null);
	}

	/**
	 * The document that the object belongs to.
	 */
	public Document getDocument() {
		return document;
	}
}
