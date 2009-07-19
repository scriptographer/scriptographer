Object.inject({
	toSource: function(simple) {
		if (typeof this.length == 'number') {
			var parts = [];
			for (var i = 0, l = this.length; i < l; i++)
				parts.push(Object.toSource(this[i], simple));
			return '[' + parts.join(', ') + ']';
		} else {
			var parts = [];
			for (var i in this)
				parts.push(i + ': ' + Object.toSource(this[i], simple));
			return '{ ' + parts.join(', ') + ' }';
		}
	},

	statics: {
		toSource: function(obj, simple, fields) {
			if (obj === null) {
				return 'null';
			} else if (obj === undefined) {
				return 'undefined';
			} else if (fields) {
				// Used by PathStyle, StrokeStyle, FillStyle
				if (simple) {
					var parts = [];
					for (var i = 0, l = fields.length; i < l; i++) {
						var name = fields[i], value = obj[name];
						if (value !== undefined)
							parts.push(name + ': ' + Object.toSource(value, true));
					}
					if (parts.length == 1 && obj.color !== undefined)
						return parts[0];
					else
						return '{ ' + parts.join(', ') + ' }';
				} else {
					var parts = [];
					for (var i = 0, l = fields.length; i < l; i++)
						parts.push(Object.toSource(obj[fields[i]], true));
					return 'new ' + obj['class'].simpleName + '(' + parts.join(', ') + ')';
				}
			} else if (!obj.toSource || /^(boolean|string|number)$/.test(typeof obj)) {
				return uneval(obj);
			} else {
				return obj.toSource(simple);
			}
		}
	}
});

Point.inject({
	toSource: function(simple) {
		if (simple)
			return '{ x: ' + this.x + ', y: ' + this.y + ' }';
		else
			return 'new Point(' + this.x + ', ' + this.y + ')';
	}
});

Size.inject({
	toSource: function(simple) {
		if (simple)
			return '{ width: ' + this.width + ', height: ' + this.height + ' }';
		else
			return 'new Size(' + this.width + ', ' + this.height + ')';
	}
});

Rectangle.inject({
	toSource: function(simple) {
		if (simple)
			return '{ x: ' + this.x + ', y: ' + this.y + ', width: ' + this.width + ', height: ' + this.height + ' }';
		else
			return 'new Rectangle(' + this.x + ', ' + this.y + ', ' + this.width + ', ' + this.height + ')';
	}
});

Segment.inject({
	toSource: function(simple) {
		var hasHandleIn = !this.handleIn.isZero();
		var hasHandleOut = !this.handleOut.isZero();
		var parts = [this.point.toSource(simple)];
		if (hasHandleIn || hasHandleOut)
			parts.push(hasHandleIn ? this.handleIn.toSource(simple) : 'null');
		if (hasHandleOut)
			parts.push(this.handleOut.toSource(simple));
		if (simple) {
			// If there's only one point, return a point instead of a list
			return parts.length == 1 ? parts[0] : '[' + parts.join(', ') + ']';
		} else {
			return 'new Segment(' + parts.join(', ') + ')';
		}
	}
});

Curve.inject({
	toSource: function(simple) {
		if (simple) {
			var parts = [];
			parts.push('{ point1: ' + this.point1.toSource(true));
			if (!this.handle1.isZero())
				parts.push(', handle1: ' + this.handle1.toSource(true));
			if (!this.handle2.isZero())
				parts.push(', handle2: ' + this.handle2.toSource(true));
			parts.push(', point2: ' + this.point2.toSource(true));
			parts.push(' }');
			return parts.join('');
		} else {
			var parts = [this.point1.toSource(false)];
			var hasHandle1 = !this.handle1.isZero();
			var hasHandle2 = !this.handle2.isZero();
			if (hasHandle1 || hasHandle2) {
				parts.push(
					hasHandle1 ? this.handle1.toSource(false) : null,
					hasHandle2 ? this.handle2.toSource(false) : null
				);
			}
			parts.push(this.point2.toSource(false));
			return 'new Curve(' + parts.join(', ') + ')';
		}
	}
})

Color.inject({
	toSource: function(simple) {
		if (simple) {
			return this.toString();
		} else {
			var comps = this.components;
			if (!this.hasAlpha())
				comps = comps.slice(0, comps.length - 1);
			return 'new ' + this['class'].simpleName + '(' + comps.join(', ') + ')';
		}
	}
})

FillStyle.inject(new function() {
	var fields = ['color', 'overprint'];
	return {
		toSource: function(simple) {
			return Object.toSource(this, simple, fields);
		}
	};
});

StrokeStyle.inject(new function() {
	var fields = ['color', 'overprint', 'width', 'cap', 'join', 'miterLimit',
			'dashOffset', 'dashArray'];
	return {
		toSource: function(simple) {
			return Object.toSource(this, simple, fields);
		}
	};
});

PathStyle.inject(new function() {
	var fields = ['fillColor', 'fillOverprint',
			'strokeColor', 'strokeOverprint', 'strokeWidth',
			'strokeCap', 'strokeJoin', 'miterLimit',
			'dashOffset', 'dashArray', 'windingRule', 'resolution'];
	return {
		toSource: function(simple) {
			return Object.toSource(this, simple, fields);
		}
	};
});

Artboard.inject(new function() {
	var fields = ['bounds', 'showCenter', 'showCrossHairs',
			'showSafeAreas', 'pixelAspectRatio'];
	return {
		toSource: function(simple) {
			return Object.toSource(this, simple, fields);
		}
	};
});

Path.inject({
	toSource: function() {
		var parts = [];
		parts.push('segments: ' + this.segments.toSource(true));
		if (this.closed)
			parts.push('closed: true');
		parts.push('style: ' + this.style.toSource(true));
		return 'new Path() { ' + parts.join(', ') + ' }';
	}
});

Group.inject({
	toSource: function() {
		var parts = [];
		parts.push('children: ' + this.children.toSource(true));
		if (this.clipped)
			parts.push('clipped: true');
		return 'new Group() { ' + parts.join(', ') + ' }';
	}
});

CompoundPath.inject({
	toSource: function() {
		var parts = [];
		parts.push('children: ' + this.children.toSource(true));
		if (this.guide)
			parts.push('guide: true');
		return 'new CompoundPath() { ' + parts.join(', ') + ' }';
	}
});
