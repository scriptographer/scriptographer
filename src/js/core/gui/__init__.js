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

script.keepAlive = true;
script.showProgress = false;

mapJavaClass(java.io.File, File);

importPackage(Packages.com.scriptographer);
importPackage(Packages.com.scratchdisk.script);
importPackage(Packages.com.scriptographer.script);

var buttonSize = new Size(27, 17);
var lineHeight = 17;
var lineBreak = java.lang.System.getProperty('line.separator');
var images = {};

function getImage(filename) {
	// Cache images so getImage can be used efficiently.
	var image = images[filename];
	if (!image) {
		image = new Image(new File(script.directory, 'resources/' + filename));
		images[filename] = image;
	}
	return image;
}

function getDescription(text, shortcut, modifiers) {
	if (shortcut) {
		var parts = [];
		var isMac = illustrator.isMacintosh();
		if (modifiers) {
			if (modifiers.shift)
				parts.push(isMac ? '\u21e7' : 'Shift');
			if (modifiers.option)
				parts.push(isMac ? '\u2325' : 'Alt');
			if (modifiers.command)
				parts.push(isMac ? '\u2318' : 'Ctrl');
		}
		parts.push(shortcut.toUpperCase());
		text += ' (' + parts.join(isMac ? '' : '+') + ')';
	}
	return text;
}

var firstRun = !script.preferences.accepted;

if (firstRun) {
	include('license.js');
	script.preferences.accepted =
			licenseDialog.doModal() == licenseDialog.defaultItem;
}

function getDocumentsDirectory() {
	if (illustrator.isMacintosh()) {
		var FileManager = com.apple.eio.FileManager;
		// Some old JVMs on Mac do not seem to support this. Fall back to
		// using user.home in that case:
		if (FileManager && typeof FileManager.OSTypeToInt == 'function') {
			return new File(FileManager.findFolder(FileManager.kUserDomain,
					FileManager.OSTypeToInt('docs')));
		} else {
			return new File(java.lang.System.getProperty('user.home'),
					'Documents');
		}
	} else {
		var view = javax.swing.filechooser.FileSystemView.getFileSystemView();
		return view.getDefaultDirectory();
	}
}

// Script Locations
var documentsDirectory = getDocumentsDirectory();
var coreDirectory = ScriptographerEngine.coreDirectory;
var scriptsDirectory = new File(ScriptographerEngine.pluginDirectory, 'Scripts');
var scriptRepositories = script.preferences.repositories;

if (script.preferences.accepted) {
	if (!scriptRepositories) {
		scriptRepositories = [];
		var dir = new File(documentsDirectory, 'Scriptographer Scripts');
		if (!dir.exists()) {
			if (Dialog.confirm('Scriptographer Scripts Folder',
				'You do not have a Scriptographer scripts folder set up\n'
				+ 'for your own scripts. Would you like to create a scripts\n'
				+ 'folder in your Documents now and define it as a script\n'
				+ 'repository in Scriptographer?\n\n'
				+ 'We recommend new users to do so.')) {
				dir.makeDirectory();
			} else {
				dir = Dialog.chooseDirectory(
					'Please choose your Scriptographer Scripts Folder.'
					+ 'We recommend' + (illustrator.isMacintosh() ? '\n' : ' ')
					+ 'to keep your scripts in a dedicated folder'
					+ 'within your Documents.',
					documentsDirectory);
			}
		}
		if (dir && dir.isDirectory()) {
			scriptRepositories.push({
				name: 'My Scripts', path: dir.path, visible: true
			});
		}
	}
	// Filter out folders that contain the core script directory, as this
	// would lead to an endless loop while loading otherwise. Also filter empty
	// ones, which were set up wrongly:
	scriptRepositories = scriptRepositories.filter(function(repository) {
		return repository.sealed || repository.path
				&& !new File(repository.path).contains(coreDirectory);
	});
	// Add standard Examples and Tutorials repositories if they don't exist:
	// Reversed sequence, due to unshift
	['Tutorials', 'Examples'].each(function(name) {
		if (!scriptRepositories.contains(function(repository) {
			return repository.name == name;
		})) {
			scriptRepositories.unshift({
				name: name, sealed: true, visible: true
			});
		}
	})
	script.preferences.repositories = scriptRepositories;
	include('console.js');
	include('about.js');
	include('repositories.js');
	include('main.js');

	if (!script.preferences.installed) {
		// include('install.js');
		script.preferences.installed = true;
	}
}
