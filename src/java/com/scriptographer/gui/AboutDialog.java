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
 * File created on 25.03.2005.
 *
 * $RCSfile: AboutDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.4 $
 * $Date: 2005/04/20 13:49:36 $
 */

package com.scriptographer.gui;

import java.awt.Point;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.Dialog;
import com.scriptographer.adm.ModalDialog;
import com.scriptographer.adm.Button;
import com.scriptographer.adm.Static;
import com.scriptographer.adm.TableLayout;
import com.scriptographer.adm.Tracker;

public 	class AboutDialog extends ModalDialog {
	static AboutDialog dialog = null;
	
	AboutDialog() {
		setTitle("About Scriptographer");
		
		Static text = new Static(this) {
			protected boolean onTrack(Tracker tracker) {
				if (tracker.getModifiers() == Tracker.MODIFIER_CLICK) {
					Point pt = tracker.getPoint();
					int height = this.getTextSize(" ", -1).height;
					int line = pt.y / height;
					String url = null;
					if (line == 1) {
						url = "http://www.scriptographer.com";
					} else if (line == 4) {
						url = "http://www.scratchdisk.com";
					}
					if (url != null) {
						if (pt.x < this.getTextSize(url, -1).width) {
							// TODO: get launch to work with urls on mac
							ScriptographerEngine.launch(url);
						}
					}
				}
				return true;
			}
		};
		text.setTrackCallback(true);
		String newLine = System.getProperty("line.separator");
		text.setText(
			"Scriptographer 0.5" + newLine + 
			"http://www.scriptographer.com" +  newLine +
			newLine +
			"© 2001-2005 JŸrg Lehni" + newLine + 
			"http://www.scratchdisk.com");
		
		Button okButton = new Button(this);
		okButton.setFont(Dialog.FONT_PALETTE);
		okButton.setText("OK");
		setDefaultItem(okButton);

		TableLayout layout = new TableLayout(new double[][] {
			{ TableLayout.FILL, TableLayout.PREFERRED },
			{ TableLayout.FILL, TableLayout.PREFERRED }}, 4, 4);
		setLayout(layout);
		setInsets(10, 10, 10, 10);
		
		addToLayout(text, "0, 0, 1, 0");
		addToLayout(okButton, "1, 1");
		
		autoLayout();
	}
	
	public static void show() {
		if (dialog == null)
			dialog = new AboutDialog();
		dialog.doModal();
	}
}
