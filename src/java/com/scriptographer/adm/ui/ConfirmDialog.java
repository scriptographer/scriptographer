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
import com.scriptographer.adm.Size;
import com.scriptographer.adm.TextPane;
import com.scriptographer.adm.layout.TableLayout;

/**
 * @author lehni
 * 
 * @jshide
 */
public class ConfirmDialog extends ModalDialog {
	public ConfirmDialog(String title, String message) {
		setName("Scriptographer Confirm");
		setTitle(title);
	
		double[][] sizes = {
			{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED,
				TableLayout.PREFERRED },
			{ TableLayout.FILL, TableLayout.PREFERRED }
		};

		TableLayout layout = new TableLayout(sizes);
		this.setLayout(layout);
		this.setMargin(10);

		ImagePane logo = new ImagePane(this);
		logo.setImage(AdmUiFactory.getImage("logo.png"));
		logo.setMargin(-4, 4, -4, -4);
		this.addToContent(logo, "0, 0, 0, 1, L, T");

		TextPane text = new TextPane(this);
		if (!Pattern.compile("[\n\r]").matcher(message).find()
				&& getTextSize(message).width > 320)
			text.setMaximumSize(new Size(320, -1));
		text.setMinimumSize(240, -1);
		text.setText(message);
		text.setMarginBottom(8);
		this.addToContent(text, "1, 0, 3, 0, L, C");
		
		Button cancelButton = new Button(this);
		cancelButton.setText("Cancel");
		cancelButton.setMarginRight(10);
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
