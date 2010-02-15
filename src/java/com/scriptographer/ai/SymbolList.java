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
 * File created on Oct 17, 2006.
 * 
 * $Id$
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
}
