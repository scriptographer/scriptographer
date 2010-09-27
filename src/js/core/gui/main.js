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
 */

// Tool

var tool = new Tool('Scriptographer Tool', getImage('tool.png')) {
	activeImage: getImage('tool-active.png'),
	tooltip: 'Execute a tool script to assign it with this tool button'
};

// Effect

var hasEffects = false; // Work in progress, turn off for now
var effect = hasEffects && new LiveEffect('Scriptographer', null, 'pre-effect');

var mainDialog = new FloatingDialog(
		'tabbed show-cycle resizing remember-placing', function() {

	// Script List

	var scriptList = new HierarchyListBox(this) {
		style: 'black-rect',
		size: [208, 20 * lineHeight],
		minimumSize: [208, 8 * lineHeight],
		entryTextRect: [0, 0, 2000, lineHeight],
		onTrackEntry: function(tracker, entry) {
			// Detect expansion of unpopulated folders and populate on the fly
			var expanded = entry.expanded;
			// This might change entry.expanded state
			entry.defaultTrack(tracker);
			if (!expanded && entry.expanded)
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
					app.launch(entry.file);
					// execute();
				}
			}
			// Return false to prevent calling of defaultTrack as we called
			// it already.
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
			text: file.alternateName || file.name,
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
			entry.childList.sealed = isRoot 
					? /^(Examples|Tutorials)$/.test(file.name) : list.sealed;
			// Remember myScriptsEntry
			if (isRoot && !myScriptsEntry && !entry.childList.sealed)
				myScriptsEntry = entry;
			entry.expanded = false;
			entry.populated = false;
			entry.populate = function() {
				if (!this.populated) {
					this.childList.directory = file;
					var files = getFiles(this.childList);
					for (var i = 0, l = files.length; i < l; i++)
						addFile(this.childList, files[i]);
					this.populated = true;
				}
			}
			entry.image = folderImage;
			directoryEntries[file] = entry;
		} else {
			entry.update = function() {
				var type = file.readAll().match(
						/(onMouse(?:Up|Down|Move|Drag))|(onCalculate)/);
				this.type = type && (type[1] && 'tool' || type[2] && 'effect');
				this.image = this.type == 'tool'
					? (currentToolFile == this.file
						? activeToolScriptImage
						: toolScriptImage)
					: hasEffects && this.type == 'effect'
						? effectImage
						: scriptImage;
			}
			entry.update();
			fileEntries[file] = entry;
		}
		return entry;
	}

	function getScriptDirectories() {
		return scriptRepositories.collect(function(repository) {
			if (repository.visible) {
				var dir = repository.sealed && repository.name == 'Examples'
						? examplesDirectory
						: new File(repository.path);
				if (dir.exists()) {
					dir.alternateName = repository.name;
					return dir;
				}
			}
		});
	}

	function getFiles(list) {
		var files;
		if (!list.directory) {
			// Return root directories
			files = getScriptDirectories();
		} else {
			files = list.directory.list(function(file) {
				return !/^__|^\.|^libraries$|^CVS$/.test(file.name) && 
					(/\.(?:js|rb|py)$/.test(file.name) || file.isDirectory());
			});
		}
		return files;
	}

	function refreshList(list, force) {
		if (!list) {
			list = scriptList;
			if (force)
				myScriptsEntry = null;
		}
		// Do only refresh populated lists
		if (list.parentEntry && !list.parentEntry.populated)
			return [];
		// Get new listing of the directory, then match with already inserted 
		// files. Create a lookup object for easily finding and tracking of
		// already inserted files.	
		var files = getFiles(list).each(function(file, i) {
			this[file.path] = {
				file: file,
				index: i,
			};
		}, {});
		// Now walk through all the already inserted files, find the ones that
		// need to be removed, and refresh already populated ones.
		var removed = list.each(function(entry) {
			if (force || !files[entry.file.path]) {
				// Don't remove right away since that would mess up the each
				// loop Instead. we collect them in the removed array, to be
				// removed in a seperate loop after.
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
					refreshList(entry.childList, false);
			}
		}, []);
		// Remove the deleted files.
		removed.each(function(entry) {
			entry.remove();
		});
		// Files now only contains new files that are not inserted yet.
		// Look through them and insert in the right paces.
		files.each(function(info) {
			addFile(list, info.file, info.index);
		});
	}

	function createScript() {
		var entry = scriptList.selectedLeafEntry;
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
			if (file) {
				if (file.exists())
					file.remove();
				file.create();
				// Use refreshList to make sure the new item appears in the
				// right place, and mark the newly added file as selected.
				// Make sure the list is populated first, so refreshList
				// actually processes it.
				list.parentEntry.populate();
				refreshList(list, false);
				list.each(function(other) {
					if (other.file == file) {
						if (entry && entry.isValid())
							entry.selected = false;
						other.selected = true;
						// The list was already populated and refreshed, just
						// expand it now to show the new entry
						list.parentEntry.expanded = true;
						throw Base.stop;
					}
				});
			}
		}
	}

	function getSelectedScriptEntry() {
		var entry = scriptList.selectedLeafEntry;
		return entry && entry.file ? entry : null;
	}

	function getEntryPath(entry) {
		var path = [];
		do {
			path.unshift(entry.text);
			entry = entry.parentEntry;
		} while (entry);
		return path.join('/');
	}

	function getEntryByPath(path) {
		var path = path.split('/');
		var list = scriptList, entry; 
		for (var i = 0, l = path.length; i < l && list; i++) {
			entry = list[path[i]];
			if (entry && entry.isDirectory) {
				entry.populate();
				list = entry.childList;
			} else {
				list = null;
			}
		}
		return i == l ? entry : null;
	}

	function compileScope(entry, handler) {
		var scr = ScriptographerEngine.compile(entry.file);
		if (scr) {
			var scope = entry.scope = scr.engine.createScope();
			if (handler instanceof ToolEventHandler) {
				scope.put('tool', handler, true);
			}
			// Don't call scr.execute directly, since we handle SG
			// specific things in ScriptographerEngine.execute:
			ScriptographerEngine.execute(scr, entry.file, scope);
			if (handler instanceof ToolEventHandler) {
				// Tell tool about the script it is associated with, so it
				// can get coordinate system information from it.
				handler.script = scope.get('script');
			}
			// Now copy over handlers from the scope and set them on the tool,
			// to allow them to be defined globally.
			// Support deprecated onOptions too, by converting it to
			// onEditOptions.
			var names = entry.type == 'tool'
				? ['onEditOptions', 'onOptions', 'onSelect', 'onDeselect',
					'onReselect', 'onMouseDown', 'onMouseUp', 'onMouseDrag',
					'onMouseMove']
				: ['onEditParameters', 'onCalculate', 'onGetInputType'];
			names.each(function(name) {
				var func = scope.getCallable(name);
				if (func)
					handler[name == 'onOptions' ? 'onEditOptions' : name] = func;
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

	function execute() {
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
			    if (hasEffects) {
    				executeEffect(entry);
    				break;
			    }
			default:
				ScriptographerEngine.execute(entry.file, null);
			}
		}
	}

	function stopAll() {
		ScriptographerEngine.stopAll(true, false);
		tool.reset();
	}

	function initAll() {
		ScriptographerEngine.setScriptDirectories(getScriptDirectories());
		refreshList(null, true);
	}

	// Effect

	var effectEntries = {};

	function followItem(item, speed, handler, first) {
		if (first && handler.distanceThreshold > 0) {
			speed = handler.distanceThreshold;
			handler.distanceThreshold = 0;
		}
		if (item instanceof Group || item instanceof CompoundPath
			|| item instanceof Layer) {
			item.children.each(function(child) {
				followItem(child, handler, speed);
			})
		} else if (item instanceof Path) {
			var curve = item.curves.first;
			if (curve) {
				handler.onHandleEvent('mouse-down', curve.point1);
				var length = item.length;
				if (true)
					speed = length / Math.round(length / speed);
				for (var pos = speed; pos < length; pos += speed)
					handler.onHandleEvent('mouse-drag', item.getPoint(pos));
				handler.onHandleEvent('mouse-up', item.curves.last.point2);
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
		// Return a copy of obj that only contains values that can be converted
		// to Json and stored in effect parameters easily.
		if (obj === null)
			return undefined;
		var type = Base.type(obj);
		// Support java.util.Map, as in Sg they are wrapped to behave like
		// native objects.
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
			// Unsupported types
			return undefined;
		default:
			return obj;
		}
	}

	function compileEffect(entry, parameters) {
		var path = getEntryPath(entry);
		parameters.path = path;
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
						// before executing onEditOptions, so the right values
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
							followItem(event.item, 10, toolHandler, true);
						}
					}
				}
			}
			effectEntries[path] = entry;
			entry.handler = handler;
			return true;
		}
	}

	// Pass on effect handlers.
	if (hasEffects)
	['onEditParameters', 'onCalculate', 'onGetInputType'].each(function(name) {
		effect[name] = function(event) {
			if (event.parameters.path) {
				var entry = effectEntries[event.parameters.path];
				if (!entry) {
					entry = getEntryByPath(event.parameters.path);
					if (entry) {
						compileEffect(entry, event.parameters);
					} else {
						// TODO: Alert?
						print('Cannot find effect script: ', event.parameters.path);
					}
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

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			mainDialog.visible = !mainDialog.visible;
		},
		onUpdate: function() {
			this.text = (mainDialog.visible ? 'Hide' : 'Show')
					+ ' Main Palette';
		}
	}.setCommand('`', MenuItem.MODIFIER_COMMAND);

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			this.text = (consoleDialog.visible ? 'Hide' : 'Show')
					+ ' Console Palette';
		}
	}.setCommand('`', MenuItem.MODIFIER_SHIFT | MenuItem.MODIFIER_COMMAND);

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
			ScriptographerEngine.reload.delay(0);
		}
	};

	// Popup Menu

	var menu = this.popupMenu;

	var executeEntry = new ListEntry(menu) {
		text: 'Execute Script',
		onSelect: execute
	};

	var stopAllEntry = new ListEntry(menu) {
		text: 'Stop Running Scripts',
		onSelect: stopAll
	};

	var createScriptEntry = new ListEntry(menu) {
		text: 'Create a New Script...',
		onSelect: createScript
	};

	var consoleEntry = new ListEntry(menu) {
		text: 'Show / Hide Console',
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			// TODO: Make onUpdate work in ListEntry
			this.text = (consoleDialog.visible ? 'Hide' : 'Show')
					+ ' Console Palette';
		}
	};

	var separator1Entry = new ListEntry(menu) {
		separator: true
	};

	var repositoriesEntry = new ListEntry(menu) {
		text: 'Manage Repositories...',
		onSelect: function() {
			var repositories = repositoriesDialog.choose(scriptRepositories);
			if (repositories) {
				scriptRepositories = repositories;
				script.preferences.repositories = repositories;
				stopAll();
				initAll();
			}
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
			app.launch('file://' + new File(scriptographer.pluginDirectory,
					'Reference/index.html'));
		}
	};

	var separator2Entry = new ListEntry(menu) {
		separator: true
	};

	var reloadEntry = new ListEntry(menu) {
		text: 'Reload',
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Event Handlers

	global.onActivate = function() {
		refreshList(null, false);
	}

	global.onKeyDown = function(event) {
		if (event.character == '`') {
			if (event.modifiers.command) {
				if (event.modifiers.shift) {
					consoleDialog.visible = !consoleDialog.visible;
					return true;
				} else {
					mainDialog.visible = !mainDialog.visible;
					return true;
				}
			} else if (!event.modifiers.shift && !event.modifiers.control) {
				tool.selected = true;
				return true;
			}
		}
	}

	// Buttons:
	var playButton = new ImageButton(this) {
		image: getImage('play.png'),
		size: buttonSize,
		toolTip: 'Execute Script',
		onClick: function() {
			execute();
		}
	};

	var stopButton = new ImageButton(this) {
		image: getImage('stop.png'),
		size: buttonSize,
		toolTip: 'Stop Running Scripts',
		onClick: stopAll
	};

	var effectButton = hasEffects && new ImageButton(this) {
		image: getImage('effect.png'),
		size: buttonSize,
		onClick: function() {
			executeEffect(getSelectedScriptEntry());
		}
	};

	var newScriptButton = new ImageButton(this) {
		image: getImage('script.png'),
		size: buttonSize,
		toolTip: 'Create a New Script...',
		onClick: createScript
	};

	var consoleButton = new ImageButton(this) {
		image: getImage('console.png'),
		size: buttonSize,
		toolTip: 'Show / Hide Console',
		onClick: function() {
			consoleDialog.visible = !consoleDialog.visible;
		}
	};

	initAll();

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
					newScriptButton,
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
