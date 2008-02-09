/*
 * Scriptographer
 * 
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 * 
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
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
 * File created on 04.11.2005.
 * 
 * $Id$
 */

package com.scriptographer.ai;

import com.scratchdisk.list.ExtendedList;
import com.scratchdisk.list.Lists;
import com.scratchdisk.list.ReadOnlyList;
import com.scratchdisk.list.StringIndexReadOnlyList;

/**
 * @author lehni
 */
public class FontFamily extends NativeWrapper implements ReadOnlyList, StringIndexReadOnlyList {
	
	protected FontFamily(int handle) {
		super(handle);
	}
	
	private native String nativeGetName(int handle);	
	
	public  String getName() {
		return nativeGetName(handle);
	}
	
	public boolean isValid() {
		return size() > 0;
	}

	/* TODO: check AIFont.h for many more features (OpenType, glyph bounds, etc)
	AIAPI AIErr (*GetFullFontName)( AIFontKey font, char *fontName, short maxName );
	AIAPI AIErr (*GetPostScriptFontName)( AIFontKey fontKey, char* postScriptFontName, short maxName );
	AIAPI AIErr (*GetFontStyleName)( AIFontKey font, char *styleName, short maxName );
	AIAPI AIErr (*GetFontFamilyUIName)( AIFontKey font, char *familyName, short maxName );
	AIAPI AIErr (*GetFontStyleUIName)( AIFontKey font, char *styleName, short maxName );
	AIAPI AIErr (*GetTypefaceName)( AITypefaceKey typeface, char *name, short maxName );
	AIAPI AIErr (*GetUserFontName)( AIFontKey font, char *userfontName, short maxName );
	AIAPI AIErr (*GetUserFontUIName)( AIFontKey font, char *userfontName, short maxName );
	AIAPI AIErr (*GetUserFontUINameUnicode)( AIFontKey fontKey, ASUnicode* userfontName, long maxName );
	AIAPI AIErr (*GetFontFamilyUINameUnicode)( AIFontKey fontKey, ASUnicode* familyName, long maxName );
	AIAPI AIErr (*GetFontStyleUINameUnicode)( AIFontKey fontKey, ASUnicode* styleName, long maxName );
	*/

	private native int nativeSize(int handle);
	
	public int size() {
		return nativeSize(handle);
	}
	
	private static native int nativeGet(int handle, int index);

	public Object get(int index) {
		return FontWeight.wrapHandle(nativeGet(handle, index));
	}

	public Object get(String name) {
		if (name != null) {
			for (int i = size() - 1; i >= 0; i--) {
				FontWeight weight = (FontWeight) get(i);
				if (name.equals(weight.getName()))
					return weight;
			}
		}
		return null;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public ExtendedList getSubList(int fromIndex, int toIndex) {
		return Lists.createSubList(this, fromIndex, toIndex);
	}
	
	protected static FontFamily wrapHandle(int handle) {
		return (FontFamily) wrapHandle(FontFamily.class, handle, null, false);
	}
}
