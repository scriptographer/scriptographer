Path.inject(new function() {
	return ['Line', 'Rectangle', 'RoundRectangle', 'RegularPolygon',
			'Star', 'Spiral', 'Oval', 'Circle', 'Arc'].each(function(name) {
		this.statics[name] = function() {
			if (!document)
				throw 'Unable to create item. There is no document.';
			var args = Array.create(arguments);
			var last = args.last;
			var props = last && last.__proto__ == Object.prototype
					? args.pop() : null;
			var res = document['create' + name].apply(document, args);
			if (res && props)
				for (var i in props)
					res[i] = props[i];
			return res;
		}
	}, {
		statics: {}
	});
});
