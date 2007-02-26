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
public class ModalDialog extends Dialog {
	// standard options from ADM:
	/**
	 * To allow modal dialogs pass mouse down events through to
	 * the user dialog tracker on the application side.
	 */
	public final static int OPTION_PASS_MOUSEDOWN_EVENT = 1 << 1;
		
	/**
	 * 0 by default. If set, ADM Modal dialogs on Windows will have a 
	 * close box on the top right hand corner. Also there is a host
	 * option that a user can use if all dialogs in the application
	 * need that behavior. 
	 */
	public final static int OPTION_HAS_SYSTEM_CONTROLS = 1 << 7;
	
	// pseudo options, to simulate the various window styles
	public final static int OPTION_RESIZING = 1 << 20;

	/**
	 * Modal Alert, cannot be combined with OPTION_RESIZING
	 */
	public final static int OPTION_ALERT = 1 << 21;

	/**
	 * 	Modal System Alert, cannot be combined with OPTION_RESIZING
	 */
	public final static int OPTION_SYSTEM_ALERT = 1 << 22;
	
	private boolean doesModal = false;
	
	protected ModalDialog(int style, int options) {
		// Allways create ModalDialogs hidden, as they need to be shown
		// explicitely
		super(style, options | OPTION_HIDDEN);
	}

	public ModalDialog(int options) {
		this(getStyle(options), options);		
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
	
	protected void onHide() throws Exception {
		if (doesModal)
			this.endModal();
		super.onHide();
	}
}
