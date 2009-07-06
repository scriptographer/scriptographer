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

Segment.inject({
	toSource: function(simple) {
		if (simple) {
			var hasHandleIn = this.handleIn.x != 0 || this.handleIn.y != 0;
			var hasHandleOut = this.handleOut.x != 0 || this.handleOut.y != 0;
			if (!hasHandleIn && !hasHandleOut)
				return this.point.toSource(true);
			var parts = [this.point.toSource(true)];
			if (hasHandleIn || hasHandleOut)
				parts.push(hasHandleIn ? this.handleIn.toSource(true) : 'null');
			if (hasHandleOut)
				parts.push(this.handleOut.toSource(true));
			if (this.corner)
				parts.push('true');
			return '[' + parts.join(', ') + ']';
		} else {
			var parts = ['new Segment(', this.point.toSource(false)];
			parts.push(')');
			return parts.join('');
		}
	}
});

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
