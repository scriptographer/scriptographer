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

var aboutDialog = new ModalDialog(function() {
	var that = this;

	var logo = new ImagePane(this) {
		image: getImage('logo.png'),
		margin: [-4, 4, -4, -4]
	};

	var lines = [
		'Scriptographer ' + scriptographer.version + '.'
				+ scriptographer.revision.toPaddedString(3),
		'http://scriptographer.org',
		'',
		'Copyright \u00a9 2001-' + (new Date().getFullYear()) + ' J\u00fcrg Lehni',
		'All rights reserved.',
		'',
		'Conception, API Design, Programming',
		'J\u00fcrg Lehni',
		'',
		'Documentation, API Design, Testing',
		'Jonathan Puckey',
		'',
		'Scriptographer is kindly supported by the',
		'R&D Department of ECAL, Lausanne',
		'',
		'To help out more, please consider a donation:',
		'http://scriptographer.org/donation',
		'',
		'Illustrator ' + app.version + '.' + app.revision,
		'Java ' + java.lang.System.getProperty('java.version')
	];

	var urls = { // Links text lines -> urls
		1: 'http://scriptographer.org',
		7: 'http://scratchdisk.com',
		10: 'http://jonathanpuckey.com',
		13: 'http://ecal.ch',
		16: 'http://scriptographer.org/donation',
	};

	var text = new TextPane(this) {
		text: lines.join('\n'),
		bottomMargin: 8,
		onTrack: function(tracker) {
			if (tracker.modifiers & Tracker.MODIFIER_BUTTON_DONW) {
				var line = Math.floor(tracker.point.y / this.getTextSize(' ').height);
				if (urls[line] && tracker.point.x < this.getTextSize(lines[line]).width)
					app.launch(urls[line]);
			}
			return true;
		}
	};

	var okButton = new Button(this) {
		text: '  OK  ',
	};

	global.onAbout = function() {
		that.doModal();
	}

	return {
		title: 'About Scriptographer',
		defaultItem: okButton,
		margin: 8,
		layout: [
			'preferred fill preferred',
			'preferred fill preferred'
		],
		content: {
			'0, 0, L, T': logo,
			'1, 0, 2, 1': text,
			'2, 2': okButton
		}
	};
});
