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
 */

package com.scriptographer.adm;

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

	private static EnumSet<DialogOption> getOptions(
			EnumSet<DialogOption> options) {
		options = options != null ? options.clone()
				: EnumSet.noneOf(DialogOption.class);
		// Always create modal dialogs hidden, and they show them in doModal()
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
			// Before showing the dialog, we need to initialize it, in order
			// to avoid flicker.
			initialize(true, false);
			Item item = nativeDoModal();
			ScriptographerEngine.setProgressVisible(progressVisible);
			return item;
		} finally {
			modal = false;
		}
	}

	public native void endModal();

	protected void onHide() {
		endModal();
		super.onHide();
	}

	protected void onActivate() {
		// This is part of a workaround for a bug in Illustrator: Invisible and
		// inactive modal dialogs seem to get active but remain invisible after
		// another modal dialog was deactivated, blocking the whole interface.
		// So if we receive an onActivate event but are not in a modal loop,
		// execute fixModal, which uses a native timer to deactivate the dialog
		// again right after activation. The fixModal field is used to let
		// onDeactivate know about this, and filter out the event.
		if (!modal) {
			fixModal = true;
			// Deactivates the invisible modal dialog right after it was
			// accidentally activated by a Illustrator CS3 bug. Immediately
			// deactivating it does not work.
			invokeLater(new Runnable() {
				public void run() {
					if (!isVisible()) {
						// Make sure that the focus goes back to whoever was
						// active before this invisible modal dialog got wrongly
						// activated.
						if (previousActiveDialog != null)
							previousActiveDialog.setActive(true);
						setActive(false);
					}
				}
			});
		} else {
			super.onActivate();
		}
	}

	protected void onDeactivate() {
		if (fixModal) {
			fixModal = false;
		} else {
			super.onDeactivate();
		}
	}
}
