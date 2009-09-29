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
		var func = tool[handler];
		if (!func || !func._installed) {
			tool.inject(Hash.create(handler, function(event) {
				if (name == 'up')
					sets.drag = {};
				removeAll(sets[name]);
				sets[name] = {};
				if(this.base)
					this.base(event);
			}));
			tool[handler]._installed = true;
		}
	}

	Item.inject(['up', 'down', 'drag'].each(function(name) {
		this['removeOn' + name.capitalize()] = function() {
			return this.removeOn(Hash.create(name, true));
		};
	}, {
		removeOn: function(obj) {
			for (var name in obj) {
				if (obj[name]) {
					sets[name][this.id] = this;
					installHandler('up');
					installHandler(name);
				}
			}
			return this;
		}
	}));
}
