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
 * File created on 14.03.2005.
 *
 * $Id$
 */

package com.scriptographer.adm;

/**
 * @author lehni
 */
public class FloatingDialog extends Dialog {
	// standard options from ADM:

	/**
	 * When creating tabbed dialog with a cycle button on the tab.
	 */
	public final static int OPTION_SHOW_CYCLE = 1 << 0;

	// pseudo options, to simulate the various window styles
	public final static int OPTION_RESIZING = 1 << 20;

	public final static int OPTION_TABBED = 1 << 21;

	public final static int OPTION_LEFTSIDED = 1 << 22;

	public final static int OPTION_NOCLOSE = 1 << 23;
	
	// TODO: define kADMDocumentWindowLayerDialogOption,
	// kADMPaletteLayerDialogOption

	public FloatingDialog(int options) {
		super(getStyle(options), options);		
	}

	public FloatingDialog() {
		this(OPTION_NONE);
	}
	
	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(int options) {
		if ((options & OPTION_TABBED) != 0) {
			if ((options & OPTION_RESIZING) != 0) {
				return STYLE_TABBED_RESIZING_FLOATING;
			} else {
				return STYLE_TABBED_FLOATING;
			}
		} else if ((options & OPTION_LEFTSIDED) != 0) {
			if ((options & OPTION_NOCLOSE) != 0) {
				return STYLE_LEFTSIDED_NOCLOSE_FLOATING;
			} else {
				return STYLE_LEFTSIDED_FLOATING;
			}
		} else {
			if ((options & OPTION_RESIZING) != 0) {
				return STYLE_RESIZING_FLOATING;
			} else if ((options & OPTION_NOCLOSE) != 0) {
				return STYLE_NOCLOSE_FLOATING;
			} else {
				return STYLE_FLOATING;
			}
		}
	}
}
