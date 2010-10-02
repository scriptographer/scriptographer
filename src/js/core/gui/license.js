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

var licenseDialog = new ModalDialog(function() {
	// Add trailing zeros to revision
	var revision = scriptographer.revision + '';
	while (revision.length < 3)
		revision = '0' + revision;

	var logo = new ImagePane(this) {
		image: getImage('logo.png'),
		marginRight: 10
	};

	var lines = [
		'You seem to be running a new version of Scriptographer',
		'for the first time.',
		'',
		'Please read the terms and conditions carefully:',
		'',
		'http://www.scriptographer.org/license',
		'',
		'And note that if you use Scriptographer in your work,',
		'you should mention it along with the authors of',
		'the scripts in use along with the product.',
		'',
		'If you use Scriptographer for commercial projects,',
		'please consider a donation to support the effort:',
		'',
		'http://www.scriptographer.org/donation',
		'',
		'Thank you!'
	];

	var urls = { // Links text lines -> urls
		5: 'http://www.scriptographer.org/license',
		14: 'http://www.scriptographer.org/donation'
	};

	var text = new TextPane(this) {
		text: lines.join('\n'),
		marginBottom: 10,
		onTrack: function(tracker) {
			if (tracker.modifiers & Tracker.MODIFIER_BUTTON_DONW) {
				var line = Math.floor(tracker.point.y / this.getTextSize(' ').height);
				if (urls[line] && tracker.point.x < this.getTextSize(lines[line]).width)
					illustrator.launch(urls[line]);
			}
			return true;
		}
	};
	var acceptButton = new Button(this) {
		text: 'Accept',
	};
	var cancelButton = new Button(this) {
		text: 'Cancel',
	};

	return {
		title: 'Welcome To Scriptographer ' + scriptographer.version,
		margin: 10,
		layout: [
			'preferred fill preferred preferred',
			'preferred fill preferred'
		],
		content: {
			'0, 0, L, T': logo,
			'1, 0, 3, 1': text,
			'2, 2': cancelButton,
			'3, 2': acceptButton
		},
		defaultItem: acceptButton,
		cancelItem: cancelButton
	};
});
