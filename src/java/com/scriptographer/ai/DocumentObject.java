/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
