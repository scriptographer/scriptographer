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
 * File created on 08.03.2005.
 *
 * $RCSfile: ConsoleWindow.java,v $
 * $Author: lehni $
 * $Revision: 1.1 $
 * $Date: 2005/03/10 22:55:18 $
 */

package com.scriptographer.gui;

import java.awt.FlowLayout;
import java.awt.Rectangle;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.ConsoleOutputStream;
import com.scriptographer.ConsoleOutputWriter;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;

public class ConsoleWindow extends Dialog implements ConsoleOutputWriter {

	public ConsoleWindow() {
		super(Dialog.STYLE_TABBED_RESIZING_FLOATING, "Scriptographer Console", new Rectangle(200, 200, 400, 300), Dialog.OPTION_TABBED_DIALOG_SHOWS_CYCLE);
		// let the ConsoleOutputStream know about this console
		ConsoleOutputStream.getInstance().setWriter(this);
	}

	static final Rectangle buttonRect = new Rectangle(0, 0, 32, 17);
	static final String newLine = java.lang.System.getProperty("line.separator");

	TextEdit textIn;
	TextEdit textOut;
	PushButton clearButton;
	StringBuffer consoleText;
	Scriptable consoleScope;

	protected void onCreate() throws Exception {
		textIn = new TextEdit(this, new Rectangle(0, 0, 300, 50), "", TextEdit.STYLE_MULTILINE) {
			public void onTrack(Tracker tracker) throws Exception {
				if (tracker.getAction() == Tracker.ACTION_KEY_STROKE && tracker.getVirtualKey() == Tracker.KEY_RETURN) {
					// enter was pressed in the input field. determine the current line:
					String text = this.getText();
					char ch;
					int end = this.getSelection()[1] - 1;
					ch = text.charAt(end--);
					if (ch == '\n' || ch == '\r') { // empty line?
						text = "";
					} else {
						while (end >= 0 && ((ch = text.charAt(end)) == '\n' || ch == '\r'))
							end--;
						int start = end;
						end++;
						while (start >= 0 && ((ch = text.charAt(start)) != '\n' && ch != '\r'))
							start--;
						start++;
						text = text.substring(start, end);
					}
					ScriptographerEngine.getInstance().executeString(text, consoleScope);
				}
			}
		};
		textIn.setMinimumSize(200, 18);
		textIn.setTrackCallbackEnabled(true);
				
		textOut = new TextEdit(this, new Rectangle(0, 0, 300, 100), "", TextEdit.STYLE_READONLY | TextEdit.STYLE_MULTILINE);
		textOut.setMinimumSize(200, 18);
		textOut.setBackgroundColor(Drawer.COLOR_INACTIVE_TAB);
		
		consoleText = new StringBuffer();
		consoleScope = ScriptographerEngine.getInstance().createScope();

		// buttons:
		clearButton = new PushButton(this, buttonRect, MainWindow.getImage("refresh.png")) {
			public void onClick() {
				textOut.setText("");
				consoleText.setLength(0);
			}
		};
		
		// layout:
		this.setInsets(-1, -1, -1, -1);
		this.setLayout(new TableLayout(new double[][] { { TableLayout.FILL }, { 0.2, TableLayout.FILL, 15 } }, -1 , -1));
		this.addToLayout(textIn, "0, 0");
		this.addToLayout(textOut, "0, 1");
		
		ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1));
		buttons.add(clearButton);
		this.addToLayout(buttons, "0, 2");
	}

	protected void onDestroy() {
		textOut = null;
	}
	
	public void println(String str) {
		if (textOut != null) {
			// if the text does not grow too long, remove old lines again:
			consoleText.append(str);
			consoleText.append(newLine);
			while (consoleText.length() >= 32768) {
				int pos = consoleText.indexOf(newLine);
				if (pos == -1) pos = consoleText.length() - 1;
				consoleText.delete(0, pos + 1);
			}
			textOut.setText(consoleText.toString());
			int end = consoleText.length();
			textOut.setSelection(end);
			ConsoleWindow.this.setVisible(true);
		}
	}
}
