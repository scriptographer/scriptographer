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
 * $RCSfile: MainWindow.java,v $
 * $Author: lehni $
 * $Revision: 1.2 $
 * $Date: 2005/03/10 23:13:31 $
 */

package com.scriptographer.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;
import com.scriptographer.ai.Tool;

public class MainWindow extends Dialog {

	public MainWindow() {
		super(Dialog.STYLE_TABBED_RESIZING_FLOATING, "Scriptographer", new Rectangle(100, 100, 400, 400), Dialog.OPTION_TABBED_DIALOG_SHOWS_CYCLE);
	}
	
	public static Image getImage(String filename) {
		try {
			return new Image(MainWindow.class.getClassLoader().getResource("com/scriptographer/gui/resources/" + filename));
		} catch (IOException e) {
			System.err.println(e);
			return new Image(1, 1, Image.TYPE_RGB);
		}
	}

	static final int lineHeight = 16;
	static final Rectangle buttonRect = new Rectangle(0, 0, 32, 17);
	
	HierarchyListBox scriptListBox;
	HierarchyList scriptList;
	FilenameFilter scriptFilter;
	Image folderImage;
	Image scriptImage;
	PushButton playButton;
	PushButton stopButton;
	PushButton refreshButton;
	PushButton consoleButton;
	PushButton newButton;
	PushButton tool1Button;
	PushButton tool2Button;
	
	protected void onCreate() throws Exception {
		// Script List:
		scriptListBox = new HierarchyListBox(this, new Rectangle(0, 0, 241, 20 * lineHeight), HierarchyListBox.STYLE_BLACK_RECT);

		scriptList = (HierarchyList) scriptListBox.getList();
//		scriptList.setBackgroundColor(Drawer.COLOR_BACKGROUND);
		scriptList.setEntrySize(2000, lineHeight);
		scriptList.setEntryTextRect(0, 0, 2000, lineHeight);
		scriptList.setTrackCallbackEnabled(true);

		// filter for hiding files:
		scriptFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.equals("CVS") && !name.startsWith(".") &&
						(name.endsWith(".js") || new File(dir, name).isDirectory());
			}
		};

		scriptImage = getImage("script.png");
		folderImage = getImage("folder.png");
		addFiles();
		// buttons:
		
		playButton = new PushButton(this, buttonRect, getImage("play.png")) {
			protected void onClick() throws Exception {
				execute();
			}
		};

		stopButton = new PushButton(this, buttonRect, getImage("stop.png")) {
			protected void onClick() {
				System.out.println("stop");
			}
		};

		refreshButton = new PushButton(this, buttonRect, getImage("refresh.png")) {
			protected void onClick() throws Exception {
				removeFiles();
				addFiles();
			}
		};

		consoleButton = new PushButton(this, buttonRect, getImage("console.png")) {
			protected void onClick() {
//				Mainvisible = !consoleDialog.visible;
			}
		};

		newButton = new PushButton(this, buttonRect, getImage("script.png")) {
			protected void onClick() {
				System.out.println("new");
			}
		};

		tool1Button = new ToolButton(this, buttonRect, 0);
		tool2Button = new ToolButton(this, buttonRect, 1);

		// layout:
		this.setInsets(-1, 0, -1, -1);
		this.setLayout(new BorderLayout());
		
		this.addToLayout(scriptListBox, BorderLayout.CENTER);
		
		ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1));
		buttons.add(playButton);
		buttons.add(stopButton);
		buttons.add(new Spacer(4, 0));
		buttons.add(refreshButton);
		buttons.add(new Spacer(4, 0));
		buttons.add(newButton);
		buttons.add(consoleButton);
		buttons.add(new Spacer(4, 0));
		buttons.add(tool1Button);
		buttons.add(tool2Button);
		this.addToLayout(buttons, BorderLayout.SOUTH);
	};
	
	void execute() throws Exception {
		ScriptEntry entry = (ScriptEntry) scriptList.getActiveEntry();
		if (entry != null && entry.file != null) {
			ScriptographerEngine.getInstance().executeFile(entry.file, null);
		}
	}
	
	void addFiles() throws IOException {
		addFiles(scriptList, ScriptographerEngine.baseDir);
	}
	
	class ToolButton extends PushButton {
		ToolButton(Dialog dialog, Rectangle rect, int index) throws IOException {
			super(dialog, rect, MainWindow.getImage("tool" + (index + 1) + ".png"));
			this.toolIndex = index;
			this.entryImage = MainWindow.getImage("tool" + (index + 1) + "script.png");
		}

		protected void onClick() throws Exception {
			ScriptEntry entry = (ScriptEntry) scriptList.getActiveEntry();
			if (entry != null && entry.file != null) {
				Tool.getTool(this.toolIndex).setScript(entry.file);
				if (entry != this.curEntry) {
					if (this.curEntry != null)
						this.curEntry.setPicture(scriptImage);
					entry.setPicture(this.entryImage);
					this.curEntry = entry;
				}
			}
		}

		ScriptEntry curEntry;
		int toolIndex;
		Image entryImage;
	}
	
	class ScriptEntry extends HierarchyListEntry {
		ScriptEntry(HierarchyList list, File file) throws IOException {
			super(list);
			this.file = file;
			isDirectory = file.isDirectory();
			setText(file.getName());
//			setBackgroundColor(Drawer.COLOR_BACKGROUND);
			if (isDirectory) {
				setExpanded(false);
				setPicture(folderImage);
				addFiles(createChildList(), file);
			} else {
				setPicture(scriptImage);
			}
		}
		
		File file;
		boolean isDirectory;

		protected void onTrack(Tracker tracker) throws Exception {
			if (tracker.getAction() == Tracker.ACTION_BUTTON_UP && (tracker.getModifiers() & Tracker.MODIFIER_DOUBLE_CLICK) != 0) {
				if (tracker.getPoint().x > this.getExpandArrowRect().getMaxX()) {
					if (isDirectory) {
						setExpanded(!isExpanded());
						((HierarchyList) getList()).invalidate();
					} else {
						execute();
					}
				}
			}
		}
}

	void addFiles(HierarchyList list, File dir) throws IOException {
		File[] files = dir.listFiles(scriptFilter);
		for (int i = 0; i < files.length; i++) {
			new ScriptEntry(list, files[i]);
		}
	}
	
	void removeFiles() {
		for (int i = scriptList.getNumEntries() - 1; i >= 0; i--)
			scriptList.removeEntry(i);
	}

	protected void onClose() {
//		this.destroy();
	}
}
