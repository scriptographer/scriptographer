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
 * File created on 08.03.2005.
 *
 * $Id$
 */

package com.scriptographer.gui;

import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import com.scriptographer.ConsoleOutputStream;
import com.scriptographer.ConsoleOutputWriter;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;
import com.scriptographer.script.ScriptScope;
import com.scriptographer.script.ScriptEngine;

/**
 * @author lehni
 */
public class ConsoleDialog extends FloatingDialog implements
		ConsoleOutputWriter {
	static final String title = "Scriptographer Console";

	public ConsoleDialog() throws Exception {
		super(FloatingDialog.OPTION_TABBED | FloatingDialog.OPTION_SHOW_CYCLE
			| FloatingDialog.OPTION_RESIZING | Dialog.OPTION_REMEMBER_PLACING);

		engine = ScriptEngine.getInstanceByName("JavaScript");

		setTitle(title);

		textIn = new TextEdit(this, TextEdit.OPTION_MULTILINE) {
			public boolean onTrack(Tracker tracker) throws Exception {
				if (tracker.getAction() == Tracker.ACTION_KEY_STROKE
					&& tracker.getVirtualKey() == Tracker.KEY_RETURN) {
					// enter was pressed in the input field. determine the
					// current line:
					String text = this.getText();
					char ch;
					int end = this.getSelection()[1] - 1;
					ch = text.charAt(end--);
					if (ch == '\n' || ch == '\r') { // empty line?
						text = "";
					} else {
						while (end >= 0
							&& ((ch = text.charAt(end)) == '\n' || ch == '\r'))
							end--;
						int start = end;
						end++;
						while (start >= 0
							&& ((ch = text.charAt(start)) != '\n' && ch != '\r'))
							start--;
						start++;
						text = text.substring(start, end + 1);
					}
					engine.evaluate(text, consoleScope);
				}
				return true;
			}
		};
		textIn.setSize(300, 100);
		textIn.setMinimumSize(200, 18);
		textIn.setTrackCallback(true);

		textOut = new TextEdit(this, TextEdit.OPTION_READONLY
			| TextEdit.OPTION_MULTILINE) {
			protected void onDraw(Drawer drawer) {
				// Workaround for mac, where TextEdit fields with a background
				// color
				// do not get completely filled
				// Fill in the missing parts.
				drawer.setColor(Drawer.COLOR_INACTIVE_TAB);
				Rectangle rect = drawer.getBoundsRect();
				// a tet line with the small font is 11 pixels heigh. there
				// seems to be a shift,
				// which was detected by trial and error. This might change in
				// future versions!
				int height = rect.height - (rect.height - 6) % 11 - 3;
				// 18 is the width of the scrollbar. This might change in future
				// versions!
				drawer.fillRect(rect.width - 18, 0, 1, height);
				drawer.fillRect(0, height, rect.width - 1, rect.height - height
					- 2);
			}
		};
		// the onDraw workaround for display problems is only needed on mac
		textOut.setDrawCallback(ScriptographerEngine.isMacintosh());
		textOut.setSize(300, 100);
		textOut.setMinimumSize(200, 18);
		textOut.setBackgroundColor(Drawer.COLOR_INACTIVE_TAB);

		consoleText = new StringBuffer();
		consoleScope = engine.createScope();

		// buttons:
		clearButton = new ImageButton(this) {
			public void onClick() {
				textOut.setText("");
				consoleText.setLength(0);
			}
		};
		clearButton.setImage(MainDialog.getImage("refresh.png"));
		clearButton.setSize(buttonSize);

		// layout:
		this.setInsets(-1, -1, -1, -1);
		this.setLayout(new TableLayout(new double[][] {
			{ TableLayoutConstants.FILL },
			{ 0.2, TableLayoutConstants.FILL, 15 } }, -1, -1));
		this.addToLayout(textIn, "0, 0");
		this.addToLayout(textOut, "0, 1");

		ItemContainer buttons = new ItemContainer(new FlowLayout(
			FlowLayout.LEFT, -1, -1));
		// ItemContainer buttons = new ItemContainer(new
		// FlowLayout(FlowLayout.LEFT, 0, 0));
		buttons.add(clearButton);
		this.addToLayout(buttons, "0, 2");

		// let the ConsoleOutputStream know about this consoleDialog
		ConsoleOutputStream.setWriter(this);
	}

	static final Dimension buttonSize = new Dimension(27, 17);

	static final String newLine = java.lang.System
		.getProperty("line.separator");

	TextEdit textIn;

	TextEdit textOut;

	ImageButton clearButton;

	StringBuffer consoleText;

	ScriptEngine engine;
	ScriptScope consoleScope;

	protected void onInitialize() {
		showText();
	}

	protected void onDestroy() {
		textOut = null;
	}

	protected void showText() {
		textOut.setText(consoleText.toString());
		int end = consoleText.length();
		textOut.setSelection(end);
		/*
		 * textOut.update(); textOut.invalidate();
		 */
		this.setVisible(true);
	}

	public void println(String str) {
		// if the text does not grow too long, remove old lines again:
		consoleText.append(str);
		consoleText.append(newLine);
		while (consoleText.length() >= 8192) {
			int pos = consoleText.indexOf(newLine);
			if (pos == -1)
				pos = consoleText.length() - 1;
			consoleText.delete(0, pos + 1);
		}
		if (isInitialized())
			showText();
	}
}
