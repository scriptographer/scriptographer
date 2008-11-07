new function() { 
	function inject(dest, src, base, generics) {
		function field(name, val, generics) {
			if (!val) {
				var get = src.__lookupGetter__(name);
				val = get ? { _get: get, _set: src.__lookupSetter__(name) } : src[name];
			}
			var type = typeof val, func = type == 'function', res = val, prev, beans;
			if (generics && func) generics[name] = function(bind) {
				return bind && dest[name].apply(bind,
					Array.prototype.slice.call(arguments, 1));
			}
			if (val !== (src.__proto__ || Object.prototype)[name]) {
				if (func) {
					if (/\[native code/.test(val))
						return;
					if ((prev = dest[name]) && /\bthis\.base\b/.test(val)) {
						var fromBase = base && base[name] == prev;
						res = (function() {
							var tmp = this.base;
							this.base = fromBase ? base[name] : prev;
							try { return val.apply(this, arguments); }
							finally { this.base = tmp; }
						}).pretend(val);
					}
					if (src._beans && (bean = name.match(/^(get|is)(([A-Z])(.*))$/)))
						field(bean[3].toLowerCase() + bean[4], {
							_get: src['get' + bean[2]] || src['is' + bean[2]],
							_set: src['set' + bean[2]]
						});
				}
				if (type == 'object' && (val._get || val._set)) {
					if (val._get)
						dest.__defineGetter__(name, val._get);
					if (val._set)
						dest.__defineSetter__(name, val._set);
				} else {
					dest[name] = res;
				}
			}
		}
		if (src) {
			for (var name in src)
				if (visible(src, name) && !/^(prototype|constructor|toString|valueOf|statics|_generics|_beans)$/.test(name))
					field(name, null, generics);
			field('toString');
			field('valueOf');
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

	function visible(obj, name) {
		return !obj.__lookupGetter__(name) && obj[name] !== obj.__proto__[name];
	}

	inject(Function.prototype, {
		inject: function(src) {
			var proto = this.prototype, base = proto.__proto__ && proto.__proto__.constructor;
			inject(proto, src, base && base.prototype, src && src._generics && this);
			inject(this, src && src.statics, base);
			for (var i = 1, l = arguments.length; i < l; i++)
				this.inject(arguments[i]);
			return this;
		},

		extend: function(src) {
			var proto = new this(this.dont), ctor = proto.constructor = extend(proto);
			ctor.dont = {};
			inject(ctor, this);
			return this.inject.apply(ctor, arguments);
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

	Base = Object.inject({
		has: function(name) {
			return visible(this, name);
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
			has: visible
		}
	});

}

Function.inject(new function() {

	return {
		_beans: true,
		_generics: true,

		getName: function() {
			var match = this.toString().match(/^\s*function\s*(\w*)/);
			return match && match[1];
		},

		getParameters: function() {
			var str = this.toString().match(/^\s*function[^\(]*\(([^\)]*)/)[1];
			return str ? str.split(/\s*,\s*/) : [];
		},

		getBody: function() {
			return this.toString().match(/^\s*function[^\{]*\{([\u0000-\uffff]*)\}\s*$/)[1];
		},

		bind: function(bind, args) {
			var that = this;
			return function() {
				return that.apply(bind, args && args.concat(Array.create(arguments)) || arguments);
			}
		},

		attempt: function(bind, args) {
			var that = this;
			return function() {
				try {
					return that.apply(bind, args && args.concat(Array.create(arguments)) || arguments);
				} catch (e) {
					return e;
				}
			}
		}
	}
});

Enumerable = new function() {
	Base.iterate = function(fn) {
		return function(iter, bind) {
			if (!iter) iter = function(val) { return val };
			else if (typeof iter != 'function') iter = function(val) { return val == iter };
			if (!bind) bind = this;
			return fn.call(this, iter, bind, this);
		};
	};

	Base.stop = {};

	var each_Array = Array.prototype.forEach || function(iter, bind) {
		for (var i = 0, l = this.length; i < l; ++i)
			iter.call(bind, this[i], i, this);
	};

	var each_Object = function(iter, bind) {
		for (var i in this)
			if (!this.__lookupGetter__(i) && this[i] !== this.__proto__[i])
				iter.call(bind, this[i], i, this);
	};

	return {
		_beans: true,
		_generics: true,

		each: Base.iterate(function(iter, bind) {
			try { (typeof this.length == 'number' ? each_Array : each_Object).call(this, iter, bind); }
			catch (e) { if (e !== Base.stop) throw e; }
			return bind;
		}),

		findEntry: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, key) {
				this.result = iter.call(bind, val, key, that);
				if (this.result) {
					this.key = key;
					this.value = val;
					throw Base.stop;
				}
			}, {});
		}),

		find: function(iter, bind) {
			return this.findEntry(iter, bind).result;
		},

		remove: function(iter, bind) {
			var entry = this.findEntry(iter, bind);
			delete this[entry.key];
			return entry.value;
		},

		some: function(iter, bind) {
			return this.find(iter, bind) != null;
		},

		every: Base.iterate(function(iter, bind, that) {
			return this.find(function(val, i) {
				return !iter.call(this, val, i, that);
			}, bind) == null;
		}),

		map: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, i) {
				this[this.length] = iter.call(bind, val, i, that);
			}, []);
		}),

		collect: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, i) {
			 	val = iter.call(bind, val, i, that);
				if (val != null) this[this.length] = val;
			}, []);
		}),

		filter: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, i) {
				if (iter.call(bind, val, i, that))
					this[this.length] = val;
			}, []);
		}),

		max: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, i) {
				val = iter.call(bind, val, i, that);
				if (val >= (this.max || val)) this.max = val;
			}, {}).max;
		}),

		min: Base.iterate(function(iter, bind, that) {
			return Base.each(this, function(val, i) {
				val = iter.call(bind, val, i, that);
				if (val <= (this.min || val)) this.min = val;
			}, {}).min;
		}),

		pluck: function(prop) {
			return this.map(function(val) {
				return val[prop];
			});
		},

		sortBy: Base.iterate(function(iter, bind, that) {
			return this.map(function(val, i) {
				return { value: val, compare: iter.call(bind, val, i, that) };
			}, bind).sort(function(left, right) {
				var a = left.compare, b = right.compare;
				return a < b ? -1 : a > b ? 1 : 0;
			}).pluck('value');
		}),

		toArray: function() {
			return this.map();
		}
	};
}

Base.inject({
	_generics: true,

	each: Enumerable.each,

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
	},

	statics: {

		check: function(obj) {
			return !!(obj || obj === 0);
		},

		type: function(obj) {
			return (obj || obj === 0) && (obj._type || typeof obj) || null;
		},

		pick: function() {
			for (var i = 0, l = arguments.length; i < l; i++)
				if (arguments[i] !== undefined)
					return arguments[i];
			return null;
		}
	}
});

$each = Base.each;
$stop = $break = Base.stop;
$check = Base.check;
$type = Base.type;

Hash = Base.extend(Enumerable, {
	_beans: true,
	_generics: true,

	initialize: function() {
		return this.merge.apply(this, arguments);
	},

	merge: function() {
		return Base.each(arguments, function(obj) {
			Base.each(obj, function(val, key) {
				this[key] = Base.type(this[key]) == 'object'
					? Hash.prototype.merge.call(this[key], val) : val;
			}, this);
		}, this);
	},

	getKeys: function() {
		return this.map(function(val, key) {
			return key;
		});
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
		}
	}
});

$H = Hash.create;

Array.inject(new function() {
	var proto = Array.prototype;

	var fields = Hash.merge({}, Enumerable, {
		_generics: true,
		_type: 'array',

		indexOf: proto.indexOf || function(obj, i) {
			i = i || 0;
			if (i < 0) i = Math.max(0, this.length + i);
			for (var l = this.length; i < l; ++i)
				if (this[i] == obj) return i;
			return -1;
		},

		lastIndexOf: proto.lastIndexOf || function(obj, i) {
			i = i != null ? i : this.length - 1;
			if (i < 0) i = Math.max(0, this.length + i);
			for (; i >= 0; i--)
				if (this[i] == obj) return i;
			return -1;
		},

		filter: Base.iterate(proto.filter || function(iter, bind, that) {
			var res = [];
			for (var i = 0, l = this.length; i < l; ++i)
				if (iter.call(bind, this[i], i, that))
					res[res.length] = this[i];
			return res;
		}),

		map: Base.iterate(proto.map || function(iter, bind, that) {
			var res = new Array(this.length);
			for (var i = 0, l = this.length; i < l; ++i)
				res[i] = iter.call(bind, this[i], i, that);
			return res;
		}),

		every: Base.iterate(proto.every || function(iter, bind, that) {
			for (var i = 0, l = this.length; i < l; ++i)
				if (!iter.call(bind, this[i], i, that))
					return false;
			return true;
		}),

		some: Base.iterate(proto.some || function(iter, bind, that) {
			for (var i = 0, l = this.length; i < l; ++i)
				if (iter.call(bind, this[i], i, that))
					return true;
			return false;
		}),

		reduce: proto.reduce || function(fn, value) {
			var i = 0;
			if (arguments.length < 2 && this.length) value = this[i++];
			for (var l = this.length; i < l; i++)
				value = fn.call(null, value, this[i], i, this);
			return value;
		},

		findEntry: function(iter, bind) {
			if (iter && !/^(function|regexp)$/.test(Base.type(iter))) {
				var i = this.indexOf(iter);
				return { key: i != -1 ? i : null, value: this[i], result: i != -1 };
			}
			return Enumerable.findEntry.call(this, iter, bind);
		},

		remove: function(iter, bind) {
			var entry = this.findEntry(iter, bind);
			if (entry.key != null)
				this.splice(entry.key, 1);
			return entry.value;
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

		getFirst: function() {
			return this[0];
		},

		getLast: function() {
			return this[this.length - 1];
		},

		compact: function() {
			return this.filter(function(value) {
				return value != null;
			});
		},

		append: function(items) {
			for (var i = 0, l = items.length; i < l; ++i)
				this[this.length++] = items[i];
			return this;
		},

		subtract: function(items) {
			for (var i = 0, l = items.length; i < l; ++i)
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
					if (index == that.length) throw Base.stop;
				}, {});
			} else {
				obj = Hash.merge({}, obj);
				return Base.each(this, function(val) {
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
			while (i--) res.swap(i, Math.rand(0, i + 1));
			return res;
		},

		statics: {
			create: function(list) {
				if (!Base.check(list)) return [];
				if (Base.type(list) == 'array') return list;
				if (list.toArray)
					return list.toArray();
				if (list.length != null) {
					var res = [];
					for (var i = 0, l = list.length; i < l; ++i)
						res[i] = list[i];
				} else {
					res = [list];
				}
				return res;
			},

			extend: function(src) {
				var ret = Base.extend(extend, src);
				ret.extend = Function.extend;
				return ret;
			}
		}
	});
	['push','pop','shift','unshift','sort','reverse','join','slice','splice','concat'].each(function(name) {
		fields[name] = proto[name];
	});
	var extend = Hash.merge({}, fields, {
		clear: function() {
			for (var i = 0, l = this.length; i < l; i++)
				delete this[i];
			this.length = 0;
		},

		toString: proto.join
	});
	extend.length = 0;
	return fields;
});

$A = Array.create;

String.inject({
	_beans: true,
	_type: 'string',

	test: function(exp, param) {
		return new RegExp(exp, param || '').test(this);
	},

	toArray: function() {
		return this ? this.split(/\s+/) : [];
	},

	toInt: function(base) {
		return parseInt(this, base || 10);
	},

	toFloat: function() {
		return parseFloat(this);
	},

	camelize: function(separator) {
		return this.replace(new RegExp(separator || '\s-', 'g'), function(match) {
			return match.charAt(1).toUpperCase();
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

	contains: function(string, s) {
		return (s ? (s + this + s).indexOf(s + string + s) : this.indexOf(string)) != -1;
	},

	times: function(count) {
		return count < 1 ? '' : new Array(count + 1).join(this);
	},

	isHtml: function() {
		return /^[^<]*(<(.|\s)+>)[^>]*$/.test(this);
	}
});

Number.inject({
	_type: 'number',

	toInt: String.prototype.toInt,

	toFloat: String.prototype.toFloat,

	times: function(func, bind) {
		for (var i = 0; i < this; ++i) func.call(bind, i);
		return bind || this;
	},

	toPaddedString: function(length, base, prefix) {
		var str = this.toString(base || 10);
		return (prefix || '0').times(length - str.length) + str;
	}
});

RegExp.inject({
	_type: 'regexp'
});

Math.rand = function(min, max) {
	return Math.floor(Math.random() * (max - min) + min);
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

Json = new function() {
	var special = { '\b': '\\b', '\t': '\\t', '\n': '\\n', '\f': '\\f', '\r': '\\r', '"' : '\\"', '\\': '\\\\' };

	function replace(chr) {
		return special[chr] || '\\u00' + Math.floor(chr.charCodeAt() / 16).toString(16) + (chr.charCodeAt() % 16).toString(16);
	}

	return {
		encode: function(obj) {
			switch (Base.type(obj)) {
				case 'string':
					return uneval(obj.toString());
				case 'array':
					return '[' + obj.collect(Json.encode) + ']';
				case 'object':
				case 'hash':
					return '{' + Hash.collect(obj, function(val, key) {
						val = Json.encode(val);
						if (val) return Json.encode(key) + ':' + val;
					}) + '}';
				default:
					return obj + '';
			}
			return null;
		},

		decode: function(str, secure) {
			try {
				return (Base.type(str) != 'string' || !str.length) ||
					(secure && !/^[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t]*$/.test(
						str.replace(/\\./g, '@').replace(/"[^"\\\n\r]*"/g, '')))
							? null : eval('(' + str + ')');
			} catch (e) {
				return null;
			}
		}
	};
};

