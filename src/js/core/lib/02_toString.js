// A list of types that simply redirect toString to toSource(true)
[FillStyle, StrokeStyle, PathStyle, Artboard].each(function(type) {
	type.inject({
		toString: function() {
			return this.toSource(true);
		}
	});
});
