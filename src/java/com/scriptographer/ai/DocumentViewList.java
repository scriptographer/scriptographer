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
 * File created on 07.04.2005.
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

	public Class<?> getComponentType() {
		return DocumentView.class;
	}
}
