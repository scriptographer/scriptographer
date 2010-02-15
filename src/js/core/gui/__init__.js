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

importPackage(Packages.com.scriptographer);
importPackage(Packages.com.scratchdisk.script);
importPackage(Packages.com.scriptographer.script);

script.showProgress = false;

// Load the core libraries
loadLibraries(new File(script.directory.parentFile, 'lib'));

var buttonSize = new Size(27, 17);
var lineHeight = 17;
var lineBreak = java.lang.System.getProperty('line.separator');

function getImage(filename) {
	return new Image(new File(script.directory, 'resources/' + filename));
}

function loadLibraries(dir) {
	var files = dir.listFiles();
	if (files) {
		for (var i = 0; i < files.length; i++) {
			var file = files[i];
			if (file.isDirectory() && !/^\.|^CVS$/.test(file.name)) {
				loadLibraries(file);
			} else {
				try {
					var engine = ScriptEngine.getEngineByFile(file);
					if (engine) {
						var scr = engine.compile(file);
						// Don't call scr.execute directly, since we handle
						// SG specific things in ScriptographerEngine.execute:
						if (scr)
							ScriptographerEngine.execute(scr, file, engine.globalScope);
					}
				} catch (e) {
					print(e);
				}
			}
		}
	}
}

var tool = new Tool('Scriptographer Tool', getImage('tool.png')) {
	activeImage: getImage('tool-active.png'),
	tooltip: 'Execute a tool script to assign it with this tool button'
};

if (!script.preferences.accepted) {
	include('license.js');
	script.preferences.accepted = licenseDialog.doModal() == licenseDialog.defaultItem;
}

if (script.preferences.accepted) {
	include('console.js');
	include('about.js');
	include('main.js');

	if (!script.preferences.installed) {
		// include('install.js');
		script.preferences.installed = true;
	}
}
