/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
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
 * $Id$
 */

var mainDialog = new FloatingDialog('tabbed show-cycle resizing remember-placing', function() {

	// Script List:
	scriptList = new HierarchyList(this) {
		style: 'black-rect',
		size: [208, 20 * lineHeight],
		minimumSize: [208, 8 * lineHeight],
		entryTextRect: [0, 0, 2000, lineHeight],
		directory: scriptographer.scriptDirectory,
		// TODO: consider adding onDoubleClick and onExpand / Collapse, instead of this 
		// workaround here. Avoid onTrack as much as possible in scripts,
		// and add what's needed behind the scenes.
		onTrackEntry: function(tracker, entry) {
			// Detect expansion of unpopulated folders and populate on the fly
			var expanded = entry.expanded;
			entry.defaultTrack(tracker); // this might change entry.expanded state
			if (!entry.populated && !expanded && entry.expanded)
				entry.populate();
			// Detect doubleclicks on files and folders.
			if (tracker.action == Tracker.ACTION_BUTTON_UP &&
					(tracker.modifiers & Tracker.MODIFIER_DOUBLE_CLICK) &&
					tracker.point.x > entry.expandArrowRect.right) {
				if (entry.isDirectory) {
					entry.expanded = !entry.expanded;
					entry.list.invalidate();
				} else {
					// Edit the file through app.launch
					// TODO: On windows, this launches the scripting host by default
					app.launch(entry.file);
					// execute();
				}
			}
			// Return false to prevent calling of defaultTrack sine we called it already.
			return false;
		}
	};

	// Filter for hiding files:
	var scriptFilter = new java.io.FilenameFilter() {
		accept: function(dir, name) {
			return !/^__|^\.|^libraries$|^CVS$/.test(name) && 
				(/\.(?:js|rb|py)$/.test(name) || new File(dir, name).isDirectory());
		}
	};

	var scriptImage = getImage('script.png');
	var toolScriptImage = getImage('script-tool.png');
	var activeToolScriptImage = getImage('script-tool-active.png');
	var folderImage = getImage('folder.png');

	var directoryEntries = {};
	var fileEntries = {};
	var currentToolFile = null;

	function addFile(list, file, index) {
		// TODO: We need to convert back to com.scriptographer.sg.File from
		// java.io.File here, since listFiles is not using that class.
		// Decide what to do: Shall we use the boots File object instead?
		if (!(file instanceof File))
			file = new File(file);
		var entry = new HierarchyListEntry(list, Base.pick(index, -1)) {
			text: file.name,
			// backgroundColor: 'background',
			file: file,
			lastModified: file.lastModified(),
			isDirectory: file.isDirectory()
		};
		if (entry.isDirectory) {
			// Create empty child list to get the arrow button but do not
			// populate yet. This is done dynamically in onTrackEntry, when
			// the user opens the list
			entry.createChildList();
			entry.childList.directory = file;
			entry.expanded = false;
			entry.populated = false;
			entry.populate = function() {
				if (!this.populated) {
					this.childList.directory = file;
					addFiles(this.childList);
					this.populated = true;
				}
			}
			entry.image = folderImage;
			directoryEntries[file] = entry;
		} else {
			entry.update = function() {
				this.isTool = /onMouse(Up|Down|Move|Drag)/.test(file.readAll());
				this.image = this.isTool
					? (currentToolFile == this.file ? activeToolScriptImage : toolScriptImage)
					: scriptImage;
			}
			entry.update();
			fileEntries[file] = entry;
		}
		return entry;
	}

	function addFiles(list) {
		if (!list)
			list = scriptList;
		var files = list.directory.listFiles(scriptFilter);
		for (var i = 0; i < files.length; i++)
			addFile(list, files[i]);
	}

	function removeFiles() {
		scriptList.removeAll();
		directoryEntries = {};
		fileEntries = {};
	}

	function refreshFiles(list) {
		if (!list)
			list = scriptList;
		// Get new listing of the directory, then match with already inserted files.
		// Create a lookup object for easily finding and tracking of already inserted files.	
		var files = list.directory.listFiles(scriptFilter).each(function(file, i) {
			this[file.path] = {
				file: file,
				index: i,
			};
		}, {});
		// Now walk through all the already inserted files, find the ones that
		// need to be removed, and refresh already populated ones.
		var removed = list.each(function(entry) {
			if (!files[entry.file.path]) {
				// Don't remove right away since that would mess up the each loop.
				// Instead. we collect them in the removed array, to be removed
				// in a seperate loop after.
				this.push(entry);
			} else {
				delete files[entry.file.path];
				// See if the file was changed, and if so, update its icon since
				// it might be a tool now
				if (entry.lastModified != entry.file.lastModified()) {
					entry.lastModified = entry.file.lastModified(); 
					if (!entry.isDirectory)
						entry.update();
				}
				if (entry.populated)
					refreshFiles(entry.childList);
			}
		}, []);
		// Remove the deleted files.
		removed.each(function(entry) {
			entry.remove();
		});
		// Files now only contains new files that are not inserted yet.
		// Look through them and insert in the right paces.
		var added = [];
		files.each(function(info) {
			added.push(addFile(list, info.file, info.index));
		});
		return {
			removed: removed,
			added: added
		}
	}

    function createFile() {
        var entry = scriptList.activeLeaf;
        var list;
        if (entry) {
            if (entry.isDirectory) {
				list = entry.childList;
            } else {
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
                file = new File(dir, 'Untitled ' + i + '.js');
                if (!file.exists())
                    break;
            }
            file = Dialog.fileSave('Create A New Script:', [
                'JavaScript Files (*.js)', '*.js',
                'All Files', '*.*'
			], file);
               // Add it to the list as well:
            if (file && file.createNewFile()) {
				// Use refreshFiles to make sure the new item appears in the
				// right place, and mark the newly added file as selected.
				var added = refreshFiles(list).added;
				added.each(function(entry) {
					if (entry.file == file)
						entry.selected = true;
				});
			}
        }
    }

	function execute() {
		var entry = scriptList.activeLeaf;
		if (entry && entry.file) {
			if (entry.isTool) {
				tool.tooltip = entry.file.name;
				tool.image = tool.activeImage;
				tool.compileScript(entry.file);
				if (entry.file != currentToolFile) {
					var curEntry = fileEntries[currentToolFile];
					if (curEntry)
						curEntry.image = toolScriptImage;
					entry.image = activeToolScriptImage;
					currentToolFile = entry.file;
				}
			} else {
				ScriptographerEngine.execute(entry.file, null);
			}
		}
	}
	
	// Add the menus:
	var scriptographerGroup = new MenuGroup(MenuGroup.GROUP_TOOL_PALETTES, MenuGroup.OPTION_ADD_ABOVE | MenuGroup.OPTION_SEPARATOR_ABOVE);

	var scriptographerItem = new MenuItem(scriptographerGroup) {
		text: 'Scriptographer'
	};

// 	var separator = new MenuGroup(scriptographerGroup, MenuGroup.OPTION_ADD_ABOVE);

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			mainDialog.visible = !mainDialog.visible;
		},
		onUpdate: function() {
			this.text = (mainDialog.visible ? 'Hide' : 'Show') + ' Main Palette';
		}
	};

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			this.text = (consoleDialog.visible ? 'Hide' : 'Show') + ' Console Palette';
		}
	};

	new MenuItem(scriptographerItem) {
		text: 'About...',
		onSelect: function() {
			aboutDialog.doModal();
		}
	};

	new MenuItem(scriptographerItem) {
		separator: true
	};

	new MenuItem(scriptographerItem) {
		text: 'Reload',
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Add the popup menu
	var menu = this.popupMenu;

	var executeEntry = new ListEntry(menu) {
		text: 'Execute Script',
		onSelect: function() {
			execute();
		}
	};

	var consoleEntry = new ListEntry(menu) {
		text: 'Show / Hide Console',
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		}
	};

	var scriptDirEntry = new ListEntry(menu) {
		text: 'Set Script Directory...',
		onSelect: function() {
			if (chooseScriptDirectory())
				refreshFiles();
		}
	};

	var aboutEntry = new ListEntry(menu) {
		text: 'About Scriptographer...',
		onSelect: function() {
			aboutDialog.doModal();
		}
	};

	var referenceEntry = new ListEntry(menu) {
		text: 'Reference...',
		onSelect: function() {
			app.launch('file://' + new File(scriptographer.pluginDirectory, 'doc/index.html'));
		}
	};

	var separatorEntry = new ListEntry(menu) {
		separator: true
	};

	var reloadEntry = new ListEntry(menu) {
		text: 'Reload',
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Buttons:
	var playButton = new ImageButton(this) {
		onClick: function() {
			execute();
		},
		image: getImage('play.png'),
		size: buttonSize
	};

	var stopButton = new ImageButton(this) {
		image: getImage('stop.png'),
		size: buttonSize,
		onClick: function() {
			ScriptographerEngine.stopAll();
		}
	};

	var consoleButton = new ImageButton(this) {
		image: getImage('console.png'),
		size: buttonSize,
		onClick: function() {
			consoleDialog.setVisible(!consoleDialog.isVisible());
		}
	};

	var newButton = new ImageButton(this) {
		image: getImage('script.png'),
		size: buttonSize,
		onClick: function() {
			createFile();
		}
	};

	global.onActivate = function() {
		refreshFiles();
	}

	if (scriptographer.scriptDirectory)
		addFiles();

	return {
		title: 'Scriptographer',
		margin: [0, -1, -1, -1],
		content: {
			center: scriptList,
			south: new ItemGroup(this) {
				layout: [ 'left', -1, -1 ],
				content: [
					playButton,
					stopButton,
					new Spacer(4, 0),
					newButton,
					consoleButton,
				]
			}
		}
	};
});
