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

if (illustrator.isMacintosh()) {
	function executeProcess(command, input) {
		var process = java.lang.Runtime.getRuntime().exec(command);
		if (input) {
			var out = new java.io.PrintStream(process.getOutputStream());
			out.print(input);
			out.flush();
		}
		// Wait for the process to finish.
		// Unfortunatelly process.waitFor does not know waitFor(timeout)
		var time = new Date().getTime(), exc;
		while(new Date().getTime() - time < 500) {
			try {
				process.exitValue();
				break;
			} catch (e) {
			}
			java.lang.Thread.sleep(10);
		}
		process.exitValue();
		function readStream(stream) {
			var reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(stream));
			var res = [], line, first = true;
			while ((line = reader.readLine()) != null) {
				if (first) first = false;
				else res.push(lineBreak);
				res.push(line);
			}
			return res.join('');
		}

		var error = readStream(process.getErrorStream());
		if (error && !(/^\s*WARNING:/.test(error)))
			throw 'Error in command \'' + command + '\': ' + error;

		var ret = readStream(process.getInputStream());
		process.destroy();
		return ret;
	}

	// Determine current user and see if it is part of the uucp group,
	// as required by RXTX
	var user = executeProcess('id -p').match(/uid.(\w*)/)[1];
	var found = false;
	var useNS = new File('/usr/bin/niutil').exists();
	var useDS = !useNS && new File('/usr/bin/dscl').exists();
	if (useDS) {
		var res = executeProcess('dsmemberutil checkmembership -U ' + user
				+ ' -G uucp');
		found = res != 'user is not a member of the group';
	} else if (useNS) {
		var groups = []
		try {
			groups = executeProcess(
					'niutil -readprop / /groups/uucp users').split(/\n/);
		} catch(e) {}
		for (var i = 0; i < groups.length && !found; i++)
			found = groups[i] == user;
	}

	// Also create /var/Lock if it does not exist yet.
	var file = new File('/var/lock');
	if (!file.exists() || !found) {
		var dialog = new ModalDialog(function() {
			var logo = new ImagePane(this) {
				image: getImage('logo.png')
			};

			var text = new TextPane(this) {
				text: 'You appear to be runing Scriptographer for the first time.\n\n' +
					'If you would like to use the included RXTX library for\n' +
					'serial port communication, some modifications would need\n' +
					'to be made now.\n\n' +
					'Please enter your password now:'
			};

			var passwordField = new TextEdit(this, 'password');

			var cancelButton = new Button(this) {
				text: 'Cancel'
			};

			var okButton = new Button(this) {
				text: '  OK  '
			};

			return {
				title: 'Scriptographer Setup',
				margin: 10,
				layout: [
					'preferred fill preferred preferred',
					'preferred fill preferred preferred',
					4, 4
				],
				content: 	{
					'0, 0': logo,
					'1, 0, 3, 1': text,
					'1, 2, 3, 2': passwordField,
					'2, 3': cancelButton,
					'3, 3': okButton
				},
				defaultItem: okButton,
				cancelItem: cancelButton,
				passwordField: passwordField
			}
		});
		var tryAgain = true;
		while (tryAgain && dialog.doModal() == dialog.defaultItem) {
			try {
				executeProcess('sudo -K');
				executeProcess('sudo -v', dialog.passwordField.text + '\n');
				if (!file.exists()) {
					executeProcess('sudo mkdir /var/lock');
					executeProcess('sudo chgrp uucp /var/lock');
					executeProcess('sudo chmod 775 /var/lock');
				}
				if (!found) {
					if (useDS) executeProcess(
							'sudo dscl . -append /Groups/uucp GroupMembership  '
							+ user);
					else if (useNS) executeProcess(
							'sudo niutil -mergeprop / /groups/uucp users '
							+ user);
				}
				executeProcess('sudo -K');
		  		Dialog.alert(
						'Finished making changes, you should be all set now.\n\n'
						+ 'Have fun!');
				tryAgain = false;
			} catch (e) {
				tryAgain = Dialog.confirm(
					'You do not seem to have the required permissions.\n' +
					'Would you like to reenter your password now?');
			}
		}
	}
}
