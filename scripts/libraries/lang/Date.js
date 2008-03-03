Date.inject({
	format: function(str, locale) {
		return (new java.text.SimpleDateFormat(str, locale)).format(this);
	}
});
