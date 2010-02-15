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
 * File created on Aug 27, 2007.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scriptographer.ui.layout.TableLayout;

/**
 * @author lehni
 * 
 * @jshide
 */
public class ConfirmDialog extends ModalDialog {
	public ConfirmDialog(String title, String message) {
		this.setTitle(title);
		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED },
			{ TableLayout.FILL, TableLayout.PREFERRED }
		};

		TableLayout layout = new TableLayout(sizes);
		this.setLayout(layout);
		this.setMargin(10);

		ImagePane logo = new ImagePane(this);
		Image image = getImage("logo.png");
		logo.setImage(image);
		logo.setRightMargin(10);

		this.addToContent(logo, "0, 0, 0, 1, L, T");

		TextPane text = new TextPane(this);
		text.setText(message);
		text.setBottomMargin(10);
		this.addToContent(text, "1, 0, 3, 0, L, C");
		
		Button cancelButton = new Button(this);
		cancelButton.setText("Cancel");
		cancelButton.setRightMargin(10);
		this.addToContent(cancelButton, "1, 1, R, T");
		
		Button okButton = new Button(this);
		okButton.setText("  OK  ");
		this.addToContent(okButton, "3, 1, R, T");

		this.setDefaultItem(okButton);
		this.setCancelItem(cancelButton);
	}

	public static boolean confirm(String title, String message) {
		ConfirmDialog dialog = new ConfirmDialog(title, message);
		return dialog.doModal() == dialog.getDefaultItem();
	}

}
