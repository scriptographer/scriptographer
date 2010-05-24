Point.inject(new function() {
	return ['round', 'ceil', 'floor', 'abs'].each(function(name) {
		this[name] = function() {
			return new Point(Math[name](this.x), Math[name](this.y));
		};
	}, {
		statics: ['min', 'max'].each(function(name) {
			this[name] = function(pt1, pt2) {
				return new Point(Math[name](pt1.x, pt2.x),
						Math[name](pt1.y, pt2.y));
			};
		}, {
			random: function() {
				return new Point(Math.random(), Math.random());
			}
		})
	});
});

Size.inject(new function() {
	return ['round', 'ceil', 'floor', 'abs'].each(function(name) {
		this[name] = function() {
			return new Size(Math[name](this.width), Math[name](this.height));
		};
	}, {
		statics: ['min', 'max'].each(function(name) {
			this[name] = function(size1, size2) {
				return new Size(Math[name](size1.width, size2.width),
						Math[name](size1.height, size2.height));
			};
		}, {
			random: function() {
				return new Size(Math.random(), Math.random());
			}
		})
	});
});
