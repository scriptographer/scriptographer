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
 * File created on Aug 27, 2007.
 *
 * $Id$
 */

package com.scriptographer.adm;

/**
 * @author lehni
 *
 */
public class AlertDialog extends ModalDialog {
	public AlertDialog(String title, String message) {
		this.setTitle(title);
		
		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
			{ TableLayout.FILL, TableLayout.PREFERRED }
		};

		TableLayout layout = new TableLayout(sizes);
		this.setLayout(layout);
		this.setMargin(10);

		ImageStatic logo = new ImageStatic(this);
		Image image = getImage("logo.png");
		logo.setImage(image);
		logo.setRightMargin(10);
		this.addToContent(logo, "0, 0, 0, 1, L, T");

		Static text = new Static(this);
		text.setText(message);
		text.setBottomMargin(10);
		this.addToContent(text, "1, 0, 2, 0, L, C");
				
		Button okButton = new Button(this);
		okButton.setText("  OK  ");
		this.addToContent(okButton, "1, 1, R, T");

		this.setDefaultItem(okButton);
	}

	public static void alert(String title, String message) {
		new AlertDialog(title, message).doModal();
	}
}
