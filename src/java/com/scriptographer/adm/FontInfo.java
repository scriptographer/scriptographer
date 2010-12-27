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
 * File created on 23.10.2005.
 */

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class FontInfo {
	public int height;
	public int ascent;
	public int descent;
	public int leading;
	public int maxWidth;
	
	protected FontInfo(int height, int ascent, int descent, int leading,
			int maxWidth) {
		this.height = height;
		this.ascent = ascent;
		this.descent = descent;
		this.leading = leading;
		this.maxWidth = maxWidth;
	}
}