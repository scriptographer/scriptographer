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
 * File created on  16.02.2005.
 */


package com.scriptographer.ai;

import com.scratchdisk.list.AbstractReadOnlyList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class DocumentList extends AbstractReadOnlyList<Document> {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private DocumentList() {
	}

	public native int size();
	
	private static native int nativeGet(int index);

	public Document get(int index) {
		return Document.wrapHandle(nativeGet(index));
	}

	private static DocumentList documents = null;

	public static DocumentList getInstance() {
		if (documents == null)
			documents = new DocumentList();

		return documents;
	}

	public Class<?> getComponentType() {
		return DocumentList.class;
	}
}
