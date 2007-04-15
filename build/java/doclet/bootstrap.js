(function() { 
	function inject(dest, src, base, hide) {
		if (src) for (var name in src)
			if (!src.has || src.has(name)) (function(val, name) {
				var res = val, baseVal = base && base[name];
				if (typeof val == 'function' && baseVal && val !== baseVal &&
					/\$super\b/.test(val)) {
					res = function() {
						var prev = this.$super;
						this.$super = base != dest ? base[name] : baseVal;
						try { return val.apply(this, arguments); }
						finally { this.$super = prev; }
					};
					res.toString = function() {
						return val.toString();
					};
				}
				dest[name] = res;
				if (hide && dest.dontEnum != null)
					dest.dontEnum(name);
			})(src[name], name);
		return dest;
	}

	function extend(obj) {
		var ctor = function() {
			if (this.$constructor && arguments[0] !== ctor.dont)
				return this.$constructor.apply(this, arguments);
		};
		ctor.prototype = obj;
		return ctor;
	}

	Function.prototype.inject = function(src, hide, base) {
		src = typeof src == 'function' ? src() : src;
		inject(this.prototype, src, base ? base.prototype : this.prototype, hide);
		inject(this, base);
		return inject(this, src.$static, base);
	};

	Function.prototype.extend = function(src, hide) {
		var ctor = extend(new this(this.dont));
		ctor.prototype.constructor = ctor;
		ctor.dont = {};
		return this.inject.call(ctor, src, hide, this);
	};

	Object.prototype.dontEnum = function(force) {
		var d = this._dontEnum = !(d = this._dontEnum) ? {} :
				d._object != this ? new (extend(d)) : d;
		d._object = this;
		for (var i = force == true ? 1 : 0; i < arguments.length; i++)
			d[arguments[i]] = { object: this, allow: force != true };
	};

	Object.prototype.dontEnum(true, "dontEnum", "_dontEnum", "__proto__",
		"prototype", "constructor", "$static");

	Object.inject({
		has: function(name) {
			var entry;
			return name in this && (!(entry = this._dontEnum[name]) ||
				entry.allow && entry.object[name] !== this[name])
		},

		inject: function(src, hide) {
			return inject(this, src, this, hide);
		},

		extend: function(src, hide) {
			return (new (extend(this))).inject(src, hide);
		}
	}, true);
})();

function $typeof(obj) {
	return obj && (obj._type || typeof obj) || undefined;
}

function $random(min, max) {
	return Math.floor(Math.random() * (max - min + 1) + min);
}

if (!Function.prototype.$constructor) {
	Function = Function.extend({
		$constructor: function() {
			var args = $A(arguments), body = args.pop();
			var cx = Packages.org.mozilla.javascript.Context.getCurrentContext();
			return cx.compileFunction({}, "function (" + args + ") {" + 
				body + "}", "Function", 0, null);
		}
	});
}

Function.inject(function() {

	return {
		parameters: function() {
			return this.toString().match(/^\s*function[^\(]*\(([^\)]*)/)[1].split(/\s*,\s*/);
		},

		body: function() {
			return this.toString().match(/^\s*function[^\{]*\{([\s\S]*)\}\s*$/)[1];
		},

		bind: function(obj) {
			var that = this, args = $A(arguments, 1);
			return function() {
				return that.apply(obj, args.concat($A(arguments)));
			}
		},

		attempt: function(obj) {
			var that = this, args = $A(arguments, 1);
			return function() {
				try { return that.apply(obj, args.concat($A(arguments))); }
				catch(e) { return e; }
			}
		}
	}
}, true);

$break = {};

function $each(obj, iter, bind) {
	return obj ? Enumerable.each.call(obj, iter, bind) : bind;
};

Enumerable = (function() {

	function iterator(iter) {
		if (!iter) return function(val) { return val };
		switch ($typeof(iter)) {
			case 'function': return iter;
			case 'regexp': return function(val) { return iter.test(val) };
		}
		return function(val) { return val == iter };
	}

	function iterate(fn, name, convert, start) {
		Object.prototype.dontEnum(true, name);
		return function(iter, bind) {
			if (convert) iter = iterator(iter);
			if (!bind) bind = this;
			var prev = bind[name];
			bind[name] = iter;
			try { return fn.call(this, iter, bind, this); }
			finally { bind[name] = prev; }
		};
	}

	var each_Array = Array.prototype.forEach || function(iter, bind) {
		for (var i = 0; i < this.length; i++)
			bind.__each(this[i], i, this);
	};

	var each_Object = function(iter, bind) {
		var entries = this._dontEnum || {};
		for (var i in this) {
			var val = this[i], entry = entries[i];
			if (!entry || entry.allow && entry.object[i] !== this[i])
				bind.__each(val, i, this);
		}
	};

	return {
		each: iterate(function(iter, bind) {
			try { (this.length != null ? each_Array : each_Object).call(this, iter, bind); }
			catch (e) { if (e !== $break) print(e + ': ' + e.fileName + ' #' + e.lineNumber); }
			return bind;
		}, "__each"),

		find: iterate(function(iter, bind, that) {
			return this.each(function(val, key) {
				if (bind.__find(val, key, that)) {
					this.value = val;
					throw $break;
				}
			}, {}).value;
		}, "__find", true),

		some: function(iter, bind) {
			return this.$super ? this.$super(iterator(iter), bind) :
				this.find(iter, bind) !== undefined;
		},

		every: iterate(function(iter, bind, that) {
			return this.$super ? this.$super(iter, bind) : this.find(function(val, i) {
				return this.__every(val, i, that);
			}, bind) === undefined;
		}, "__every", true),

		map: iterate(function(iter, bind, that) {
			return this.$super ? this.$super(iter, bind) : this.each(function(val, i) {
				this.push(bind.__map(val, i, that));
			}, []);
		}, "__map", true),

		filter: iterate(function(iter, bind, that) {
			return this.$super ? this.$super(iter, bind) : this.each(function(val, i) {
				if (bind.__filter(val, i, that)) this.push(val);
			}, []);
		}, "__filter", true),

		contains: function(obj) {
			return this.find(function(val) { return obj == val }) !== undefined;
		},

		max: iterate(function(iter, bind, that) {
			return this.each(function(val, i) {
				val = bind.__max(val, i, that);
				if (val >= (this.max || val)) this.max = val;
			}, {}).max;
		}, "__max", true),

		min: iterate(function(iter, bind, that) {
			return this.each(function(val, i) {
				val = bind.__min(val, i, that);
				if (val <= (this.min || val)) this.min = val;
			}, {}).min;
		}, "__min", true),

		pluck: function(prop) {
			return this.map(function(val) {
				return val[prop];
			});
		},

		sortBy: iterate(function(iter, bind, that) {
			return this.map(function(val, i) {
				return { value: val, compare: this.__sortBy(val, i, that) };
			}, bind).sort(function(left, right) {
				var a = left.compare, b = right.compare;
				return a < b ? -1 : a > b ? 1 : 0;
			}).pluck('value');
		}, "__sortBy", true),

		swap: function(i, j) {
			var tmp = this[i];
			this[i] = this[j];
			this[j] = tmp;
		},

		toArray: function() {
			return this.map();
		}
	}
})();

Object.inject({
	each: Enumerable.each,
	clone: function() {
		return this.each(function(val, i) {
			this[i] = val;
		}, {});
	}
}, true);

function $A(list, start, end) {
	if (!list) return [];
	else if (list.toArray && !start && end == null) return list.toArray();
	var res = [];
	if (!start) start = 0;
	if (end == null) end = list.length;
	for (var i = start; i < end; i++)
		res[i - start] = list[i];
	return res;
}

Array.methods = {};

Array.methods.inject(Enumerable);

Array.methods.inject({
	_type: "array",

	indexOf: Array.prototype.indexOf || function(obj, i) {
		i = i || 0;
		if (i < 0) i = Math.max(0, this.length + i);
		for (i; i < this.length; i++) if (this[i] == obj) return i;
		return -1;
	},

	lastIndexOf: Array.prototype.lastIndexOf || function(obj, i) {
		i = i != null ? i : this.length - 1;
		if (i < 0) i = Math.max(0, this.length + i);
		for (i; i >= 0; i--) if (this[i] == obj) return i;
		return -1;
	},

	find: function(iter) {
		if (iter && !/function|regexp/.test($typeof(iter))) return this[this.indexOf(iter)];
		else return Enumerable.find.call(this, iter);
	},

	remove: function(obj) {
		var i = this.indexOf(obj);
		if (i != -1) return this.splice(i, 1);
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

	first: function() {
		return this[0];
	},

	last: function() {
		return this[this.length - 1];
	},

	compact: function() {
		return this.filter(function(value) {
			return value != null;
		});
	},

	append: function(obj) {
		return $each(obj, function(val) {
			this.push(val);
		}, this);
	},

	include: function(obj) {
		return $each(obj, function(val) {
			if (this.indexOf(val) == -1) this.push(val);
		}, this);
	},

	flatten: function() {
		return this.each(function(val) {
			if (val != null && val.flatten) this.append(val.flatten());
			else this.push(val);
		}, []);
	},

	shuffle: function() {
		var res = this.clone();
		var i = this.length;
		while (i--) res.swap(i, $random(0, i));
		return res;
	}
});

Array.inject(Array.methods, true);

String.inject({
	test: function(exp, param) {
		return new RegExp(exp, param || '').test(this);
	},

	toArray: function() {
		return this ? this.split(/\s+/) : [];
	},

	toInt: function() {
		return parseInt(this);
	},

	toFloat: function() {
		return parseFloat(this);
	},

	toCamelCase: function() {
		return this.replace(/-\D/g, function(match) {
			return match.charAt(1).toUpperCase();
		});
	},

	hyphenate: function() {
		return this.replace(/\w[A-Z]/g, function(match) {
			return (match.charAt(0) + '-' + match.charAt(1).toLowerCase());
		});
	},

	capitalize: function() {
		return this.toLowerCase().replace(/\b[a-z]/g, function(match) {
			return match.toUpperCase();
		});
	},

	trim: function() {
		return this.replace(/^\s+|\s+$/g, '');
	},

	clean: function() {
		return this.replace(/\s{2,}/g, ' ').trim();
	}
});

Number.inject({
	_type: 'number',

	times: function(iter) {
		for (var i = 0; i < this; i++) iter();
		return this;
	},

	toInt: String.prototype.toInt,

	toFloat: String.prototype.toFloat
});

RegExp.inject({
	_type: "regexp"
});

function $H(obj) {
	return $typeof(obj) == 'hash' ? obj : new Hash(obj);
}

Hash = Object.extend({
	_type: "hash",

	$constructor: function(obj) {
		if (obj) this.merge(obj);
	},

	clone: function() {
		return new Hash(this);
	},

	merge: function(obj) {
		return obj.each(function(val, i) {
			this[i] = $typeof(this[i]) != 'object' ? val : arguments.callee.call(this[i], val);
		}, this);
	},

	keys: function() {
		return this.map(function(val, i) {
			return i;
		});
	},

	values: Enumerable.toArray
}, true);

Hash.inject(Enumerable, true);
