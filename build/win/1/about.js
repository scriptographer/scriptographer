/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Scripting Plugin for Adobe Illustrator
 * http://scriptographer.org/
 *
 * Copyright (c) 2002-2010, Juerg Lehni
 * http://scratchdisk.com/
 *
 * All rights reserved. See LICENSE file for details.
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
		'Illustrator ' + illustrator.version + '.' + illustrator.revision,
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
		marginBottom: 8,
		onTrack: function(tracker) {
			if (tracker.modifiers & Tracker.MODIFIER_BUTTON_DONW) {
				var line = Math.floor(tracker.point.y / this.getTextSize(' ').height);
				if (urls[line] && tracker.point.x < this.getTextSize(lines[line]).width)
					illustrator.launch(urls[line]);
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
