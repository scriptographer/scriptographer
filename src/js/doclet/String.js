// Enhance String a bit:
String.inject({
	endsWith: function(end) {
		return this.length >= end.length && this.substring(this.length - end.length) == end;
	},

	startsWith: function(start) {
		return this.length >= start.length && this.substring(0, start.length) == start;
	},

	isLowerCase: function() {
		return this.toLowerCase() == this;
	},

	isUpperCase: function() {
		return this.toUpperCase() == this;
	}
});
