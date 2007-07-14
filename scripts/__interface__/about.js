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

var aboutDialog = new ModalDialog(function() {
	this.title = "About Scriptographer";

	// Add trailing zeros to revision
	var revision = scriptographer.revision + '';
	while (revision.length < 3)
		revision = '0' + revision;

	var logo = new ImageStatic(this);
	logo.image = getImage("logo.png");

	var text = new Static(this) {
		text: "Scriptographer " + scriptographer.version + "." + revision + "\n"
			+ "http://www.scriptographer.com\n\n"
			+ "\u00a9 2001-" + (new Date().getFullYear()) + " J\u00fcrg Lehni\n"
			+ "http://www.scratchdisk.com\n\n"
			+ "All rights reserved.\n\n"
			+ "Illustrator " + app.version + "." + app.revision + "\n"
			+ "Java " + java.lang.System.getProperty("java.version") + "\n",

		onTrack: function(tracker) {
			if (tracker.modifiers & Tracker.MODIFIER_CLICK) {
				var line = Math.floor(tracker.point.y / this.getTextSize(" ", -1).height);
				var url = line == 1 ? "http://www.scriptographer.com"
						: line == 4 ? "http://www.scratchdisk.com"
						: null;
				if (url && tracker.point.x < this.getTextSize(url, -1).width)
					app.launch(url);
			}
			return true;
		}
	};
	var okButton = new Button(this) {
		font: Dialog.FONT_PALETTE,
		text: 'OK'
	};

	this.defaultItem = okButton;
	this.margins = 10;
	this.layout = new TableLayout([
			[ 'prefered', 'fill', 'prefered' ],
			[ 'prefered', 'fill', 'prefered' ]
		], 4, 4);
	this.content = {
		'0, 0': logo,
		'1, 0, 2, 1': text,
		 '2, 2': okButton
	}
});
