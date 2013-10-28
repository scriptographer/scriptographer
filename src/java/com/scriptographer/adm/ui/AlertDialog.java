/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
 * 
 * File created on Aug 27, 2007.
 */

package com.scriptographer.adm.ui;

import java.util.regex.Pattern;

import com.scriptographer.adm.Button;
import com.scriptographer.adm.ImagePane;
import com.scriptographer.adm.ModalDialog;
import com.scriptographer.ui.Size;
import com.scriptographer.adm.TextPane;
import com.scriptographer.adm.layout.TableLayout;

/**
 * @author lehni
 * 
 * @jshide
 */
public class AlertDialog extends ModalDialog {
	public AlertDialog(String title, String message) {
		// Set a name for auto destruction of dialogs...
		setName("Scriptographer Alert");
		setTitle(title);
		
		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
			{ TableLayout.FILL, TableLayout.PREFERRED }
		};

		TableLayout layout = new TableLayout(sizes);
		setLayout(layout);
		setMargin(10);

		ImagePane logo = new ImagePane(this);
		logo.setImage(AdmUiFactory.getImage("logo.png"));
		logo.setMargin(-4, 4, -4, -4);
		addToContent(logo, "0, 0, 0, 1, L, T");

		TextPane text = new TextPane(this);
		if (!Pattern.compile("[\n\r]").matcher(message).find()
				&& getTextSize(message).width > 320)
			text.setMaximumSize(new Size(320, -1));
		text.setMinimumSize(240, -1);
		text.setText(message);
		text.setMarginBottom(8);
		addToContent(text, "1, 0, 2, 0, L, C");

		Button okButton = new Button(this);
		okButton.setText("  OK  ");
		addToContent(okButton, "1, 1, R, T");

		setDefaultItem(okButton);
	}

	public static void alert(String title, String message) {
		new AlertDialog(title, message).doModal();
	}
}
