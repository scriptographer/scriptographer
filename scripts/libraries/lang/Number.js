Number.inject({
	format: function(str) {
		return (new java.text.DecimalFormat(str || '#,##0.00'))['format(double)'](this);
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
});
