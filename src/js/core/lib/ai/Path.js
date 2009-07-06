Path.inject(new function() {
	return ['Line', 'Rectangle', 'RoundRectangle', 'RegularPolygon', 'Star', 'Spiral', 'Oval', 'Circle'].each(function(name) {
		this.statics[name] = function() {
			var args = Array.create(arguments);
			var last = args.last;
			var props = last && last.__proto__ == Object.prototype ? args.pop() : null;
			var res = document['create' + name].apply(document, args);
			if (props)
				for (var i in props)
					res[i] = props[i];
			return res;
		}
	}, {
		statics: {}
	});
});
