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
 * File created on 14.03.2005.
 *
 * $Id:ModalDialog.java 402 2007-08-22 23:24:49Z lehni $
 */

package com.scriptographer.ui;

import java.util.EnumSet;

import com.scratchdisk.util.EnumUtils;
import com.scriptographer.ScriptographerEngine;

/**
 * @author lehni
 */
public class ModalDialog extends Dialog {

	private boolean modal;

	private boolean fixModal;
	
	protected ModalDialog(int style, EnumSet<DialogOption> options) {
		// Always create ModalDialogs hidden, as they need to be shown
		// explicitly
		super(style, getOptions(options));
	}

	public ModalDialog(EnumSet<DialogOption> options) {
		this(getStyle(options), options);		
	}

	public ModalDialog(DialogOption[] options) {
		this(EnumUtils.asSet(options));
	}

	public ModalDialog() {
		this((EnumSet<DialogOption>) null);
	}

	private static EnumSet<DialogOption> getOptions(EnumSet<DialogOption> options) {
		options = options != null ? options.clone() : EnumSet.noneOf(DialogOption.class);
		options.add(DialogOption.HIDDEN);
		return options;
	}

	/*
	 * Extract the style from the pseudo options:
	 */
	private static int getStyle(EnumSet<DialogOption> options) {
		if (options != null) {
			if (options.contains(DialogOption.RESIZING)) {
				return STYLE_RESIZING_MODAL;
			} else if (options.contains(DialogOption.ALERT)) {
				return STYLE_ALERT;
			} else if (options.contains(DialogOption.SYSTEM_ALERT)) {
				return STYLE_SYSTEM_ALERT;
			}
		}
		return STYLE_MODAL;
	}

	private native Item nativeDoModal();

	public Item doModal() {
		boolean progressVisible = ScriptographerEngine.getProgressVisible();
		ScriptographerEngine.setProgressVisible(false);
		try {
			modal = true;
			Item item = nativeDoModal();
			ScriptographerEngine.setProgressVisible(progressVisible);
			return item;
		} finally {
			modal = false;
		}
	}

	public native void endModal();

	private native void fixModal();

	protected void onHide() throws Exception {
		this.endModal();
		super.onHide();
	}

	protected void onActivate() throws Exception {
		// This is part of a workaround for a bug in Illustrator:
		// invisible and inactive modal dialogs seem to get active
		// but remain invisible after another modal dialog was 
		// deactivated. So if we receive an onActivate event but
		// are not in a modal loop, execute fixModal, which uses
		// a native timer to deactivate the dialog again right after
		// activation. The fixModal field is used to let onDeactivate
		// know about this, and filter out the event.
		if (!modal) {
			fixModal = true;
			this.fixModal();
		} else {
			super.onActivate();
		}
	}

	protected void onDeactivate() throws Exception {
		if (fixModal) {
			fixModal = false;
		} else {
			super.onDeactivate();
		}
	}
}
