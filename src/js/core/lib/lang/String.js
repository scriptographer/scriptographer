String.inject({
	truncate: function(length, suffix) {
		if (this.length > length) {
			suffix = suffix || '';
			return this.substring(0, length - suffix.length).trim() + suffix;
		}
		return this;
	}
});
