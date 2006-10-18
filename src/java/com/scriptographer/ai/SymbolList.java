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
 * File created on Oct 17, 2006.
 * 
 * $RCSfile: SymbolList.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2006/10/18 14:11:55 $
 */

package com.scriptographer.ai;

import com.scriptographer.util.AbstractReadOnlyList;
import com.scriptographer.util.StringIndexList;

public class SymbolList extends AbstractReadOnlyList implements StringIndexList {
	Document document;

	protected SymbolList(Document document) {
		this.document = document;
	}
	
	private static native int nativeGetLength(int docHandle);

	public int getLength() {
		return nativeGetLength(document.handle);
	}

	private static native int nativeGet(int docHandle, int index);

	public Object get(int index) {
		return Symbol.wrapHandle(nativeGet(document.handle, index), document);
	}

	private static native int nativeGet(int docHandle, String name);

	public Object get(String name) {
		return Symbol.wrapHandle(nativeGet(document.handle, name), document);
	}

	public Symbol getSymbol(int index) {
		return (Symbol) get(index);
	}

	public Symbol getSymbol(String name) {
		return (Symbol) get(name);
	}
}
