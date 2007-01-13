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
 * $RCSfile$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.scriptographer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import com.scriptographer.ScriptographerEngine;
import com.scriptographer.adm.*;
import com.scriptographer.ai.Tool;

public class MainDialog extends FloatingDialog {
	static final String title = "Scriptographer";
	static final int lineHeight = 16;
	static final Dimension buttonSize = new Dimension(27, 17);
	
	HierarchyList scriptList;
	FilenameFilter scriptFilter;
	Image folderImage;
	Image scriptImage;
	ToolButton tool1Button;
	ToolButton tool2Button;

	ConsoleDialog consoleDialog;

	public MainDialog(ConsoleDialog consoleDlg) throws IOException {
		super(FloatingDialog.OPTION_TABBED | FloatingDialog.OPTION_SHOW_CYCLE |
				FloatingDialog.OPTION_RESIZING | Dialog.OPTION_REMEMBER_PLACING);

		this.consoleDialog = consoleDlg;
		
		setTitle(title);
		setIncrement(1, lineHeight);

		// add the menus:
		// use a space in the beginning of the name so it appears on top of all entries :)
		MenuItem scriptographerItem = new MenuItem(MenuGroup.GROUP_TOOL_PALETTES, " Scriptographer");

		new MenuItem(scriptographerItem, "Main") {
			public void onClick() {
				MainDialog.this.setVisible(true);
			}
		};

		new MenuItem(scriptographerItem, "Console") {
			public void onClick() {
				consoleDialog.setVisible(true);
			}
		};

		new MenuItem(scriptographerItem, "Reload") {
			public void onClick() {
				ScriptographerEngine.reload();
			}
		};

		// add the popup menu
		PopupMenu menu = getPopupMenu();

		ListEntry executeEntry = new ListEntry(menu) {
			protected void onClick() throws Exception {
				execute();
			}
		};
		executeEntry.setText("Execute Script");

		ListEntry refreshEntry = new ListEntry(menu) {
			protected void onClick() throws IOException {
				refreshFiles();
			}
		};
		refreshEntry.setText("Refresh List");

		ListEntry consoleEntry = new ListEntry(menu) {
			protected void onClick() {
				consoleDialog.setVisible(!consoleDialog.isVisible());
			}
		};
		consoleEntry.setText("Show / Hide Console");

		ListEntry scriptDirEntry = new ListEntry(menu) {
			protected void onClick() throws Exception {
				if (ScriptographerEngine.chooseScriptDirectory())
					refreshFiles();
			}
		};
		scriptDirEntry.setText("Set Script Directory...");
		
		ListEntry aboutEntry = new ListEntry(menu) {
			protected void onClick() {
				AboutDialog.show();
			}
		};
		aboutEntry.setText("About Scriptographer...");
		
		ListEntry helpEntry = new ListEntry(menu) {
			protected void onClick() {
				ScriptographerEngine.launch("file://" + new File(ScriptographerEngine.getPluginDirectory(), "doc/index.html"));
			}
		};
		helpEntry.setText("Help...");

		ListEntry separatorEntry = new ListEntry(menu);
		separatorEntry.setSeparator(true);
		
		ListEntry reloadEntry = new ListEntry(menu) {
			protected void onClick() {
				ScriptographerEngine.reload();
			}
		};
		reloadEntry.setText("Reload");

		menu.setVisible(true);

		// Script List:
		scriptList = new HierarchyList(this);
		scriptList.setStyle(List.STYLE_BLACK_RECT);
		scriptList.setSize(208, 20 * lineHeight);
		scriptList.setMinimumSize(208, 8 * lineHeight);

//		scriptList.setBackgroundColor(Drawer.COLOR_BACKGROUND);
		scriptList.setEntrySize(2000, lineHeight);
		scriptList.setEntryTextRect(0, 0, 2000, lineHeight);
		scriptList.setTrackEntryCallback(true);

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
		ImageButton playButton = new ImageButton(this) {
			protected void onClick() throws Exception {
				execute();
			}
		};
		playButton.setImage(getImage("play.png"));
		playButton.setSize(buttonSize);

		ImageButton stopButton = new ImageButton(this) {
			protected void onClick() {
				ScriptographerEngine.stopAll();
			}
		};
		stopButton.setImage(getImage("stop.png"));
		stopButton.setSize(buttonSize);

		ImageButton refreshButton = new ImageButton(this) {
			protected void onClick() throws Exception {
				refreshFiles();
			}
		};
		refreshButton.setImage(getImage("refresh.png"));
		refreshButton.setSize(buttonSize);

		ImageButton consoleButton = new ImageButton(this) {
			protected void onClick() {
				consoleDialog.setVisible(!consoleDialog.isVisible());
			}
		};
		consoleButton.setImage(getImage("console.png"));
		consoleButton.setSize(buttonSize);

		ImageButton newButton = new ImageButton(this) {
			protected void onClick() throws IOException {
				createFile();
			}
		};
		newButton.setImage(getImage("script.png"));
		newButton.setSize(buttonSize);

		tool1Button = new ToolButton(this, 0);
		tool2Button = new ToolButton(this, 1);

		// layout:
		this.setInsets(-1, 0, -1, -1);
		this.setLayout(new BorderLayout());
		
		this.addToLayout(scriptList, BorderLayout.CENTER);
		
		ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1));
		// ItemContainer buttons = new ItemContainer(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
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
	}

	public static Image getImage(String filename) {
		try {
			return new Image(MainDialog.class.getClassLoader().getResource("com/scriptographer/gui/resources/" + filename));
		} catch (IOException e) {
			System.err.println(e);
			return new Image(1, 1, Image.TYPE_RGB);
		}
	}

	void execute() throws Exception {
		ScriptEntry entry = (ScriptEntry) scriptList.getActiveLeaf();
		if (entry != null && entry.file != null) {
			ScriptographerEngine.executeFile(entry.file, null);
		}
	}
	
	void addFiles() throws IOException {
		File dir = ScriptographerEngine.getScriptDirectory();
		if (dir != null)
			addFiles(scriptList, dir);
	}
	
	class ToolButton extends ImageButton {
		ToolButton(Dialog dialog, int index) throws IOException {
			super(dialog);
			setImage(MainDialog.getImage("tool" + (index + 1) + ".png"));
			toolIndex = index;
			entryImage = MainDialog.getImage("tool" + (index + 1) + "script.png");
			setSize(buttonSize);
		}

		protected void onClick() throws Exception {
			ScriptEntry entry = (ScriptEntry) scriptList.getActiveLeaf();
			if (entry != null && entry.file != null) {
				Tool.getTool(toolIndex).setScript(entry.file);
				if (entry.file != curFile) {
					ScriptEntry curEntry = (ScriptEntry) files.get(curFile);
					if (curEntry != null)
						curEntry.setImage(scriptImage);
					entry.setImage(entryImage);
					curFile = entry.file;
				}
			}
		}
		
		protected void updatePicture() throws IOException {
			ScriptEntry curEntry = (ScriptEntry) files.get(curFile);
			if (curEntry != null)
				curEntry.setImage(entryImage);
		}

		File curFile;
		int toolIndex;
		Image entryImage;
	}

	HashMap directories = new HashMap();
	HashMap files = new HashMap();

	class ScriptEntry extends HierarchyListEntry {
		ScriptEntry(HierarchyList list, File file) throws IOException {
			super(list);
			this.file = file;
			isDirectory = file.isDirectory();
			setText(file.getName());
//			setBackgroundColor(Drawer.COLOR_BACKGROUND);
			if (isDirectory) {
				setExpanded(false);
				setImage(folderImage);
				addFiles(createChildList(), file);
				directories.put(file, this);
			} else {
				setImage(scriptImage);
				files.put(file, this);
			}
		}
		
		File file;
		boolean isDirectory;

		protected boolean onTrack(Tracker tracker) throws Exception {
			if (tracker.getAction() == Tracker.ACTION_BUTTON_UP && (tracker.getModifiers() & Tracker.MODIFIER_DOUBLE_CLICK) != 0) {
				if (tracker.getPoint().x > this.getExpandArrowRect().getMaxX()) {
					if (isDirectory) {
						setExpanded(!isExpanded());
						((HierarchyList) getList()).invalidate();
					} else {
						// execute();
						ScriptographerEngine.launch(file.getPath());
					}
				}
			}
			return true;
		}
	}

	void addFiles(HierarchyList list, File dir) throws IOException {
		File[] files = dir.listFiles(scriptFilter);
		for (int i = 0; i < files.length; i++) {
			new ScriptEntry(list, files[i]);
		}
	}
	
	void removeFiles() {
		scriptList.removeAll();
		directories.clear();
		files.clear();
	}
	
	void refreshFiles() throws IOException {
		// collect expanded items:
		HashMap expandedDirs = new HashMap();
		Iterator it = directories.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) it.next();
			File file = (File) mapEntry.getKey();
			ScriptEntry entry = (ScriptEntry) mapEntry.getValue();
			if (entry.isDirectory && entry.isExpanded()) {
				expandedDirs.put(file, file);
			}
		}

		removeFiles();
		addFiles();
		
		// now restore the expanded state:
		it = expandedDirs.values().iterator();
		while (it.hasNext()) {
			File file = (File) it.next();
			ScriptEntry entry = (ScriptEntry) directories.get(file);
			if (entry != null)
				entry.setExpanded(true);
		}

		tool1Button.updatePicture();
		tool2Button.updatePicture();
	}
	
	void createFile() throws IOException {
		ScriptEntry entry = (ScriptEntry) scriptList.getActiveLeaf();
		// determine the list and directory where the new file will be 
		// created:
		HierarchyList list;
		
		if (entry == null) {
			list = scriptList;
		} else {
			if (entry.isDirectory) list = entry.getChildList();
			else {
				list = (HierarchyList) entry.getList();
				entry = (ScriptEntry) list.getParentEntry();
			}
		}
		File dir;
		// if we're at root, entry is null:
		if (entry == null) dir = ScriptographerEngine.getScriptDirectory();
		else dir = entry.file;
		
		if (dir != null) {
			// create a non existing filename:
			File file;
			for (int i = 1;;i++) {
				file = new File(dir, "Untitled " + i + ".js");
				if (!file.exists())
					break;
			}
			file = Dialog.fileSave("Create A New Script:", new String[] {
				"JavaScript Files (*.js)",
					"*.js",
				"All Files",
					"*.*"
			}, file);
			if (file != null && file.createNewFile()) {
				// add it to the list as well:
				new ScriptEntry(list, file);
			}
		}
	}
}
