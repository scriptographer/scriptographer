Path.inject(new function() {

	function set(res, properties) {
		if (properties)
			for (var i in properties)
				res[i] = properties[i];
		return res;
	}

	return {
		statics: {
			Line: function(pt1, pt2, properties) {
				return set(document.createLine(pt1, pt2), properties);
			},

			Rectangle: function(rect, properties) {
				return set(document.createRectangle(rect), properties);
			},

			RoundRectangle: function(rect, hor, ver, properties) {
				return set(document.createRoundRectangle(rect, hor, ver), properties);
			},

			RegularPolygon: function(numSides, center, radius, properties) {
				return set(document.createRegularPolygon(numSides, center, radius), properties);
			},

			Star: function(numPoints, center, radius1, radius2, properties) {
				return set(document.createStar(numPoints, center, radius1, radius2), properties);
			},

			Spiral: function(firstArcCenter, start, decayPercent, numQuarterTurns, clockwiseFromOutside, properties) {
				return set(document.createSpiral(firstArcCenter, start, decayPercent, numQuarterTurns, clockwiseFromOutside), properties);
			},

			Oval: function(rect, properties) {
				return set(document.createOval(rect), properties);
			},

			Circle: function(point, radius, properties) {
				return set(document.createCircle(point, radius), properties);
			}
		}
	}
});
