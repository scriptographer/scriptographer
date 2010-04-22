new function() { 
	function has(obj, name) {
		return obj.hasOwnProperty(name);
	}

	function define(obj, name, desc) {
		try {
			return Object.defineProperty(obj, name, desc);
		} catch (e) {
			obj[name] = desc.value;
		}
	}

	function describe(obj, name) {
		try {
			return Object.getOwnPropertyDescriptor(obj, name);
		} catch (e) {
			return has(obj, name)
				? { enumerable: true, configurable: true, writable: true, value: obj[name] }
				: null;
		}
	}

	function inject(dest, src, enumerable, base, generics) {
		function field(name, val, dontCheck, generics) {
			if (!val)
				val = (val = describe(src, name)) && (val.get ? val : val.value);
			var type = typeof val, func = type == 'function', res = val,
				prev = dest[name], bean;
			if (generics && func && (!src.preserve || !generics[name])) generics[name] = function(bind) {
				return bind && dest[name].apply(bind,
					Array.prototype.slice.call(arguments, 1));
			}
			if ((dontCheck || val !== undefined && has(src, name)) && (!prev || !src.preserve)) {
				if (func) {
					if (prev && /\bthis\.base\b/.test(val)) {
						var fromBase = base && base[name] == prev;
						res = (function() {
							var tmp = this.base;
							define(this, 'base', { value: fromBase ? base[name] : prev, configurable: true });
							try { return val.apply(this, arguments); }
							finally { this.base = tmp; }
						}).pretend(val);
					}
					if ((src.beans || src._beans) && (bean = name.match(/^(get|is)(([A-Z])(.*))$/)))
						try {
							field(bean[3].toLowerCase() + bean[4], {
								get: src['get' + bean[2]] || src['is' + bean[2]],
								set: src['set' + bean[2]]
							}, true);
						} catch (e) {}
				}
				if (!res || func || !res.get && !res.set)
					res = { value: res, writable: true };
				if ((describe(dest, name) || { configurable: true }).configurable) {
					res.configurable = true;
					res.enumerable = enumerable;
				}
				define(dest, name, res);
			}
		}
		if (src) {
			for (var name in src)
				if (has(src, name) && !/^(statics|generics|preserve|_?beans|prototype)$/.test(name))
					field(name, null, true, generics);
		}
	}

	function extend(obj) {
		function ctor(dont) {
			if (this.initialize && dont !== ctor.dont)
				return this.initialize.apply(this, arguments);
		}
		ctor.prototype = obj;
		ctor.toString = function() {
			return (this.prototype.initialize || function() {}).toString();
		}
		return ctor;
	}

	inject(Function.prototype, {
		inject: function(src) {
			if (src) {
				var proto = this.prototype, base = proto.__proto__ && proto.__proto__.constructor;
				inject(proto, src, false, base && base.prototype, src.generics && this);
				inject(this, src.statics, true, base);
			}
			for (var i = 1, l = arguments.length; i < l; i++)
				this.inject(arguments[i]);
			return this;
		},

		extend: function(src) {
			var proto = new this(this.dont), ctor = extend(proto);
			define(proto, 'constructor', { value: ctor, writable: true, configurable: true });
			ctor.dont = {};
			inject(ctor, this, true);
			return arguments.length ? this.inject.apply(ctor, arguments) : ctor;
		},

		pretend: function(fn) {
			this.toString = function() {
				return fn.toString();
			}
			this.valueOf = function() {
				return fn.valueOf();
			}
			return this;
		}
	});

	function each(obj, iter, bind) {
		return obj ? (typeof obj.length == 'number'
			? Array : Hash).prototype.each.call(obj, iter, bind) : bind;
	}

	Base = Object.inject({
		has: function(name) {
			return has(this, name);
		},

		each: function(iter, bind) {
			return each(this, iter, bind);
		},

		inject: function() {
			for (var i = 0, l = arguments.length; i < l; i++)
				inject(this, arguments[i]);
			return this;
		},

		extend: function() {
			var res = new (extend(this));
			return res.inject.apply(res, arguments);
		},

		statics: {
			has: has,
			each: each,
			define: define,
			describe: describe,

			type: function(obj) {
				return (obj || obj === 0) && (obj._type
					|| (obj instanceof java.lang.Object
						&& !(obj instanceof org.mozilla.javascript.Scriptable)
						? 'java' : typeof obj)) || null;
			},

			check: function(obj) {
				return !!(obj || obj === 0);
			},

			pick: function() {
				for (var i = 0, l = arguments.length; i < l; i++)
					if (arguments[i] !== undefined)
						return arguments[i];
				return null;
			},

			iterator: function(iter) {
				return !iter
					? function(val) { return val }
					: typeof iter != 'function'
						? function(val) { return val == iter }
						: iter;
			},

			stop: {}
		}
	}, {
		generics: true,

		debug: function() {
			return /^(string|number|function|regexp)$/.test(Base.type(this)) ? this
				: Base.each(this, function(val, key) { this.push(key + ': ' + val); }, []).join(', ');
		},

		clone: function() {
			return Base.each(this, function(val, i) {
				this[i] = val;
			}, new this.constructor());
		},

		toQueryString: function() {
			return Base.each(this, function(val, key) {
				this.push(key + '=' + escape(val));
			}, []).join('&');
		}
	});
}

$each = Base.each;
$type = Base.type;
$check = Base.check;
$pick = Base.pick;
$stop = $break = Base.stop;

Enumerable = {
	generics: true,
	preserve: true,

	findEntry: function(iter, bind) {
		var that = this, iter = Base.iterator(iter), ret = null;
		Base.each(this, function(val, key) {
			var res = iter.call(bind, val, key, that);
			if (res) {
				ret = { key: key, value: val, result: res };
				throw Base.stop;
			}
		});
		return ret;
	},

	find: function(iter, bind) {
		var entry = this.findEntry(iter, bind);
		return entry && entry.result;
	},

	contains: function(obj) {
		return !!this.findEntry(obj);
	},

	remove: function(iter, bind) {
		var entry = this.findEntry(iter, bind);
		if (entry) {
			delete this[entry.key];
			return entry.value;
		}
	},

	filter: function(iter, bind) {
		var that = this;
		return Base.each(this, function(val, i) {
			if (iter.call(bind, val, i, that))
				this[this.length] = val;
		}, []);
	},

	map: function(iter, bind) {
		var that = this;
		return Base.each(this, function(val, i) {
			this[this.length] = iter.call(bind, val, i, that);
		}, []);
	},

	every: function(iter, bind) {
		var that = this;
		return this.find(function(val, i) {
			return !iter.call(this, val, i, that);
		}, bind || null) == null;
	},

	some: function(iter, bind) {
		return this.find(iter, bind || null) != null;
	},

	collect: function(iter, bind) {
		var that = this, iter = Base.iterator(iter);
		return Base.each(this, function(val, i) {
		 	val = iter.call(bind, val, i, that);
			if (val != null)
				this[this.length] = val;
		}, []);
	},

	max: function(iter, bind) {
		var that = this;
		return Base.each(this, function(val, i) {
			val = iter.call(bind, val, i, that);
			if (val >= (this.max || val)) this.max = val;
		}, {}).max;
	},

	min: function(iter, bind) {
		var that = this;
		return Base.each(this, function(val, i) {
			val = iter.call(bind, val, i, that);
			if (val <= (this.min || val)) this.min = val;
		}, {}).min;
	},

	pluck: function(prop) {
		return this.map(function(val) {
			return val[prop];
		});
	},

	sortBy: function(iter, bind) {
		var that = this;
		return this.map(function(val, i) {
			return { value: val, compare: iter.call(bind, val, i, that) };
		}, bind).sort(function(left, right) {
			var a = left.compare, b = right.compare;
			return a < b ? -1 : a > b ? 1 : 0;
		}).pluck('value');
	},

	toArray: function() {
		return this.map();
	}
};

Hash = Base.extend(Enumerable, {
	generics: true,

	initialize: function(arg) {
		if (typeof arg == 'string') {
			for (var i = 0, l = arguments.length; i < l; i += 2)
				this[arguments[i]] = arguments[i + 1];
		} else {
			this[arguments.length == 1 ? 'append' : 'merge'].apply(this, arguments);
		}
		return this;
	},

	each: function(iter, bind) {
		if (!bind) bind = this;
		iter = Base.iterator(iter);
		try {
			for (var i in this)
				if (this.hasOwnProperty(i))
					iter.call(bind, this[i], i, this);
		} catch (e) {
			if (e !== Base.stop) throw e;
		}
		return bind;
	},

	append: function() {
		for (var i = 0, l = arguments.length; i < l; i++) {
			var obj = arguments[i];
			for (var key in obj)
				if (Base.has(obj, key))
					this[key] = obj[key];
		}
		return this;
	},

	merge: function() {
		return Array.each(arguments, function(obj) {
			Base.each(obj, function(val, key) {
				this[key] = Base.type(this[key]) == 'object'
					? Hash.prototype.merge.call(this[key], val)
					: Base.type(val) == 'object' ? Base.clone(val) : val;
			}, this);
		}, this);
	},

	getKeys: function() {
		return Hash.getKeys(this);
	},

	getValues: Enumerable.toArray,

	getSize: function() {
		return this.each(function() {
			this.size++;
		}, { size: 0 }).size;
	},

	statics: {
		create: function(obj) {
			return arguments.length == 1 && obj.constructor == Hash
				? obj : Hash.prototype.initialize.apply(new Hash(), arguments);
		},

		getKeys: Object.keys || function(obj) {
			return Hash.map(function(val, key) {
				return key;
			});
		}
	}
});

$H = Hash.create;

Array.inject(Enumerable, {
	generics: true,
	beans: true,
	_type: 'array',

	each: function(iter, bind) {
		try {
			Array.prototype.forEach.call(this, Base.iterator(iter), bind = bind || this);
		} catch (e) {
			if (e !== Base.stop) throw e;
		}
		return bind;
	},

	collect: function(iter, bind) {
		var that = this;
		return this.each(function(val, i) {
			if ((val = iter.call(bind, val, i, that)) != null)
				this[this.length] = val;
		}, []);
	},

	findEntry: function(iter, bind) {
		if (typeof iter != 'function') {
			var i = this.indexOf(iter);
			return i == -1 ? null : { key: i, value: iter, result: iter };
		}
		return Enumerable.findEntry.call(this, iter, bind);
	},

	remove: function(iter, bind) {
		var entry = this.findEntry(iter, bind);
		if (entry.key != null)
			this.splice(entry.key, 1);
		return entry.value;
	},

	contains: function(obj) {
		return this.indexOf(obj) != -1;
	},

	remove: function(iter, bind) {
		var entry = this.findEntry(iter, bind);
		if (entry) {
			this.splice(entry.key, 1);
			return entry.value;
		}
	},

	toArray: function() {
		return this.concat([]);
	},

	clone: function() {
		return this.toArray();
	},

	clear: function() {
		this.length = 0;
	},

	compact: function() {
		return this.filter(function(value) {
			return value != null;
		});
	},

	append: function(items) {
		for (var i = 0, l = items.length; i < l; i++)
			this[this.length++] = items[i];
		return this;
	},

	subtract: function(items) {
		for (var i = 0, l = items.length; i < l; i++)
			Array.remove(this, items[i]);
		return this;
	},

	intersect: function(items) {
		for (var i = this.length - 1; i >= 0; i--)
			if (!items.find(this[i]))
				this.splice(i, 1);
		return this;
	},

	associate: function(obj) {
		if (!obj)
			obj = this;
		else if (typeof obj == 'function')
			obj = this.map(obj);
		if (obj.length != null) {
			var that = this;
			return Base.each(obj, function(name, index) {
				this[name] = that[index];
				if (index == that.length)
					throw Base.stop;
			}, {});
		} else {
			obj = Hash.append({}, obj);
			return Array.each(this, function(val) {
				var type = Base.type(val);
				Base.each(obj, function(hint, name) {
					if (hint == 'any' || type == hint) {
						this[name] = val;
						delete obj[name];
						throw Base.stop;
					}
				}, this);
			}, {});
		}
	},

	flatten: function() {
		return Array.each(this, function(val) {
			if (val != null && val.flatten) this.append(val.flatten());
			else this.push(val);
		}, []);
	},

	swap: function(i, j) {
		var tmp = this[j];
		this[j] = this[i];
		this[i] = tmp;
		return tmp;
	},

	shuffle: function() {
		var res = this.clone();
		var i = this.length;
		while (i--) res.swap(i, Math.rand(i + 1));
		return res;
	},

	pick: function() {
		return this[Math.rand(this.length)];
	},

	getFirst: function() {
		return this[0];
	},

	getLast: function() {
		return this[this.length - 1];
	}
});

Array.inject(new function() {
	var proto = Array.prototype;

	var fields = ['push','pop','shift','unshift','sort','reverse','join','slice','splice','forEach',
		'indexOf','lastIndexOf','filter','map','every','some','reduce','concat'].each(function(name) {
		this[name] = proto[name];
	}, { generics: true, preserve: true });

	Array.inject(fields);

	Hash.append(fields, proto, {
		clear: function() {
			for (var i = 0, l = this.length; i < l; i++)
				delete this[i];
			this.length = 0;
		},

		toString: proto.join,

		length: 0
	});

	return {
		statics: {
			create: function(list) {
				if (!Base.check(list)) return [];
				if (Base.type(list) == 'array') return list;
				if (list.toArray)
					return list.toArray();
				if (list.length != null) {
					var res = [];
					for (var i = 0, l = list.length; i < l; i++)
						res[i] = list[i];
				} else {
					res = [list];
				}
				return res;
			},

			extend: function(src) {
				var ret = Base.extend(fields, src);
				ret.extend = Function.extend;
				return ret;
			}
		}
	};
});

$A = Array.create;

Function.inject(new function() {
	return {
		generics: true,

		bind: function(bind, args) {
			var that = this;
			return function() {
				return that.apply(bind, args || arguments);
			}
		},

		attempt: function(bind, args) {
			var that = this;
			return function() {
				try {
					return that.apply(bind, args || arguments);
				} catch (e) {
					return e;
				}
			}
		}
	}
});

Number.inject({
	_type: 'number',

	limit: function(min, max) {
		return Math.min(max, Math.max(min, this));
	},

	times: function(func, bind) {
		for (var i = 0; i < this; i++)
			func.call(bind, i);
		return bind || this;
	},

	toInt: function(base) {
		return parseInt(this, base || 10);
	},

	toFloat: function() {
		return parseFloat(this);
	},

	toPaddedString: function(length, base, prefix) {
		var str = this.toString(base || 10);
		return (prefix || '0').times(length - str.length) + str;
	}
});

String.inject({
	_type: 'string',

	test: function(exp, param) {
		return new RegExp(exp, param || '').test(this);
	},

	toArray: function() {
		return this ? this.split(/\s+/) : [];
	},

	toInt: Number.prototype.toInt,

	toFloat: Number.prototype.toFloat,

	camelize: function(separator) {
		return this.replace(separator ? new RegExp('[' + separator + '](\\w)', 'g') : /-(\w)/g, function(all, chr) {
			return chr.toUpperCase();
		});
	},

	uncamelize: function(separator) {
		separator = separator || ' ';
		return this.replace(/[a-z][A-Z0-9]|[0-9][a-zA-Z]|[A-Z]{2}[a-z]/g, function(match) {
			return match.charAt(0) + separator + match.substring(1);
		});
	},

	hyphenate: function(separator) {
		return this.uncamelize(separator || '-').toLowerCase();
	},

	capitalize: function() {
		return this.replace(/\b[a-z]/g, function(match) {
			return match.toUpperCase();
		});
	},

	escapeRegExp: function() {
		return this.replace(/([-.*+?^${}()|[\]\/\\])/g, '\\$1');
	},

	trim: function(exp) {
		exp = exp ? '[' + exp + ']' : '\\s';
		return this.replace(new RegExp('^' + exp + '+|' + exp + '+$', 'g'), '');
	},

	clean: function() {
		return this.replace(/\s{2,}/g, ' ').trim();
	},

	contains: function(string, sep) {
		return (sep ? (sep + this + sep).indexOf(sep + string + sep) : this.indexOf(string)) != -1;
	},

	times: function(count) {
		return count < 1 ? '' : new Array(count + 1).join(this);
	},

	isHtml: function() {
		return /^[^<]*(<(.|\s)+>)[^>]*$/.test(this);
	}
});

RegExp.inject({
	_type: 'regexp'
});

Date.inject({
	statics: {
		SECOND: 1000,
		MINUTE: 60000,
		HOUR: 3600000,
		DAY: 86400000,
		WEEK: 604800000, 
		MONTH: 2592000000, 
		YEAR: 31536000000 
	}
});

Math.rand = function(first, second) {
	return second == undefined
		? Math.rand(0, first)
		: Math.floor(Math.random() * (second - first) + first);
}

Array.inject({
	hexToRgb: function(toArray) {
		if (this.length >= 3) {
			var rgb = [];
			for (var i = 0; i < 3; i++)
				rgb.push((this[i].length == 1 ? this[i] + this[i] : this[i]).toInt(16));
			return toArray ? rgb : 'rgb(' + rgb.join(',') + ')';
		}
	},

	rgbToHex: function(toArray) {
		if (this.length >= 3) {
			if (this.length == 4 && this[3] == 0 && !toArray) return 'transparent';
			var hex = [];
			for (var i = 0; i < 3; i++) {
				var bit = (this[i] - 0).toString(16);
				hex.push(bit.length == 1 ? '0' + bit : bit);
			}
			return toArray ? hex : '#' + hex.join('');
		}
	},

	rgbToHsb: function() {
		var r = this[0], g = this[1], b = this[2];
		var hue, saturation, brightness;
		var max = Math.max(r, g, b), min = Math.min(r, g, b);
		var delta = max - min;
		brightness = max / 255;
		saturation = (max != 0) ? delta / max : 0;
		if (saturation == 0) {
			hue = 0;
		} else {
			var rr = (max - r) / delta;
			var gr = (max - g) / delta;
			var br = (max - b) / delta;
			if (r == max) hue = br - gr;
			else if (g == max) hue = 2 + rr - br;
			else hue = 4 + gr - rr;
			hue /= 6;
			if (hue < 0) hue++;
		}
		return [Math.round(hue * 360), Math.round(saturation * 100), Math.round(brightness * 100)];
	},

	hsbToRgb: function() {
		var br = Math.round(this[2] / 100 * 255);
		if (this[1] == 0) {
			return [br, br, br];
		} else {
			var hue = this[0] % 360;
			var f = hue % 60;
			var p = Math.round((this[2] * (100 - this[1])) / 10000 * 255);
			var q = Math.round((this[2] * (6000 - this[1] * f)) / 600000 * 255);
			var t = Math.round((this[2] * (6000 - this[1] * (60 - f))) / 600000 * 255);
			switch (Math.floor(hue / 60)) {
				case 0: return [br, t, p];
				case 1: return [q, br, p];
				case 2: return [p, br, t];
				case 3: return [p, q, br];
				case 4: return [t, p, br];
				case 5: return [br, p, q];
			}
		}
	}
});

String.inject({
	hexToRgb: function(toArray) {
		var hex = this.match(/^#?(\w{1,2})(\w{1,2})(\w{1,2})$/);
		return hex && hex.slice(1).hexToRgb(toArray);
	},

	rgbToHex: function(toArray) {
		var rgb = this.match(/\d{1,3}/g);
		return rgb && rgb.rgbToHex(toArray);
	}
});

Json = {
	encode: function(obj, properties) {
		return Base.type(obj) != 'java' ? JSON.stringify(obj, properties) : null;
	},

	decode: function(str, secure) {
		try {
			return JSON.parse(str);
		} catch (e) {
			return null;
		}
	}
};

