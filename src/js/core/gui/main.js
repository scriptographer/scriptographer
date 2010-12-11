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

var hasEffects = true; // Work in progress, turn off for now
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
				entry.data.populate();
			// Detect doubleclicks on files and folders.
			if (tracker.action == Tracker.ACTION_BUTTON_UP
					 && tracker.point.x > entry.expandArrowRect.right) {
				if (tracker.modifiers & Tracker.MODIFIER_DOUBLE_CLICK) {
					if (entry.data.isDirectory) {
						entry.expanded = !entry.expanded;
						entry.list.invalidate();
					} else {
						chooseEntry(entry);
					}
				}
				updateItems();
			}
			// Return false to prevent calling of defaultTrack as we called
			// it already.
			return false;
		}
	};

	this.onTrack = function(tracker) {
		if (tracker.action == Tracker.ACTION_BUTTON_UP)
			updateItems();
	}

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
			data: {
				file: file,
				lastModified: file.lastModified,
				isDirectory: file.isDirectory()
			}
		};
		var isRoot = list == scriptList;
		if (entry.data.isDirectory) {
			// Create empty child list to get the arrow button but do not
			// populate yet. This is done dynamically in onTrackEntry, when
			// the user opens the list
			entry.createChildList();
			entry.childList.data.directory = file;
			// Seal Examples and Tutorials and pass on sealed setting
			entry.childList.data.sealed = isRoot
					? /^(Examples|Tutorials)$/.test(file.name)
					: list.data.sealed;
			// Remember myScriptsEntry
			if (isRoot && !myScriptsEntry && !entry.childList.data.sealed)
				myScriptsEntry = entry;
			entry.expanded = false;
			entry.data.populated = false;
			entry.data.populate = function() {
				if (!entry.data.populated) {
					entry.childList.data.directory = file;
					var files = getFiles(entry.childList);
					for (var i = 0, l = files.length; i < l; i++)
						addFile(entry.childList, files[i]);
					entry.data.populated = true;
				}
			}
			entry.image = folderImage;
			directoryEntries[file] = entry;
		} else {
			entry.data.update = function() {
				var type = file.readAll().match(
						/(onMouse(?:Up|Down|Move|Drag))|(onCalculate)/);
				entry.data.type = type && (type[1] && 'tool'
						|| type[2] && 'effect');
				entry.image = entry.data.type == 'tool'
						? (currentToolFile == entry.data.file
								? activeToolScriptImage
								: toolScriptImage)
						: hasEffects && entry.data.type == 'effect'
								? effectImage
								: scriptImage;
			}
			entry.data.update();
			fileEntries[file] = entry;
		}
		return entry;
	}

	function getScriptDirectories() {
		return scriptRepositories.collect(function(repository) {
			if (repository.visible) {
				var dir = repository.sealed
						&& /^(Examples|Tutorials)$/.test(repository.name)
								? new File(scriptsDirectory, repository.name)
								: new File(repository.path);
				// Only show repositories that actually exist:
				if (dir.exists()) {
					dir.alternateName = repository.name;
					return dir;
				}
			}
		});
	}

	function getFiles(list) {
		var files;
		if (!list.data.directory) {
			// Return root directories
			files = getScriptDirectories();
		} else {
			files = list.data.directory.list(function(file) {
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
		if (list.parentEntry && !list.parentEntry.data.populated)
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
			if (force || !files[entry.data.file.path]) {
				// Don't remove right away since that would mess up the each
				// loop Instead. we collect them in the removed array, to be
				// removed in a seperate loop after.
				this.push(entry);
			} else {
				delete files[entry.data.file.path];
				// See if the file was changed, and if so, update its icon since
				// it might be a tool now
				var lastModified = entry.data.file.lastModified;
				if (entry.data.lastModified != lastModified) {
					entry.data.lastModified = lastModified; 
					if (!entry.data.isDirectory)
						entry.data.update();
				}
				if (entry.data.populated)
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

	function editScript() {
		chooseEntry(getSelectedScriptEntry());
	}

	function pickNonExistingFile(dir, extension) {
		// Find a non existing filename:
		for (var i = 1; ; i++) {
			var file = new File(dir, 'Untitled ' + i + (extension || ''));
			if (!file.exists())
				return file;
		}
	}

	function updateCreatedFile(list, file) {
		// Use refreshList to make sure the new item appears in the
		// right place, and mark the newly added file as selected.
		// Make sure the list is populated first, so refreshList
		// actually processes it.
		list.parentEntry.data.populate();
		refreshList(list, false);
		var current = scriptList.selectedLeafEntry;
		list.each(function(other) {
			if (other.data.file == file) {
				if (current && current.isValid()) {
					current.selected = false;
					current = null;
				}
				other.selected = true;
				// The list was already populated and refreshed, just
				// expand it now to show the new entry
				list.parentEntry.expanded = true;
				throw Base.stop;
			}
		});
		// Update buttons and menu entries
		updateItems();
	}

	function createScript() {
		var list = getSelectedDirectoryList();
		if (!list)
			list = myScriptsEntry.childList;
		var dir = list.data.directory;
		if (dir) {
			var file = pickNonExistingFile(dir, '.js');
			file = Dialog.fileSave('Create a New Script', [
				'JavaScript Files (*.js)', '*.js',
				'All Files', '*.*'
			], file);
			   // Add it to the list as well:
			if (file) {
				if (file.exists())
					file.remove();
				file.createNewFile();
				updateCreatedFile(list, file);
			}
		}
	}

	function createDirectory() {
		var list = getSelectedDirectoryList();
		var dir = list && list.data.directory;
		if (dir) {
			var file = pickNonExistingFile(dir);
			var values = Dialog.prompt('Create New Folder', {
				text: { type: 'text', value: 'Enter a new folder name:' },
				name: { type: 'string', value: file.name, width: 300 }
			});
			if (values) {
				file = new File(dir, values.name);
				if (file.exists()) {
					Dialog.alert("A folder named '" + values.name
							+ "' already exists!");
				} else {
					file.makeDirectory();
					updateCreatedFile(list, file);
				}
			}
		}
	}

	function getSelectedScriptEntry() {
		var entry = scriptList.selectedLeafEntry;
		return entry && entry.data.file ? entry : null;
	}

	function getSelectedDirectoryList() {
		var entry = scriptList.selectedLeafEntry;
		var list = entry && (entry.data.isDirectory
				? entry.childList : entry.list);
		if (list && !list.data.sealed)
			return list;
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
			if (entry && entry.data.isDirectory) {
				entry.data.populate();
				list = entry.childList;
			} else {
				list = null;
			}
		}
		return i == l ? entry : null;
	}

	function compileScope(entry, handler, populate) {
		var scr = ScriptographerEngine.compile(entry.data.file);
		if (scr) {
			var scope = entry.data.scope = scr.engine.createScope();
			if (populate) {
				for (var i in populate)
					scope.put(i, populate[i]);
			}
			if (handler instanceof ToolHandler) {
				scope.put('tool', handler, true);
			}
			// Don't call scr.execute directly, since we handle SG
			// specific things in ScriptographerEngine.execute:
			ScriptographerEngine.execute(scr, entry.data.file, scope);
			if (ScriptographerEngine.lastError)
				return null;
			adjustOrigin(scope);
			if (handler instanceof ToolHandler) {
				// Tell tool about the script it is associated with, so it
				// can get coordinate system information from it.
				handler.script = scope.get('script');
			}
			// Now copy over handlers from the scope and set them on the tool,
			// to allow them to be defined globally.
			// Support deprecated onOptions too, by converting it to
			// onEditOptions.
			var names = entry.data.type == 'tool'
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

	function isEffect(entry) {
		return entry ? /^(tool|effect)$/.test(entry.data.type) : false;
	}

	function executeEffect(entry) {
		if (!entry)
			entry = getSelectedScriptEntry();
		if (isEffect(entry)) {
			// This works even for multiple selections, as the path style
			// apparently is applied to all of the selected items. Fine
			// with us... But not so clean...
			var item = document && document.selectedItems.first;
			if (item) {
				var parameters = new LiveEffectParameters();
				if (compileEffect(entry, parameters)) {
					item.addEffect(effect, parameters);
					if (!item.editEffect(effect, parameters))
						item.removeEffect(effect, parameters);
				}
			} else {
				Dialog.alert('In order to assign Scriptographer Effects\n'
					+ 'to items, please select some items\n'
					+ 'before executing the script.');
			}
		}
	}

	function chooseEntry(entry) {
		// Edit the file through illustrator.launch
		illustrator.launch(entry.data.file);
		// execute();
	}

	function setToolEntry(entry) {
		var curEntry = fileEntries[currentToolFile];
		if (curEntry && curEntry.isValid())
			curEntry.image = toolScriptImage;
		if (entry) {
			entry.image = activeToolScriptImage;
			currentToolFile = entry.data.file;
		} else {
			currentToolFile = null;
		}
	}

	function execute() {
		var entry = getSelectedScriptEntry();
		if (entry) {
			switch (entry.data.type) {
			case 'tool':
				// Manually call onStop in tool scopes before they get overridden.
				if (entry.data.scope) {
					var onStop = entry.data.scope.getCallable('onStop');
					if (onStop)
						onStop.call(tool);
				}
				tool.title = tool.tooltip = entry.data.file.name;
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
				if (entry.data.file != currentToolFile)
					setToolEntry(entry);
				break;
			case 'effect':
			    if (hasEffects) {
    				executeEffect(entry);
    				break;
			    }
			default:
				var scr = ScriptographerEngine.compile(entry.data.file);
				if (scr) {
					var scope = scr.engine.createScope();
					// Don't call scr.execute directly, since we handle SG
					// specific things in ScriptographerEngine.execute:
					ScriptographerEngine.execute(scr, entry.data.file, scope);
					adjustOrigin(scope);
				}
			}
			/*
			// We want to keep keyboard focus on the main dialog even if the
			// script opens palettes.
			(function() {
				mainDialog.active = true;
			}).delay(1);
			*/
		}
	}

	function adjustOrigin(scope) {
		// Update ruler origin on CS4 and below to make sure units are displayed
		// correctly when in top-down coordinates.
		if (document && illustrator.version < 15
					&& script.preferences.adjustOrigin) {
			var scr = scope.get('script');
			var origin = document.rulerOrigin
					+ (scr.coordinateSystem == 'top-down'
							? document.bounds.topLeft
							: document.bounds.bottomLeft);
			if (document.rulerOrigin != origin)
				document.rulerOrigin = origin;
		}
	}

	function stopAll() {
		ScriptographerEngine.stopAll(true, false);
		tool.reset();
		setToolEntry(null);
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
		for (var key in values) {
			// Merge the stored values into the current scope values, in order
			// to preserve things like handler functions, which are filtered out
			// by filterScope().
			// TODO: See if there is a better way to handle this? If a script
			// replaces handler functions dynamically this would not work!
			scope.put(key, Hash.merge(scope.get(key), values[key]));
		}
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
			for (var i = 0, l = obj.length; i < l; i++) {
				res[i] = filterScope(obj[i]);
			}
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
		var isTool = entry.data.type == 'tool';
		// Create a ToolHandler that handles all the complicated ToolEvent
		// stuff for us, to replicate completely the behavior of tools.
		var handler = isTool ? new ToolHandler() : {};
		// The same scope is shared among all instances of this Effect.
		// So we need to save and restore scope variables into the
		// event.parameters object. The values are saved as Json, as duplicating
		// effects otherwise does not create deep copies of parameters, which
		// breaks storing values in JS objects which would get converted to
		// shared Dictionaries otherwise.
		var palette;
		var scope = compileScope(entry, handler, {
			// Override the Palette constructor with one that just stores the
			// palette definition in local variables, so it can be used to
			// display a Dialog.prompt() instead in onEditParameters below.
			Palette: function(title, components, values) {
				palette = this;
				this.title = title;
				this.components = components;
				this.values = values;
			}
		});
		if (scope) {
			if (palette) {
				// Since we need to modify the original values object in the
				// scope, scan through the scope for its value and keep a
				// reference to its name, so we can always get the current
				// values from any effect's scope. Also scan for components,
				// as these might be accessed again as well.
				scope.getKeys().each(function(key) {
					var value = scope.get(key);
					if (value == palette.values) {
						palette.valuesName = key;
					} else if (value == palette.components) {
						palette.componentsName = key;
					}
				});
			}
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
							if (palette) {
								// Now fetch the values object under its
								// determined name:
								if (palette.valuesName)
									palette.values =
											scope.get(palette.valuesName);
								if (palette.componentsName)
									palette.components =
											scope.get(palette.componentsName);
								Dialog.prompt(palette.title, palette.components,
											palette.values);
								// No need to place back the updated values
								// object in the scope, as Dialog.prompt
								// modifies it directly.
							} else {
								toolHandler.onHandleEvent('edit-options', null);
							}
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
			entry.data.handler = handler;
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
						print('Cannot find effect script: ',
								event.parameters.path);
					}
				}
				var func = entry && entry.data.handler
						&& entry.data.handler[name];
				if (func)
					return func.call(entry.data.scope, event);
			}
		}
	});

	// Event Handlers

	global.onActivate = function() {
		refreshList(null, false);
	}

	global.onKeyDown = function(event) {
		if (event.character == '`') {
			if (event.modifiers.command && event.modifiers.option) {
				if (event.modifiers.shift) {
					consoleDialog.visible = !consoleDialog.visible;
				} else {
					mainDialog.visible = !mainDialog.visible;
				}
				return true;
			} else if (!event.modifiers.command && !event.modifiers.option
						&& !event.modifiers.shift && !event.modifiers.control) {
				tool.selected = true;
				return true;
			}
		} else if (event.modifiers.command) {
			switch (event.keyCode) {
			case 'period':
				// Stop
				stopAll();
				return true;
			case 'e':
				// Execute
				execute();
				return true;
			case 'n':
				if (event.modifiers.option && event.modifiers.shift) {
					// New Script
					// Make sure the dialog is up for keyboard navigation after
					// bringing up the dialog.
					mainDialog.active = true;
					createScript();
					return true;
				}
			}
		} else if (mainDialog.active) {
			// Handle keyboard navigation in scriptList
			var entry = getSelectedScriptEntry();
			if (entry) {
				if (/up|down/.test(event.keyCode)) {
					var down = event.keyCode == 'down';
					// Step into open folders when moving down
					if (down && entry.data.isDirectory && entry.expanded
							&& !entry.childList.isEmpty()) {
						next = entry.childList.first;
					} else {
						var index = entry.index + (down ? 1 : -1);
						var next = entry.list[index];
						if (next) {
							// Step into open foldres when moving up
							if (!down && next.data.isDirectory && next.expanded
									&& !next.childList.isEmpty()) {
								next = next.childList.last;
							}
						} else {
							// Move a level up as there is no more on this level
							next = entry.list.parentEntry;
							if (next && down) {
								// Select the next entry on the parent level
								// when moving down
								next = next.list[next.index + 1];
							}
						}
					}
					if (next) {
						entry.selected = false;
						next.selected = true;
						updateItems();
					}
					return true;
				} else if (/left|right/.test(event.keyCode)) {
					var left = event.keyCode == 'left';
					// Just like on OSX Finder, if we're stepping left and are
					// not on an open directory,  step one level up and do not
					// collapse anything yet.
					if (left && (!entry.data.isDirectory || !entry.expanded)) {
						var parent = entry.list.parentEntry;
						if (parent) {
							parent.selected = true;
							entry.selected = false;
							return true;
						}
					} else if (entry.data.isDirectory) {
						entry.expanded = !left;
						if (entry.expanded)
							entry.data.populate();
						entry.list.invalidate();
						return true;
					}
				} else if (/return|enter/.test(event.keyCode)) {
					// Choose
					chooseEntry(entry);
					return true;
				}
			}
		}
	}

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
	}.setCommand('`', MenuItem.MODIFIER_OPTION | MenuItem.MODIFIER_COMMAND);

	new MenuItem(scriptographerItem) {
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			this.text = (consoleDialog.visible ? 'Hide' : 'Show')
					+ ' Console Palette';
		}
	}.setCommand('`', MenuItem.MODIFIER_SHIFT | MenuItem.MODIFIER_OPTION
			| MenuItem.MODIFIER_COMMAND);

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

	// UI Descriptions

	var texts = {
		execute: getDescription('Execute Script', 'e', { command: true }),
		effect: 'Apply Script as Live Effect...',
		stopAll: getDescription('Stop Running Scripts', '.', { command: true }),
		editScript: getDescription('Edit Script...', null, { enter: true }),
		createScript: getDescription('Create a New Script...', 'n',
				{ shift: true, option: true, command: true }),
		createDirectory: 'Create New Folder...',
		console: 'Show / Hide Console',
		repositories: 'Manage Repositories...',
		adjustOrigin: 'Automatically Adjust Ruler Origin',
		about: 'About Scriptographer...',
		reference: 'Reference...',
		reload: 'Reload'
	};

	// Popup Menu

	var menu = this.popupMenu;

	var executeEntry = new ListEntry(menu) {
		text: texts.execute,
		onSelect: execute
	};

	var effectEntry = hasEffects && new ListEntry(menu) {
		text: texts.effect,
		onSelect: executeEffect
	};

	var stopAllEntry = new ListEntry(menu) {
		text: texts.stopAll,
		onSelect: stopAll
	};

	var separator0Entry = new ListEntry(menu) {
		separator: true
	};

	var editScriptEntry = new ListEntry(menu) {
		text: texts.editScript,
		onSelect: editScript
	};

	var separator1Entry = new ListEntry(menu) {
		separator: true
	};

	var createScriptEntry = new ListEntry(menu) {
		text: texts.createScript,
		onSelect: createScript
	};

	var createDirectoryEntry = new ListEntry(menu) {
		text: texts.createDirectory,
		onSelect: createDirectory
	};

	var separator2Entry = new ListEntry(menu) {
		separator: true
	};

	var consoleEntry = new ListEntry(menu) {
		text: texts.console,
		onSelect: function() {
			consoleDialog.visible = !consoleDialog.visible;
		},
		onUpdate: function() {
			// TODO: Make onUpdate work in ListEntry
			this.text = (consoleDialog.visible ? 'Hide' : 'Show')
					+ ' Console Palette';
		}
	};

	var separator3Entry = new ListEntry(menu) {
		separator: true
	};

	var repositoriesEntry = new ListEntry(menu) {
		text: texts.repositories,
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

	if (illustrator.version < 15) {
		// Add a menu item that controls whether origins are automatically 
		// adjusted. Default is true:
		if (script.preferences.adjustOrigin === undefined)
			script.preferences.adjustOrigin = true;

		var adjustOriginEntry = new ListEntry(menu) {
			text: texts.adjustOrigin,
			checked: script.preferences.adjustOrigin,
			onSelect: function() {
				this.checked = !this.checked;
				script.preferences.adjustOrigin = this.checked;
			}
		};
	}

	var separator4Entry = new ListEntry(menu) {
		separator: true
	};

	var aboutEntry = new ListEntry(menu) {
		text: texts.about,
		onSelect: function() {
			aboutDialog.doModal();
		}
	};

	var referenceEntry = new ListEntry(menu) {
		text: texts.reference,
		onSelect: function() {
			illustrator.launch('file://' + new File(
					scriptographer.pluginDirectory,
					'Reference/index.html'));
		}
	};

	var separator2Entry = new ListEntry(menu) {
		separator: true
	};

	var reloadEntry = new ListEntry(menu) {
		text: texts.reload,
		onSelect: function() {
			ScriptographerEngine.reload();
		}
	};

	// Buttons

	var executeButton = new ImageButton(this) {
		image: getImage('play.png'),
		disabledImage: getImage('play-disabled.png'),
		size: buttonSize,
		toolTip: texts.execute,
		onClick: execute
	};

	var effectButton = hasEffects && new ImageButton(this) {
		image: getImage('effect.png'),
		disabledImage: getImage('effect-disabled.png'),
		size: buttonSize,
		toolTip: texts.effect,
		onClick: executeEffect
	};

	var stopButton = new ImageButton(this) {
		image: getImage('stop.png'),
		disabledImage: getImage('stop-disabled.png'),
		size: buttonSize,
		toolTip: texts.stopAll,
		onClick: stopAll
	};

	var editScriptButton = new ImageButton(this) {
		image: getImage('edit-script.png'),
		disabledImage: getImage('edit-script-disabled.png'),
		size: buttonSize,
		toolTip: texts.editScript,
		onClick: editScript
	};

	var createScriptButton = new ImageButton(this) {
		image: getImage('new-script.png'),
		disabledImage: getImage('new-script-disabled.png'),
		size: buttonSize,
		toolTip: texts.createScript,
		onClick: createScript
	};

	var createDirectoryButton = new ImageButton(this) {
		image: getImage('folder.png'),
		disabledImage: getImage('folder-disabled.png'),
		size: buttonSize,
		toolTip: texts.createDirectory,
		onClick: createDirectory
	};

	var consoleButton = new ImageButton(this) {
		image: getImage('console.png'),
		size: buttonSize,
		toolTip: texts.console,
		onClick: function() {
			consoleDialog.visible = !consoleDialog.visible;
		}
	};

	function updateItems() {
		// Update buttons and menu entries according to selected script or
		// directory.
		var entry = getSelectedScriptEntry();
		// Make sure it's not a directory
		if (entry && entry.data.isDirectory)
			entry = null;
		// Do not allow creation of new items inside sealed repositories
		var canCreate = createScriptButton.enabled = entry
				? !(entry.data.isDirectory && entry.childList
						|| entry.list).data.sealed
				: false;
		// Now update the actual items
		executeEntry.enabled = executeButton.enabled = !!entry;
		if (hasEffects)
			effectEntry.enabled = effectButton.enabled = isEffect(entry);
		editScriptEntry.enabled = editScriptButton.enabled = !!entry;
		createScriptEntry.enabled = createScriptButton.enabled = canCreate;
		createDirectoryEntry.enabled = createDirectoryButton.enabled = canCreate;
	}

	initAll();
	updateItems();

	return {
		title: 'Scriptographer',
		margin: [0, -1, -1, -1],
		content: {
			center: scriptList,
			south: new ItemGroup(this) {
				layout: [ 'left', -1, -1 ],
				content: [
					executeButton,
					stopButton,
					effectButton,
					new Spacer(4, 0),
					editScriptButton,
					new Spacer(4, 0),
					createScriptButton,
					createDirectoryButton,
					new Spacer(4, 0),
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
