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
