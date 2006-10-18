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
 * File created on 14.03.2005.
 *
 * $RCSfile: FloatingDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/18 14:08:30 $
 */

package com.scriptographer.adm;

public class FloatingDialog extends Dialog {
	public final static int
		// standard options from ADM:

		OPTION_SHOW_CYCLE = 1 << 0,
		//	 When creating tabbed dialog with a cycle button on the tab.
	
		// pseudo options, to simulate the various window styles (above 1 << 8)

		OPTION_TABBED = 1 << 11,
		//   Modal Alert, cannot be combined with OPTION_RESIZING

		OPTION_LEFTSIDED = 1 << 12,

		OPTION_NOCLOSE = 1 << 13;
		//   Modal System Alert, cannot be combined with OPTION_RESIZING

	public FloatingDialog(int options) {
		// filter out the pseudo styles from the options:
		// (max. real bitis 8, and the mask is (1 << (max + 1)) - 1
		super(getStyle(options), options & ((1 << 9) - 1));		
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
