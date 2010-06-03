// Temporary Item Removal by Jonathan Puckey

new function() {
	var sets = {
		down: {}, drag: {}, up: {}
	};

	function removeAll(set) {
		for(var id in set) {
			var item = set[id];
			if(item.isValid())
				item.remove();
			for(var type in sets) {
				var other = sets[type];
				if(other != set && other[item.id])
					delete other[item.id];
			}
		}
	}

	function installHandler(name) {
		var handler = 'onMouse' + name.capitalize();
		// Inject a onMouse handler that performs all the behind the scene magic
		// and calls the script's handler at the end, if defined.
		var func = tool[handler];
		if (!func || !func._installed) {
			tool.inject(new Hash(handler, function(event) {
				// Always clear the drag set on mouse-up
				if (name == 'up')
					sets.drag = {};
				removeAll(sets[name]);
				sets[name] = {};
				// Call the script's overridden handler, if defined
				if(this.base)
					this.base(event);
			}));
			// Only install this handler once, and mark it as installed,
			// to prevent repeated installing.
			tool[handler]._installed = true;
		}
	}

	Item.inject(['up', 'down', 'drag'].each(function(name) {
		this['removeOn' + name.capitalize()] = function() {
			return this.removeOn(new Hash(name, true));
		};
	}, {
		removeOn: function(obj) {
			for (var name in obj) {
				if (obj[name]) {
					sets[name][this.id] = this;
					// Since the drag set gets cleared in up, we need to make
					// sure it's installed too
					if (name == 'drag')
						installHandler('up');
					installHandler(name);
				}
			}
			return this;
		}
	}));
}
