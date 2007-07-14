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
 * File created on 25.03.2005.
 *
 * $Id: interface.js 294 2007-04-15 19:20:29Z lehni $
 */

var mainDialog = new FloatingDialog(
		FloatingDialog.OPTION_TABBED |
		FloatingDialog.OPTION_SHOW_CYCLE |
		FloatingDialog.OPTION_RESIZING |
		Dialog.OPTION_REMEMBER_PLACING, function() {

	var lineHeight = 17;
	this.title = "Scriptographer";
	this.setIncrement(1, lineHeight);

	// Script List:
	scriptList = new HierarchyList(this) {
		style: List.STYLE_BLACK_RECT,
		size: [208, 20 * lineHeight],
		minimumSize: [208, 8 * lineHeight],
		entrySize: [2000, lineHeight],
		entryTextRect: [0, 0, 2000, lineHeight],
		// TODO: consider adding onDoubleClick, instead of this nasty workaround here!
		// Avoid onTrack as much as possible in scripts, and add what's needed behind
		// the scenes
		onTrackEntry: function(tracker, entry) {
			if (tracker.action == Tracker.ACTION_BUTTON_UP &&
					(tracker.modifiers & Tracker.MODIFIER_DOUBLE_CLICK) &&
					tracker.point.x > entry.expandArrowRect.right) {
				if (entry.directory) {
					entry.expanded = !entry.expanded;
					entry.list.invalidate();
				} else {
					app.launch(entry.file);
					// execute();
				}
			}
		}
	};

	// Filter for hiding files:
	var scriptFilter = new java.io.FilenameFilter() {
		accept: function(dir, name) {
			return name != "CVS" && !/^\./.test(name) &&
				(/\.(?:js|rb|py)$/.test(name)
				|| !/^__/.test(name) && new File(dir, name).directory);
		}
	};

	var scriptImage = getImage("script.png");
	var folderImage = getImage("folder.png");

	var directoryEntries = {};
	var fileEntries = {};

	function addFile(list, file, selected) {
		var entry = new HierarchyListEntry(list) {
			text: file.name,
			selected: selected && selected[file] || selected == true,
			// backgroundColor: Drawer.COLOR_BACKGROUND,
			file: file,
			directory: file.directory
		};
		if (entry.directory) {
			addFiles(entry.createChildList(), file, selected);
			entry.expanded = false;
			entry.image = folderImage;
			directoryEntries[file] = entry;
		} else {
			entry.image = scriptImage;
			fileEntries[file] = entry;
		}
		return entry;
	}

	function addFiles(list, dir, selected) {
		var files = dir.listFiles(scriptFilter);
		for (var i in files)
			addFile(list, files[i], selected);
	}

	function removeFiles() {
		scriptList.removeAll();
		directoryEntries = {};
		fileEntries = {};
	}

    function createFile() {
        var entry = scriptList.activeLeaf;
        var list;
        if (entry) {
            if (entry.directory) list = entry.childList;
            else {
                list = entry.list;
                entry = list.parentEntry;
            }
        } else list = scriptList;
        // If we're at root, entry is null:
        var dir = entry ? entry.file : scriptographer.scriptDirectory;
        if (dir) {
            // Find a non existing filename:
            var file;
            for (var i = 1;;i++) {
                file = new File(dir, "Untitled " + i + ".js");
                if (!file.exists())
                    break;
            }
            file = Dialog.fileSave("Create A New Script:", [
                "JavaScript Files (*.js)", "*.js",
                "All Files", "*.*"
			], file);
               // Add it to the list as well:
            if (file && file.createNewFile())
				addFile(list, file, true);
        }
    }

	function refreshFiles() {
		// Collect expanded items:
		var expandedDirs = {}, selected = {};
		for (file in directoryEntries) {
			var entry = directoryEntries[file];
			if (entry.directory && entry.expanded)
				expandedDirs[file] = true;
		}
		// Create a lookup table for all selected files, so they
		// can be selected again in addFiles
		var sel = scriptList.selectedEntries;
		for (var i = 0; i < sel.length; i++)
			selected[sel[i].file] = true;
		removeFiles();
		addFiles(scriptList, scriptographer.scriptDirectory, selected);
		// Now restore the expanded state:
		for (file in expandedDirs) {
			var entry = directoryEntries[file];
			if (entry) entry.expanded = true;
		}
		tool1Button.setCurrentImage();
		tool2Button.setCurrentImage();
	}

	function execute() {
		var entry = scriptList.activeLeaf;
		if (entry && entry.file)
			ScriptographerEngine.execute(entry.file, null);
	}

	var that = this;

	// Add the menus:
	// use a space in the beginning of the name so it appears on top of all entries :)
	var scriptographerItem = new MenuItem(MenuGroup.GROUP_TOOL_PALETTES, " Scriptographer");

	new MenuItem(scriptographerItem, "Main") {
		onSelect: function() {
			that.visible = true;
		}
	};

	new MenuItem(scriptographerItem, "Console") {
		onSelect: function() {
			consoleDialog.visible = true;
		}
	};

	new MenuItem(scriptographerItem, "Reload") {
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Add the popup menu
	var menu = this.popupMenu;

	var executeEntry = new ListEntry(menu) {
		text: "Execute Script",
		onSelect: function() {
			execute();
		}
	};

	var refreshEntry = new ListEntry(menu) {
		text: "Refresh List",
		onSelect: function() {
			refreshFiles();
		}
	};

	var consoleEntry = new ListEntry(menu) {
		text: "Show / Hide Console",
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		}
	};

	var scriptDirEntry = new ListEntry(menu) {
		text: "Set Script Directory...",
		onSelect: function() {
			if (ScriptographerEngine.chooseScriptDirectory())
				refreshFiles();
		}
	};

	var aboutEntry = new ListEntry(menu) {
		text: "About Scriptographer...",
		onSelect: function() {
			aboutDialog.doModal();
		}
	};

	var helpEntry = new ListEntry(menu) {
		text: "Help...",
		onSelect: function() {
			app.launch("file://" + new File(scriptographer.pluginDirectory, "doc/index.html"));
		}
	};

	var separatorEntry = new ListEntry(menu) {
		separator: true
	};

	var reloadEntry = new ListEntry(menu) {
		text: "Reload",
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Buttons:
	var playButton = new ImageButton(this) {
		onClick: function() {
			execute();
		},
		image: getImage("play.png"),
		size: buttonSize
	};

	var stopButton = new ImageButton(this) {
		onClick: function() {
			ScriptographerEngine.stopAll();
		},
		image: getImage("stop.png"),
		size: buttonSize
	};

	var refreshButton = new ImageButton(this) {
		onClick: function() {
			refreshFiles();
		},
		image: getImage("refresh.png"),
		size: buttonSize
	};

	var consoleButton = new ImageButton(this) {
		onClick: function() {
			consoleDialog.setVisible(!consoleDialog.isVisible());
		},
		image: getImage("console.png"),
		size: buttonSize
	};

	var newButton = new ImageButton(this) {
		onClick: function() {
			createFile();
		},
		image: getImage("script.png"),
		size: buttonSize
	};

	var tool1Button = new ImageButton(this) {
		image: getImage("tool1.png"),
		size: buttonSize,
		toolIndex: 0,
		entryImage: getImage("tool1script.png")
	};

	var tool2Button = new ImageButton(this) {
		image: getImage("tool2.png"),
		size: buttonSize,
		toolIndex: 1,
		entryImage: getImage("tool2script.png")
	};

	tool1Button.onClick = tool2Button.onClick = function() {
		var entry = scriptList.activeLeaf;
		if (entry && entry.file) {
			Tool.getTool(this.toolIndex).setScript(entry.file);
			if (entry.file != this.curFile) {
				this.setCurrentImage(scriptImage);
				entry.image = this.entryImage;
				this.curFile = entry.file;
			}
		}
	}

	tool1Button.setCurrentImage = tool2Button.setCurrentImage = function(image) {
		var curEntry = fileEntries[this.curFile];
		if (curEntry)
			curEntry.image = image ? image : this.entryImage;
	}

	// Layout:
	this.margins = [-1, 0, -1, -1];
	this.layout = new BorderLayout();
	this.content = {
		center: scriptList,
		south: new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1), [
			playButton,
			stopButton,
			new Spacer(4, 0),
			refreshButton,
			new Spacer(4, 0),
			newButton,
			consoleButton,
			new Spacer(4, 0),
			tool1Button,
			tool2Button
		])
	};

	addFiles(scriptList, scriptographer.scriptDirectory);
});
