Number.inject({
	format: function(str) {
		return (new java.text.DecimalFormat(str || '#,##0.00'))['format(double)'](this);
	}
});
