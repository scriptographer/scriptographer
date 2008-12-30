if (!script.preferences.accepted) {
	include('license.js');
	script.preferences.accepted = licenseDialog.doModal() == licenseDialog.defaultItem;
}

if (script.preferences.accepted) {
	// Read the script directory first, or ask for it if its not defined:
	var dir = script.preferences.scriptDirectory;
	// If no script directory is defined, try the default place for Scripts:
	// The subdirectory 'scripts' in the plugin directory:
	dir = dir
		? new File(dir)
		: new File(scriptographer.pluginDirectory, 'scripts');
	if (!dir.exists() || !dir.isDirectory()) {
		if (!chooseScriptDirectory(dir))
			Dialog.alert('Could not find Scriptographer script directory.');
	} else {
		setScriptDirectory(dir);
	}

	include('console.js');
	include('about.js');
	include('main.js');

	if (!script.preferences.installed) {
		include('install.js');
		script.preferences.installed = true;
	}
}
