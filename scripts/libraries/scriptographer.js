Number.prototype.format = function(str) {
	return (new java.text.DecimalFormat(str || '#,##0.00'))['format(double)'](this);
}

Date.prototype.format = function(str, locale) {
	return (new java.text.SimpleDateFormat(str, locale)).format(this);
}

String.prototype.truncate = function(length, suffix) {
	if (this.length > length) {
		if (suffix == null)
			suffix = '';
		return this.substring(0, length - suffix.length).trim() + suffix;
	}
	return this;
}

Shape = new function() {
	function set(res, properties) {
		if (properties)
			for (var i in properties)
				res[i] = properties[i];
		return res;
	}

	return {
		Line: function(pt1, pt2, properties) {
			return set(document.createLine(pt1, pt2), properties);
		},

		Rectangle: function(rect, properties) {
			return set(document.createOval(rect), properties);
		},

		Oval: function(rect, properties) {
			return set(document.createOval(rect), properties);
		}
	};
}