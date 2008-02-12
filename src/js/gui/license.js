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
 * $Id$
 */

var licenseDialog = new ModalDialog(function() {
	// this.font = Dialog.FONT_PALETTE;

	// Add trailing zeros to revision
	var revision = scriptographer.revision + '';
	while (revision.length < 3)
		revision = '0' + revision;

	var logo = new ImageStatic(this) {
		image: getImage('logo.png'),
		rightMargin: 10
	};

	var text = new Static(this) {
		text: 'You seem to be running a new version of Scriptographer\n'
			+ 'for the first time.\n\n'
			+ 'Please read the terms and conditions carefully:\n\n'
			+ 'http://www.scriptographer.com/license\n\n'
			+ 'And note that if you use Scriptographer in your work,\n'
			+ 'you should mention it along with the authors of\n'
			+ 'the scripts in use along with the product.\n\n'
			+ 'If you use Scriptographer for commercial projects,\n'
			+ 'please consider a donation to support the effort:\n\n'
			+ 'http://www.scriptographer.com/donation\n\n'
			+ 'Thank you!',

		bottomMargin: 10,

		onTrack: function(tracker) {
			if (tracker.modifiers & Tracker.MODIFIER_CLICK) {
				var line = Math.floor(tracker.point.y / this.getTextSize(' ', -1).height);
				var url = line == 5 ? 'http://www.scriptographer.com/license'
						: line == 14 ? 'http://www.scriptographer.com/donation'
						: null;
				if (url && tracker.point.x < this.getTextSize(url, -1).width)
					app.launch(url);
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
