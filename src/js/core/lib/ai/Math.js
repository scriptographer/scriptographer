Point.inject(new function() {
	return ['round', 'ceil', 'floor', 'abs'].each(function(name) {
		this[name] = function() {
			return new Point(Math[name](this.x), Math[name](this.y));
		};
	}, {
		statics: {
			random: function() {
				return new Point(Math.random(), Math.random());
			}
		}
	});
});

Size.inject(new function() {
	return ['round', 'ceil', 'floor', 'abs'].each(function(name) {
		this[name] = function() {
			return new Size(Math[name](this.width), Math[name](this.height));
		};
	}, {
		statics: {
			random: function() {
				return new Size(Math.random(), Math.random());
			}
		}
	});
});

Rectangle.inject(new function() {
	return ['round', 'ceil', 'floor', 'abs'].each(function(name) {
		this[name] = function() {
			return new Rectangle(Math[name](this.bottomRight), Math[name](this.size));
		};
	}, {});
});