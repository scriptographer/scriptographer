Number.inject(new function() {
	var formats = {};

	return {
		format: function(str) {
			str = str || '#,##0.00';
			var format = formats[str];
			if (!format)
				format = formats[str] = new java.text.DecimalFormat(str);
			return format['format(double)'](this);
		},

		toRadians: function() {
			return this * Math.PI / 180;
		},

		toDegrees: function() {
			return this * 180 / Math.PI;
		},

		isEven: function() {
			return this % 2 == 0;
		},

		isOdd: function() {
			return this % 2 != 0;
		}
	}
});
