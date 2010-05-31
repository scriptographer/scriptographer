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

var firstRun = !script.preferences.accepted;

if (firstRun) {
	include('license.js');
	script.preferences.accepted = licenseDialog.doModal() == licenseDialog.defaultItem;
}

// Script Locations
var userDirectory = new File(java.lang.System.getProperty('user.home'));
var examplesDirectory = new File(ScriptographerEngine.pluginDirectory, 'Examples');
var scriptRepositories = script.preferences.repositories;

if (script.preferences.accepted) {
	if (!scriptRepositories) {
		scriptRepositories = [
			// Add standard Examples repository
			{ name: 'Examples', sealed: true, visible: true }
		];
		var dir = Dialog.chooseDirectory(
				'Please choose your Scriptographer Script Folder. It is recommended\n'
				+ ' that you keep your scripts in a dedicated folder within your Documents.',
				userDirectory);
		if (dir && dir.isDirectory()) {
			scriptRepositories.push({
				name: 'My Scripts', path: dir.path, visible: true
			});
		}
		script.preferences.repositories = scriptRepositories;
	}
	include('console.js');
	include('about.js');
	include('repositories.js');
	include('main.js');

	if (!script.preferences.installed) {
		// include('install.js');
		script.preferences.installed = true;
	}
}
