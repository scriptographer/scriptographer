[FillStyle, StrokeStyle, PathStyle].each(function(type) {
	type.inject({
		toString: function() {
			return this.toSource(true);
		}
	});
});
