/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2005 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: ModalDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/25 00:27:57 $
 */

package com.scriptographer.adm;

public class ModalDialog extends Dialog {
	public final static int
		// standard options from ADM:
		OPTION_PASS_MOUSEDOWN_EVENT = 1 << 1,
		//	 To allow modal dialogs pass mouse down events through to
		//	 the user dialog tracker on the application side.
		
		OPTION_HAS_SYSTEM_CONTROLS = 1 << 7,
		//	 0 by default. If set, ADM Modal dialogs on Windows will have a 
		//	 close box on the top right hand corner. Also there is a host
		//	 option that a user can use if all dialogs in the application
		//	 need that behavior. 
		//	 dagashe:09/29/00:added for Acrobat 5.0 bug #382265
	
		// pseudo options, to simulate the various window styles (above 1 << 8)

		OPTION_ALERT = 1 << 11,
		//   Modal Alert, cannot be combined with OPTION_RESIZING
		OPTION_SYSTEM_ALERT = 1 << 12;
		//   Modal System Alert, cannot be combined with OPTION_RESIZING
	
	private boolean doesModal = false;
	
	protected ModalDialog(int style, int options) {
		super(style, options);
	}

	public ModalDialog(int options) {
		// filter out the pseudo styles from the options:
		// (max. real bitis 8, and the mask is (1 << (max + 1)) - 1
		super(getStyle(options), options & ((1 << 9) - 1));		
	}

	public ModalDialog() {
		this(OPTION_NONE);
	}

	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(int options) {
		if ((options & OPTION_RESIZING) != 0) {
			return STYLE_RESIZING_MODAL;
		} else if ((options & OPTION_ALERT) != 0) {
			return STYLE_ALERT;
		} else if ((options & OPTION_SYSTEM_ALERT) != 0) {
			return STYLE_SYSTEM_ALERT;
		}
		return STYLE_MODAL;
	}

	public native Item doModal();
	public native void endModal();
}
