function PreferenceProperty(name, defaultValue) {
	var cacheName = '_' + name;

	return {
		get: function() {
			var value = this[cacheName];
			if (value === undefined)
				value = this[cacheName] = Base.pick(script.preferences[name],
						defaultValue);
			return value;
		},

		set: function(value) {
			this[cacheName] = script.preferences[name] = value;
		}
	}
}
