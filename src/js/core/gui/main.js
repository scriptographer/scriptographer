/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2010 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.org/ for updates and contact.
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

// Tool

var tool = new Tool('Scriptographer Tool', getImage('tool.png')) {
	activeImage: getImage('tool-active.png'),
	tooltip: 'Execute a tool script to assign it with this tool button'
};

// Effect

var effect = new LiveEffect('Scriptographer', null, 'pre-effect');

var mainDialog = new FloatingDialog('tabbed show-cycle resizing remember-placing', function() {

	// Script List

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

	var scriptImage = getImage('script.png');
	var effectImage = getImage('effect.png');
	var toolScriptImage = getImage('script-tool.png');
	var activeToolScriptImage = getImage('script-tool-active.png');
	var folderImage = getImage('folder.png');

	var directoryEntries = {};
	var fileEntries = {};
	var currentToolFile = null;
	var myScriptsEntry = null;

	function addFile(list, file, index) {
		var entry = new HierarchyListEntry(list, Base.pick(index, -1)) {
			text: file.name,
			// backgroundColor: 'background',
			file: file,
			lastModified: file.lastModified,
			isDirectory: file.isDirectory()
		};
		var isRoot = list == scriptList;
		if (entry.isDirectory) {
			// Create empty child list to get the arrow button but do not
			// populate yet. This is done dynamically in onTrackEntry, when
			// the user opens the list
			entry.createChildList();
			entry.childList.directory = file;
			// Seal Examples and Tutorials and pass on sealed setting
			entry.childList.sealed = isRoot ? /^(Examples|Tutorials)$/.test(file.name) : list.sealed;
			if (isRoot && file.name == 'My Scripts')
				myScriptsEntry = entry;
			entry.expanded = false;
			entry.populated = false;
			entry.populate = function() {
				if (!this.populated) {
					this.childList.directory = file;
					var files = getFiles(this.childList);
					for (var i = 0; i < files.length; i++)
						addFile(this.childList, files[i]);
					this.populated = true;
				}
			}
			entry.image = folderImage;
			directoryEntries[file] = entry;
		} else {
			entry.update = function() {
				var type = file.readAll().match(/(onMouse(?:Up|Down|Move|Drag))|(onCalculate)/);
				this.type = type && (type[1] && 'tool' || type[2] && 'effect');
				this.image = this.type == 'tool'
					? (currentToolFile == this.file
						? activeToolScriptImage
						: toolScriptImage)
					: this.type == 'effect'
						? effectImage
						: scriptImage;
			}
			entry.update();
			fileEntries[file] = entry;
		}
		return entry;
	}

	function getFiles(list) {
		if (!list.directory)
			return [];
		var files = list.directory.list(function(file) {
			return !/^__|^\.|^libraries$|^CVS$/.test(file.name) && 
				(/\.(?:js|rb|py)$/.test(file.name) || file.isDirectory());
		});
		if (list == scriptList) {
			var order = {
				'Examples': 1,
				'Tutorials': 2,
				'My Scripts': 3
			};
			files.sort(function(file1, file2) {
				var pos1 = order[file1.name] || 4;
				var pos2 = order[file2.name] || 4;
				return pos1 < pos2
					? -1 
					: pos1 > pos2
						? 1
						: 0;
			});
		}
		return files;
	}

	function refreshList(list) {
		if (!list) {
			scriptList.directory = scriptographer.scriptDirectory;
			list = scriptList;
		}
		// Do only refresh populated lists
		if (list.parentEntry && !list.parentEntry.populated)
			return null;
		// Get new listing of the directory, then match with already inserted files.
		// Create a lookup object for easily finding and tracking of already inserted files.	
		var files = getFiles(list).each(function(file, i) {
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
				var lastModified = entry.file.lastModified;
				if (entry.lastModified != lastModified) {
					entry.lastModified = lastModified; 
					if (!entry.isDirectory)
						entry.update();
				}
				if (entry.populated)
					refreshList(entry.childList);
			}
		}, []);
		// Remove the deleted files.
		removed.each(function(entry) {
			entry.remove();
		});
		// Files now only contains new files that are not inserted yet.
		// Look through them and insert in the right paces.
		var added = files.each(function(info) {
			this.push(addFile(list, info.file, info.index));
		}, []);
		return {
			removed: removed,
			added: added
		}
	}

	function createFile() {
		var entry = scriptList.activeLeaf;
		var list = entry && (entry.isDirectory ? entry.childList : entry.list);
		if (!list || list.sealed)
			list = myScriptsEntry ? myScriptsEntry.childList : scriptList;
		var dir = list.directory;
		if (dir) {
			// Find a non existing filename:
			var file;
			for (var i = 1; ; i++) {
				file = new File(dir, 'Untitled ' + i + '.js');
				if (!file.exists())
					break;
			}
			file = Dialog.fileSave('Create a New Script:', [
				'JavaScript Files (*.js)', '*.js',
				'All Files', '*.*'
			], file);
			   // Add it to the list as well:
			if (file && file.createNewFile()) {
				// Use refreshList to make sure the new item appears in the
				// right place, and mark the newly added file as selected.
				var res = refreshList(list);
				if (res) {
					res.added.each(function(newEntry) {
						if (newEntry.file == file) {
							if (entry)
								entry.selected = false;
							newEntry.selected = true;
						}
					});
				}
			}
		}
	}

	function getSelectedScriptEntry() {
		var entry = scriptList.activeLeaf;
		return entry && entry.file ? entry : null;
	}

	function compileScope(entry, handler) {
		var scr = ScriptographerEngine.compile(entry.file);
		if (scr) {
			var scope = entry.scope = scr.engine.createScope();
			if (handler instanceof ToolEventHandler)
				scope.put('tool', handler, true);
			// Don't call scr.execute directly, since we handle SG
			// specific things in ScriptographerEngine.execute:
			ScriptographerEngine.execute(scr, entry.file, scope);
			// Now copy over handlers from the scope and set them on the tool,
			// to allow them to be defined globally.
			var names = entry.type == 'tool'
				? ['onOptions', 'onSelect', 'onDeselect', 'onReselect',
					'onMouseDown', 'onMouseUp', 'onMouseDrag', 'onMouseMove']
				: ['onEditParameters', 'onCalculate', 'onGetInputType'];
			names.each(function(name) {
				var func = scope.getCallable(name);
				if (func)
					handler[name] = func;
			});
			return scope;
		}
	}

	function executeEffect(entry) {
		if (entry && /^(tool|effect)$/.test(entry.type)) {
			// This works even for multiple selections, as the path style
			// apparently is applied to all of the selected items. Fine
			// with us... But not so clean...
			var item = document.selectedItems.first;
			if (item) {
				var parameters = new LiveEffectParameters();
				if (compileEffect(entry, parameters)) {
					item.addEffect(effect, parameters);
					item.editEffect(effect, parameters);
				}
			}
			else
				Dialog.alert('In order to assign Scriptographer Effects\n'
					+ 'to items, please select some items\n'
					+ 'before executing the script.');
		}
	}

	function execute(asEffect) {
		var entry = getSelectedScriptEntry();
		if (entry) {
			switch (entry.type) {
			case 'tool':
				// Manually call onStop in tool scopes before they get overridden.
				if (entry.scope) {
					var onStop = entry.scope.getCallable('onStop');
					if (onStop)
						onStop.call(tool);
				}
				tool.title = tool.tooltip = entry.file.name;
				tool.image = tool.activeImage;
				// Reset settings
				tool.initialize();
				var scope = compileScope(entry, tool);
				if (scope) {
					// Call onInit on the tool scope, for backward compatibility.
					var onInit = scope.getCallable('onInit');
					if (onInit)
						onInit.call(tool);
				}
				if (entry.file != currentToolFile) {
					var curEntry = fileEntries[currentToolFile];
					if (curEntry && curEntry.isValid())
						curEntry.image = toolScriptImage;
					entry.image = activeToolScriptImage;
					currentToolFile = entry.file;
				}
				break;
			case 'effect':
				executeEffect(entry);
				break;
			default:
				ScriptographerEngine.execute(entry.file, null);
			}
		}
	}

	// Script Directory Stuff

	function chooseScriptDirectory(dir) {
		dir = Dialog.chooseDirectory(
			'Please choose the Scriptographer script directory',
			dir || scriptographer.scriptDirectory || scriptographer.pluginDirectory);
		if (dir && dir.isDirectory()) {
			script.preferences.scriptDirectory = dir.path;
			setScriptDirectory(dir);
			return true;
		}
	}

	function setScriptDirectory(dir) {
		// Tell Scriptographer about where to look for scripts.
		ScriptographerEngine.scriptDirectory = dir;
		// Load librarires:
		// TODO: Is this still used?
		ScriptographerEngine.loadLibraries(new File(dir, 'Libraries'));
		refreshList();
	}

	// Read the script directory first, or ask for it if its not defined:
	var dir = script.preferences.scriptDirectory;
	// If no script directory is defined, try the default place for Scripts:
	// The subdirectory 'scripts' in the plugin directory:
	dir = dir
		? new File(dir)
		: new File(scriptographer.pluginDirectory, 'Scripts');
	if (!dir.exists() || !dir.isDirectory()) {
		if (!chooseScriptDirectory(dir))
			Dialog.alert('Could not find Scriptographer script directory.');
	} else {
		setScriptDirectory(dir);
	}

	// Effect

	var effectEntries = {};

	function followItem(item, speed, handler, scope) {
		if (item instanceof Group || item instanceof CompoundPath
			|| item instanceof Layer) {
			item.children.each(function(child) {
				followItem(child, handler, speed);
			})
		} else if (item instanceof Path) {
			var curve = item.curves.first;
			if (curve) {
				handler.onHandleEvent('mouse-down', curve.point1);
				for (var pos = speed, length = item.length; pos < length; pos += speed)
					handler.onHandleEvent('mouse-drag', item.getPoint(pos));
				handler.onHandleEvent('mouse-up', item.curves.last.point2);
				/*
				var path = item.clone();
				if  (path.closed)
					path.segments.push(path.segments.first);
				path.curvesToPoints(speed);
				for (var i = 0, l = path.segments.length; i < l; i++)
					handler.onHandleEvent('mouse-drag', path.segments[i].point);
				path.remove();
				*/
			}
		}
	}

	function saveScope(scope, tool, parameters) {
		// Create a Json version of all variables in the scope.
		parameters.scope = Json.encode(scope.getKeys().each(function(key) {
			if (key != 'global') {
				var value = scope.get(key);
				var value = filterScope(value);
				if (value !== undefined)
					this[key] = value;
			}
		}, {}));
		// We also need to backup and restore distanceThreshold
		parameters.distanceThreshold = tool.distanceThreshold;
	}

	function restoreScope(scope, tool, parameters) {
		var values = Json.decode(parameters.scope);
		for (var key in values)
			scope.put(key, values[key]);
		tool.distanceThreshold = parameters.distanceThreshold;
	}

	function filterScope(obj) {
		if (obj === null)
			return undefined;
		var type = Base.type(obj);
		if (type == 'java' && obj instanceof java.util.Map)
			type = 'object';
		switch (type) {
		case 'object':
			var res = {};
			for (var key in obj) {
				var value = filterScope(obj[key]);
				if (value !== undefined)
					res[key] = value;
			}
			return res;
		case 'array':
			var res = [];
			for (var i = 0, l = obj.length; i < l; i++)
				res[i] = filterScope(key[i]);
			return res;
		case 'java':
		case 'function':
		case 'regexp':
			return undefined;
		default:
			return obj;
		}
	}

	function compileEffect(entry, parameters) {
		var path = entry.file.path;
		effectEntries[path] = entry;
		parameters.file = path;
		var isTool = entry.type == 'tool';
		// Create a ToolEventHandler that handles all the complicated ToolEvent
		// stuff for us, to replicate completely the behavior of tools.
		var handler = isTool ? new ToolEventHandler() : {};
		// The same scope is shared among all instances of this Effect.
		// So we need to save and restore scope variables into the event.parameters
		// object. The values are saved as Json, as duplicating effects otherwise
		// does not create deep copies of parameters, which breaks storing values
		// in JS objects which would get converted to shared Dictionaries otherwise.
		var scope = compileScope(entry, handler);
		if (scope) {
			scope.put('effect', effect, true);
			if (isTool) {
				var toolHandler = handler;
				handler = {
					onEditParameters: function(event) {
						// Restore previously saved values into scope,
						// before executing onOptions, so the right values
						// are used.
						if (event.parameters.scope)
							restoreScope(scope, toolHandler, event.parameters);
						try {
							toolHandler.onHandleEvent('edit-options', null);
						} finally {
							// Save the new values from the scope.
							saveScope(scope, toolHandler, event.parameters);
						}
					},

					onCalculate: function(event) {
						if (event.parameters.scope) {
							restoreScope(scope, toolHandler, event.parameters);
							var speed = Math.max(10, toolHandler.distanceThreshold);
							// Erase distanceThreshold, as we're stepping exactly
							// by that amount anyway, and there is always a bit
							// of imprecision involved with length calculations.
							toolHandler.distanceThreshold = 0;
//							var t = new Date();
							followItem(event.item, speed, toolHandler, this);
//							print(new Date() - t);
						}
					}
				}
			}
			entry.handler = handler;
			return true;
		}
	}

	// Pass on effect handlers.
	['onEditParameters', 'onCalculate', 'onGetInputType'].each(function(name) {
		effect[name] = function(event) {
			if (event.parameters.file) {
				var entry = effectEntries[event.parameters.file];
				if (!entry) {
					// TODO: Support finding unindexed effect files from the path inside
					// scriptList, so effects work after reloading too!
				}
				var func = entry && entry.handler && entry.handler[name];
				if (func)
					return func.call(entry.scope, event);
			}
		}
	});

	// Menus

	var scriptographerGroup = new MenuGroup(MenuGroup.GROUP_TOOL_PALETTES,
			MenuGroup.OPTION_ADD_ABOVE | MenuGroup.OPTION_SEPARATOR_ABOVE);

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
	}.setCommand('M', MenuItem.MODIFIER_SHIFT | MenuItem.MODIFIER_COMMAND);

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			this.text = (consoleDialog.visible ? 'Hide' : 'Show') + ' Console Palette';
		}
	}.setCommand('C', MenuItem.MODIFIER_SHIFT | MenuItem.MODIFIER_COMMAND);

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

	// Popup Menu

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
		},
		onUpdate: function() {
			// TODO: Make onUpdate work in ListEntry
			this.text = (consoleDialog.visible ? 'Hide' : 'Show') + ' Console Palette';
		}
	};

	var scriptDirEntry = new ListEntry(menu) {
		text: 'Set Script Directory...',
		onSelect: chooseScriptDirectory
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
			app.launch('file://' + new File(scriptographer.pluginDirectory, 'Reference/index.html'));
		}
	};

	var separatorEntry = new ListEntry(menu) {
		separator: true
	};

	var reloadEntry = new ListEntry(menu) {
		text: 'Reload',
		onSelect: function() {
			ScriptographerEngine.reload.delay(1);
		}
	};

	// Event Handlers

	global.onActivate = function() {
		refreshList();
	}

	global.onKeyDown = function(event) {
		if (event.character == '`' && !event.modifiers.command && !event.modifiers.shift) {
			tool.selected = true;
			return true;
		}
	}

	// Buttons:
	var playButton = new ImageButton(this) {
		image: getImage('play.png'),
		size: buttonSize,
		onClick: function() {
			execute();
		}
	};

	var stopButton = new ImageButton(this) {
		image: getImage('stop.png'),
		size: buttonSize,
		onClick: function() {
			ScriptographerEngine.stopAll();
		}
	};

	var effectButton = new ImageButton(this) {
		image: getImage('effect.png'),
		size: buttonSize,
		onClick: function() {
			executeEffect(getSelectedScriptEntry());
		}
	};

	var consoleButton = new ImageButton(this) {
		image: getImage('console.png'),
		size: buttonSize,
		onClick: function() {
			consoleDialog.visible = !consoleDialog.visible;
		}
	};

	var newButton = new ImageButton(this) {
		image: getImage('script.png'),
		size: buttonSize,
		onClick: function() {
			createFile();
		}
	};

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
					effectButton,
					new Spacer(4, 0),
					newButton,
					consoleButton,
				]
			}
		}
	};
});

// Force mainDialog to show if this is the first run of Scriptographer
if (firstRun) {
	(function() {
		consoleDialog.visible = false;
		mainDialog.visible = true;
	}).delay(1);
}
