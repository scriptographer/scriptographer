/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2006 Juerg Lehni, http://www.scratchdisk.com.
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
 * $RCSfile: ConsoleDialog.java,v $
 * $Author: lehni $
 * $Revision: 1.11 $
 * $Date: 2006/11/04 11:47:27 $
 */

package com.scriptographer.gui;

import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;

import org.mozilla.javascript.Scriptable;

import com.scriptographer.ConsoleOutputStream;
import com.scriptographer.ConsoleOutputWriter;
import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;

public class ConsoleDialog extends FloatingDialog implements ConsoleOutputWriter {
	static final String title = "Scriptographer Console";

	public ConsoleDialog() throws Exception {
		super(FloatingDialog.OPTION_TABBED | FloatingDialog.OPTION_SHOW_CYCLE | Dialog.OPTION_RESIZING);
		setTitle(title);
		setSize(200, 240);

		textIn = new TextEdit(this, TextEdit.OPTION_MULTILINE) {
			public boolean onTrack(Tracker tracker) throws Exception {
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
						text = text.substring(start, end + 1);
					}
					ScriptographerEngine.getInstance().executeString(text, consoleScope);
				}
				return true;
			}
		};
		textIn.setSize(300, 100);
		textIn.setMinimumSize(200, 18);
		textIn.setTrackCallback(true);
				
		textOut = new TextEdit(this, TextEdit.OPTION_READONLY | TextEdit.OPTION_MULTILINE);
		textOut.setSize(300, 100);
		textOut.setMinimumSize(200, 18);
		textOut.setBackgroundColor(Drawer.COLOR_INACTIVE_TAB);
		
		consoleText = new StringBuffer();
		consoleScope = ScriptographerEngine.getInstance().createScope(null);

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
		this.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { 0.2, TableLayoutConstants.FILL, 15 } }, -1 , -1));
		this.addToLayout(textIn, "0, 0");
		this.addToLayout(textOut, "0, 1");
		
		ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1));
		// ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, 0, 0));
		buttons.add(clearButton);
		this.addToLayout(buttons, "0, 2");

		autoLayout();
		loadPreferences(title);

		// let the ConsoleOutputStream know about this consoleDialog
		ConsoleOutputStream.getInstance().setWriter(this);
	}

	static final Dimension buttonSize = new Dimension(27, 17);
	static final String newLine = java.lang.System.getProperty("line.separator");

	TextEdit textIn;
	TextEdit textOut;
	ImageButton clearButton;
	StringBuffer consoleText;
	Scriptable consoleScope;
	
	protected void onDestroy() {
		textOut = null;
		savePreferences(title);
	}
	
	public void println(String str) {
		if (textOut != null) {
			// if the text does not grow too long, remove old lines again:
			consoleText.append(str);
			consoleText.append(newLine);
			while (consoleText.length() >= 8192) {
				int pos = consoleText.indexOf(newLine);
				if (pos == -1) pos = consoleText.length() - 1;
				consoleText.delete(0, pos + 1);
			}
			textOut.setText(consoleText.toString());
			int end = consoleText.length();
			textOut.setSelection(end);
			/*
			textOut.update();
			textOut.invalidate();
			*/
			this.setVisible(true);
		}
	}
}
