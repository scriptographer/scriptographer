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
			if (obj === null) return 'null';
			else if (obj === undefined) return 'undefined';
			else if (fields) {
				// Used by StrokeStyle and FillStyle
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
					return 'new ' + obj.getClass().simpleName + '(' + parts.join(', ') + ')';
				}
			} else {
				var type = typeof obj;
				if (type == 'boolean' || type == 'string' || type == 'number')
					return uneval(obj);
				else if (obj.toSource)
					return obj.toSource(simple);
				else
					return uneval(obj);
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
			if (hasHandleIn)
				parts.push(this.handleIn.toSource(true));
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
			return 'new ' + this.getClass().simpleName + '(' + comps.join(', ') + ')';
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
	var fields = ['color', 'overprint', 'width', 'dashOffset', 'dashArray', 'cap', 'join', 'miterLimit'];
	return {
		toSource: function(simple) {
			return Object.toSource(this, simple, fields);
		}
	};
});

PathStyle.inject({
	toSource: function(simple) {
		var parts = [];
		parts.push('fill: ' + Object.toSource(this.fill, true));
		parts.push('stroke: ' + Object.toSource(this.stroke, true));
		return simple ? '{ ' + parts.join(', ') + ' }' : 'new PathStyle() { ' + parts.join(', ') + ' }';
	}
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
