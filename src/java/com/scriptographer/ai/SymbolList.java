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
 * File created on Oct 17, 2006.
 */

package com.scriptographer.ai;

import com.scratchdisk.list.AbstractReadOnlyList;
import com.scratchdisk.list.ReadOnlyStringIndexList;

/**
 * @author lehni
 * 
 * @jshide
 */
public class SymbolList extends AbstractReadOnlyList<Symbol> implements
		ReadOnlyStringIndexList<Symbol> {
	Document document;

	protected SymbolList(Document document) {
		this.document = document;
	}
	
	private static native int nativeSize(int docHandle);

	public int size() {
		return nativeSize(document.handle);
	}

	private static native int nativeGet(int docHandle, int index);

	public Symbol get(int index) {
		return Symbol.wrapHandle(nativeGet(document.handle, index), document);
	}

	private static native int nativeGet(int docHandle, String name);

	public Symbol get(String name) {
		return Symbol.wrapHandle(nativeGet(document.handle, name), document);
	}

	public Class<?> getComponentType() {
		return Symbol.class;
	}
}
