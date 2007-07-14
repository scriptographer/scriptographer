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

var consoleDialog = new FloatingDialog (
		FloatingDialog.OPTION_TABBED |
		FloatingDialog.OPTION_SHOW_CYCLE |
		FloatingDialog.OPTION_RESIZING |
		FloatingDialog.OPTION_REMEMBER_PLACING, function() {

	this.title = "Scriptographer Console";
	this.bounds = [200, 200, 400, 300];

	var engine = ScriptEngine.getEngineByName("JavaScript");
	var consoleScope = engine != null ? engine.createScope() : null;

	var textIn = new TextEdit(this, TextEdit.OPTION_MULTILINE) {
		size: [300, 100],
		minimumSize: [200, 18],
		onTrack: function(tracker) {
			if (tracker.action == Tracker.ACTION_KEY_STROKE
				&& tracker.virtualKey == Tracker.KEY_RETURN) {
				// enter was pressed in the input field. determine the
				// current line:
				var text = this.text;
				var end = this.getSelection()[1] - 1;
				var ch = text.charAt(end--);
				if (ch == '\n' || ch == '\r') { // empty line?
					text = "";
				} else {
					while (end >= 0
						&& ((ch = text[end]) == '\n' || ch == '\r'))
						end--;
					var start = end;
					end++;
					while (start >= 0
						&& ((ch = text[start]) != '\n' && ch != '\r'))
						start--;
					start++;
					text = text.substring(start, end + 1);
				}
				engine.evaluate(text, consoleScope);
			}
			return true;
		}
	};

	var textOut = new TextEdit(this, TextEdit.OPTION_READONLY
		| TextEdit.OPTION_MULTILINE) {
		size: [300, 100],
		minimumSize: [200, 18],
		backgroundColor: Drawer.COLOR_INACTIVE_TAB,
		// the onDraw workaround for display problems is only needed on mac
		onDraw: app.macintosh && false ? function(drawer) {
			// Workaround for mac, where TextEdit fields with a background
			// color
			// do not get completely filled
			// Fill in the missing parts.
			drawer.setColor(Drawer.COLOR_INACTIVE_TAB);
			var rect = drawer.boundsRect;
			// a tet line with the small font is 11 pixels heigh. there
			// seems to be a shift,
			// which was detected by trial and error. This might change in
			// future versions!
			var height = rect.height - (rect.height - 6) % 11 - 3;
			// 18 is the width of the scrollbar. This might change in future
			// versions!
			drawer.fillRect(rect.width - 18, 0, 1, height);
			drawer.fillRect(0, height, rect.width - 1, rect.height - height - 2);
		} : null
	};

	var that = this;
	var consoleText = new java.lang.StringBuffer();

	function showText() {
		if (textOut != null) {
			textOut.text = consoleText;
			textOut.setSelection(consoleText.length());
			// textOut.update();
			// textOut.invalidate();
			that.setVisible(true);
		}
	}

	// Buttons:
	var clearButton = new ImageButton(this) {
		onClick: function() {
			textOut.text = "";
			consoleText.setLength(0);
		},
		image: getImage("refresh.png"),
		size: buttonSize
	};

	// layout:
	this.margins = [-1, -1, -1, -1];
	this.layout = new TableLayout([
			[ 'fill' ],
			[ 0.2, 'fill', 15 ]
		], -1, -1);
	this.content = {
		'0, 0': textIn,
		'0, 1': textOut,
		'0, 2': new ItemContainer(new FlowLayout(FlowLayout.LEFT, -1, -1), [
			clearButton
		])
	}

	var lineBreak = java.lang.System.getProperty('line.separator');

	this.println = function(str) {
		if (textOut) {
			// If the text does not grow too long, remove old lines again:
			consoleText.append(str);
			consoleText.append(lineBreak);
			while (consoleText.length() >= 8192) {
				var pos = consoleText.indexOf(lineBreak);
				if (pos == -1)
					pos = consoleText.length() - 1;
				consoleText['delete'](0, pos + 1);
			}
			if (that.isInitialized())
				showText();
		}
	}

	this.onInitialize = function() {
		showText();
	}

	this.onDestroy = function() {
		textOut = null;
	}
});

// Interface with ScriptographerEngine
ScriptographerEngine.setCallback(new ScriptographerCallback() {
	println: function(str) {
		consoleDialog.println(str);
	},

	onAbout: function() {
		aboutDialog.doModal();
	}
});