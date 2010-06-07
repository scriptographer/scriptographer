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

var repositoriesDialog = new ModalDialog(function() {
	var that = this;
	// Set font now as all best-size calculations depend on it (e.g. setting
	// only width on edits).
	this.font = 'palette';

	var cancelButton = new Button(this) {
		text: 'Cancel'
	};

	var okButton = new Button(this) {
		text: '  OK  '
	};

	var upButton = new ImageButton(this) {
		image: getImage('up.png'),
		disabledImage: getImage('up-disabled.png'),
		size: buttonSize,
		toolTip: 'Move Repository Up',
		marginLeft: 0,
		onClick: function() {
			moveEntry(selectedEntry, -1);
		}
	};

	var downButton = new ImageButton(this) {
		image: getImage('down.png'),
		disabledImage: getImage('down-disabled.png'),
		size: buttonSize,
		toolTip: 'Move Repository Down',
		onClick: function() {
			moveEntry(selectedEntry, 2);
		}
	};

	var addButton = new ImageButton(this) {
		image: getImage('new.png'),
		disabledImage: getImage('new-disabled.png'),
		size: buttonSize,
		toolTip: 'Add New Repository',
		onClick: function() {
			var dir = chooseDirectory();
			if (dir) {
				addEntry({ name: '', path: dir.path, visible: true }, true);
				nameEdit.active = true;
			}
		}
	};

	var removeButton = new ImageButton(this) {
		image: getImage('remove.png'),
		disabledImage: getImage('remove-disabled.png'),
		toolTip: 'Remove Repository',
		size: buttonSize,
		onClick: function() {
			removeEntry(selectedEntry);
		}
	};

	var hideCheckbox = new ImageCheckBox(this) {
		image: getImage('hidden.png'),
		disabledImage: getImage('hidden-disabled.png'),
		toolTip: 'Hide Repository',
		size: buttonSize,
		onClick: changeSelectedVisibility
	};

	var nameEdit = new TextEdit(this) {
		width: 80,
		onChange: changeSelectedEntry
	};

	var arrowImage = new ImagePane(this) {
		image: getImage('arrow.png'),
	};

	var pathEdit = new TextEdit(this) {
		onChange: changeSelectedEntry
	};

	var chooseButton = new ImageButton(this) {
		image: getImage('folder.png'),
		disabledImage: getImage('folder-disabled.png'),
		size: buttonSize,
		toolTip: 'Choose Directory',
		onClick: changeSelectedDirectory
	};

	var selectedEntry = null;
	var previousEntry = {}; // To force a change when setting to null.
	var separator = ' -> ';

	function addEntry(repository, select) {
		var entry = new ListEntry(repositoriesList) {
			name: repository.name,
			directory: repository.path && new File(repository.path),
			visible: repository.visible,
			sealed: repository.sealed
		};
		updateEntry(entry);
		if (select)
			selectEntry(entry);
		return entry;
	}

	function updateEntry(entry) {
		var parts = [];
		if (entry.name)
			parts.push(entry.name);
		if (entry.directory && !entry.sealed)
			parts.push(entry.directory);
		entry.text = parts.join(separator);
		entry.image = getImage(entry.visible ? 'folder.png' : 'folder-hidden.png');
	}

	function selectEntry(entry) {
		if (previousEntry != entry) {
			if (previousEntry && previousEntry.isValid && previousEntry.isValid())
				previousEntry.selected = false;
			previousEntry = entry
			if (entry)
				entry.selected = true;
			updateEditor(entry);
			selectedEntry = entry;
		}
	}

	function moveEntry(entry, dir) {
		if (entry) {
			// There's no way to move, we need to duplicate and remove.
			var index = entry.index + dir;
			if (index > repositoriesList.length)
				index = 0;
			var other = repositoriesList.add(index, entry);
			other.name = entry.name;
			other.directory = entry.directory;
			other.visible = entry.visible;
			other.sealed = entry.sealed;
			var selected = entry.selected;
			entry.remove();
			if (selected)
				selectEntry(other);
		}
	}

	function removeEntry(entry) {
		if (entry && !entry.sealed && (!entry.name && !entry.directory
				|| Dialog.confirm('Removing Repository',
						'Do you really want to remove the repository\n'
						+ "'" + (entry.name || entry.directory) + "'?"))) {
			if (entry.selected) {
				var index = entry.index;
				entry.remove();
				do {
					entry = repositoriesList[index];
				} while (!entry && --index >= 0)
				selectEntry(entry);
			}
		}
	}

	function chooseDirectory(directory) {
		return Dialog.chooseDirectory(
				'Choose a Scriptographer Script Repository Folder.',
				directory || userDirectory);
	}

	function changeSelectedEntry() {
		if (selectedEntry) {
			if (nameEdit.enabled)
				selectedEntry.name = nameEdit.text;
			if (pathEdit.enabled)
				selectedEntry.directory = new File(pathEdit.text);
			updateEntry(selectedEntry);
		}
	}

	function changeSelectedVisibility() {
		if (selectedEntry) {
			selectedEntry.visible = !hideCheckbox.checked;
			changeSelectedEntry();
			updateEditor(selectedEntry);
		}
	}

	function changeSelectedDirectory() {
		if (selectedEntry) {
			var dir = chooseDirectory(selectedEntry.directory);
			if (dir) {
				pathEdit.text = dir;
				changeSelectedEntry();
			}
		}
	}

	function updateEditor(entry) {
		var dir = entry && entry.directory;
		var enabled = !entry || !entry.sealed;
		if (entry)
			editor.enabled = true;
		removeButton.enabled = chooseButton.enabled = enabled;
		nameEdit.enabled = pathEdit.enabled = enabled;
		nameEdit.text = entry && entry.name || '';
		pathEdit.text = enabled && dir || '';
		hideCheckbox.checked = entry && !entry.visible;
		if (!entry)
			editor.enabled = false;
		addButton.enabled = true;
	}

	var width = 500;
	var repositoriesList = new ListBox(this) {
		style: 'black-rect',
		size: [width, 10 * lineHeight],
		minimumSize: [width, 8 * lineHeight],
		entryTextRect: [0, 0, width, lineHeight],
		// onChange does not fire for key events, so abuse onTrack
		onTrackEntry: function(tracker, entry) {
			// This might change the selection state
			entry.defaultTrack(tracker);
			selectEntry(this.selected.first);
			// Return false to prevent calling of defaultTrack as we called
			// it already.
			return false;
		},

		onChange: function() {
			selectEntry(this.selected.first);
		}
	};

	var editor = new ItemGroup(this) {
		marginTop: -1,
		marginBottom: 6,
		layout: [
			'preferred preferred 4 preferred 4 preferred preferred 4 preferred preferred fill preferred',
			'preferred',
			-1, -1
		],
		content: {
			'0, 0': upButton,
			'1, 0': downButton,
			'3, 0': hideCheckbox,
			'5, 0': addButton,
			'6, 0': removeButton,
			'8, 0': nameEdit,
			'9, 0': arrowImage,
			'10, 0': pathEdit,
			'11, 0': chooseButton
		}
	};

	return {
		title: 'Manage Scriptographer Repositories',
		defaultItem: okButton,
		cancelItem: cancelButton,
		margin: 8,
		layout: [
			'preferred',
			'preferred preferred preferred',
			0, 0
		],
		content: {
			'0, 0': repositoriesList,
			'0, 1': editor,
			'0, 2': new ItemGroup(this) {
				layout: [ 'right' ],
				content: [
					cancelButton,
					okButton
				]
			}
		},

		choose: function(repositories) {
			repositoriesList.removeAll();
			if (repositories) {
				repositories.each(function(repository) {
					addEntry(repository);
				})
			}
			selectEntry();
			if (this.doModal() == okButton) {
				return repositoriesList.map(function(entry) {
					return {
						name: entry.name,
						path: entry.directory && entry.directory.path,
						visible: entry.visible,
						sealed: entry.sealed
					};
				});
			}
			return null;
		}
	};
});
