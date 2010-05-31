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
			moveEntry(-1);
		}
	};

	var downButton = new ImageButton(this) {
		image: getImage('down.png'),
		disabledImage: getImage('down-disabled.png'),
		size: buttonSize,
		toolTip: 'Move Repository Down',
		onClick: function() {
			moveEntry(2);
		}
	};

	var addButton = new ImageButton(this) {
		image: getImage('new.png'),
		disabledImage: getImage('new-disabled.png'),
		size: buttonSize,
		toolTip: 'Add New Repository',
		onClick: function() {
			addEntry('', '', true, true);
			chooseDirectory();
			nameEdit.active = true;
		}
	};

	var removeButton = new ImageButton(this) {
		image: getImage('remove.png'),
		disabledImage: getImage('remove-disabled.png'),
		toolTip: 'Remove Repository',
		size: buttonSize,
		onClick: removeEntry
	};

	var visibleButton = new ImageButton(this) {
		image: getImage('visible.png'),
		disabledImage: getImage('visible-disabled.png'),
		toolTip: 'Show / Hide Repository',
		size: buttonSize,
		onClick: changeEntryVisibility
	};

	var nameEdit = new TextEdit(this) {
		width: 80,
		onChange: changeEntry
	};

	var arrowImage = new ImagePane(this) {
		image: getImage('arrow.png'),
	};

	var pathEdit = new TextEdit(this) {
		onChange: changeEntry
	};

	var chooseButton = new ImageButton(this) {
		image: getImage('folder.png'),
		disabledImage: getImage('folder-disabled.png'),
		size: buttonSize,
		toolTip: 'Choose Directory',
		onClick: chooseDirectory
	};

	var selectedEntry = null;
	var previousEntry = {}; // To force a change when setting to null.
	var separator = ' -> ';

	function getEntryText(name, directory) {
		var parts = [];
		if (name)
			parts.push(name);
		if (directory && directory != examplesDirectory)
			parts.push(directory);
		return parts.join(separator);
	}

	function getEntryImage(visible) {
		return getImage(visible ? 'folder.png' : 'folder-hidden.png');
	}

	function addEntry(name, directory, visible, select) {
		var entry = new ListEntry(repositoriesList) {
			text: getEntryText(name, directory),
			image: getEntryImage(visible),
			name: name,
			directory: directory,
			visible: visible
		}
		if (select)
			selectEntry(entry);
		return entry;
	}

	function changeEntry() {
		if (selectedEntry) {
			if (nameEdit.enabled)
				selectedEntry.name = nameEdit.text;
			if (pathEdit.enabled)
				selectedEntry.directory = new File(pathEdit.text);
			selectedEntry.image = getEntryImage(selectedEntry.visible);
			selectedEntry.text = getEntryText(selectedEntry.name,
					selectedEntry.directory);
		}
	}

	function changeEntryVisibility() {
		if (selectedEntry) {
			selectedEntry.visible = !selectedEntry.visible;
			changeEntry();
			updateEditor(selectedEntry);
		}
	}

	function removeEntry() {
		if (selectedEntry && Dialog.confirm('Removing Repository',
				'Do you really want to remove this repository?')) {
			var index = selectedEntry.index;
			selectedEntry.remove();
			var entry;
			do {
				entry = repositoriesList[index];
			} while (!entry && --index >= 0)
			selectEntry(entry);
		}
	}

	function updateEditor(entry) {
		var dir = entry && entry.directory;
		var enabled = dir != examplesDirectory;
		if (entry)
			editor.enabled = true;
		removeButton.enabled = chooseButton.enabled = enabled;
		nameEdit.enabled = pathEdit.enabled = enabled;
		nameEdit.text = entry && entry.name || '';
		pathEdit.text = enabled && dir || '';
		visibleButton.image = getImage(entry && entry.visible
				? 'visible.png' : 'visible-disabled.png');
		if (!entry)
			editor.enabled = false;
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

	function moveEntry(dir) {
		if (selectedEntry) {
			// There's no way to move, we need to duplicate and remove.
			var entry = repositoriesList.add(selectedEntry.index + dir, selectedEntry);
			entry.name = selectedEntry.name;
			entry.directory = selectedEntry.directory;
			selectedEntry.remove();
			selectEntry(entry);
		}
	}

	function chooseDirectory() {
		if (selectedEntry) {
			var dir = Dialog.chooseDirectory(
					'Choose a Scriptographer Script Repository Folder:',
					selectedEntry.directory || userDirectory);
			if (dir) {
				selectedEntry.directory = dir;
				pathEdit.text = dir;
				changeEntry();
			}
		}
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
			'3, 0': visibleButton,
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
					addEntry(repository.name, new File(repository.path),
							repository.visible);
				})
			}
			selectEntry();
			if (this.doModal() == okButton) {
				repositories = [];
				return repositoriesList.each(function(entry) {
				 	var repository = {
						path: entry.directory.path,
						visible: entry.visible
					};
					if (entry.name)
						repository.name = entry.name;
					this.push(repository);
				}, repositories);
			}
			return null;
		}
	};
});
