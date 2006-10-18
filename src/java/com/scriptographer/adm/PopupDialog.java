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
 * $RCSfile: PopupDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2006/10/18 14:08:28 $
 */

package com.scriptographer.adm;

public class PopupDialog extends ModalDialog {
	public final static int
	// standard options from ADM:
	
	OPTION_AS_FLOATING = 1 << 8,
	//	 If this option is set for a dialog of style kADMPopupControldialogStyle
	//	 then ADM will create the dialog of kFloatingwindowclass. This option
	//	 is currently used only on MacOSX.

	// pseudo options, to simulate the various window styles (above 1 << 8)

	OPTION_CONTROL = 1 << 11;
	//   create a STYLE_POPUP_CONTROL instead of STYLE_POPUP

	public PopupDialog(int options) {
		// filter out the pseudo styles from the options:
		// (max. real bitis 8, and the mask is (1 << (max + 1)) - 1
		super(getStyle(options), options & ((1 << 9) - 1));		
	}

	public PopupDialog() {
		this(OPTION_NONE);
	}
	
	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(int options) {
		if ((options & OPTION_CONTROL) != 0) {
			return STYLE_POPUP;
		} else {
			return STYLE_POPUP_CONTROL;
		}
	}
}
