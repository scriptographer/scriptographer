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

var consoleDialog = new FloatingDialog(
		'tabbed show-cycle resizing remember-placing', function() {
	var that = this;
	var engine = ScriptEngine.getEngineByName('JavaScript');
	var consoleScope = engine != null ? engine.createScope() : null;

	var textIn = new TextEdit(this, 'multiline') {
		size: [300, 100],
		minimumSize: [200, 18],
		onTrack: function(tracker) {
			if (tracker.action == Tracker.ACTION_KEY_STROKE
					&& tracker.virtualKey == Tracker.KEY_RETURN) {
				// enter was pressed in the input field. determine the
				// current line:
				var text = this.text;
				var end = this.selection[1] - 1;
				if (/[\n\r]/.test(text.charAt(end))) { // empty line?
					text = '';
				} else {
					while (end >= 0 && /[\n\r]/.test(text[end]))
						end--;
					var start = end;
					end++;
					while (start >= 0 && !/[\n\r]/.test(text[start]))
						start--;
					start++;
					text = text.substring(start, end).trim();
				}
				try {
					if (text != '') {
						var scr = engine.compile(text, 'console');
						if (scr) {
							var res = ScriptographerEngine.execute(scr, null,
									consoleScope);
							if (res !== undefined)
								print(res);
						}
					}
				} catch (e) {
					if (e.javaException) {
						e = e.javaException;
						print(e.fullMessage || e.message);
					} else {
						print(e);
					}
				}
			}
			return true;
		}
	};

	var textOut = new TextEdit(this, 'readonly multiline') {
		size: [300, 100],
		minimumSize: [200, 18]
	};

	var consoleText = new java.lang.StringBuffer();

	function showText() {
		if (textOut) {
			textOut.text = consoleText;
			that.visible = true;
			textOut.selection = consoleText.length() - 1;
		}
	}

	// Buttons:
	var clearButton = new ImageButton(this) {
		image: getImage('refresh.png'),
		size: buttonSize,
		toolTip: 'Clear Console',
		onClick: function() {
			textOut.text = '';
			consoleText.setLength(0);
		}
	};

	// Layout:
	return {
		title: 'Scriptographer Console',
		size: [400, 300],
		margin: -1,
		layout: [
			'preferred fill',
			'0.2 fill 15',
			-1, -1
		],
		content: {
			'0, 0, 1, 0': textIn,
			'0, 1, 1, 1': textOut,
			'0, 2': clearButton
		},

		println: function(str) {
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
		},

		onInitialize: function() {
			showText();

			// Interface with ScriptographerEngine
			ScriptographerEngine.setCallback(new ScriptographerCallback() {
				println: function(str) {
					that.println(str);
				}
			});
		},

		onDestroy: function() {
			textOut = null;
		}
	}
});
