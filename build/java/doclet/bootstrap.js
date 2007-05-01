/**
 * Bootstrap JavaScript Library
 * (c) 2006-2007 Juerg Lehni, http://scratchdisk.com/
 *
 * Bootstrap is released under the MIT license
 * http://bootstrap-js.net/
 *
 * Inspirations:
 * http://dean.edwards.name/weblog/2006/03/base/
 * http://dev.helma.org/Wiki/JavaScript+Inheritance+Sugar/
 * http://prototypejs.org/
 * http://mootools.net/
 *
 * Some code in this file is based on Mootools.net and adapted to the
 * architecture of Bootstrap, with added changes in design and architecture
 * where deemeded necessary.
 * See http://www.bootstrap-js.net/wiki/MootoolsDifferences
 */

////////////////////////////////////////////////////////////////////////////////
// Core (inject / extend)

(function() { // bootstrap
	/**
	 * Private function that injects functions from src into dest, overriding
	 * (and inherinting from) base. if allowProto is set, the name "prototype"
	 * is inherited too. This is false for static fields, as prototype there
	 * points to the classes' prototype.
	 */
	function inject(dest, src, base, hide) {
		// Iterate through all definitions in src with an iteator function
		// that checks if the field is a function that needs to be wrapped for
		// calls of $super. This is only needed if the function in base is
		// different from the one in src, and if the one in src is actually
		// calling base through $super. the string of the function is parsed
		// for $super to detect calls.
		// dest[name] then is set to either src[name] or the wrapped function.
		if (src) for (var name in src)
			if (has(src, name)) (function(val, name) {
				/* TODO: decide what to do with this!
				if (typeof val == 'function' && /\$super\./.test(val)) {
					val = new Function(val.parameters(), val.body().replace(/\$super\./, 'this.__proto__.__proto__.'));
				}
				*/
				var res = val, baseVal = base && base[name];
				var func = typeof val == 'function', match;
				if (func && baseVal && val !== baseVal && /\$super\b/.test(val)) {
					res = function() {
						var prev = this.$super;
						// Look up the $super function each time if we can,
						// to reflect changes to the base class after 
						// inheritance. this only works if inject is called
						// with the third argument (base), see code bellow.
						this.$super = base != dest ? base[name] : baseVal;
						try { return val.apply(this, arguments); }
						finally { this.$super = prev; }
					};
					// Redirect toString to the one from the original function
					// to "hide" the wrapper function
					res.toString = function() {
						return val.toString();
					};
				}
				// Check if the function defines a getter or setter by looking
				// at its name. TODO: measure speed decrease of inject due to this!
				if (func && (match = name.match(/([^$]*)\$(g|s)et$/)))
					dest['__define' + match[2].toUpperCase() + 'etter__'](match[1], res);
				else
					dest[name] = res;
				// Parameter hide was named dontEnum, but this caused 
				// problems on Opera, where it then seems to point to
				// Object.prototype.dontEnum, which must be a bug in Opera.
				if (hide && dest.dontEnum != null)
					dest.dontEnum(name);
			})(src[name], name);
		return dest;
	}

	/**
	 * Private function that creates a constructor to extend the given object.
	 * When this constructor is called through new, a new object is craeted
	 * that inherits all from obj.
	 */
	function extend(obj) {
		// Create the constructor for the new prototype that calls $constructor
		// if it is defined.
		var ctor = function() {
			// Call the constructor function, if defined and we're not inheriting
			// in which case ctor.dont would be set, see further bellow.
			if (this.$constructor && arguments[0] !== ctor.dont)
				return this.$constructor.apply(this, arguments);
		};
		ctor.prototype = obj;
		return ctor;
	}

	/**
	 * Private function that checks if an object contains a given property.
	 */
	function has(obj, name) {
		// This is tricky: as described in Object.prototype.dontEnum, the
		// _dontEnum  objects form a inheritance sequence between prototypes.
		// So if we check  obj._dontEnum[name], we're not sure that the
		// value returned is actually from the current object. It might be
		// from a parent in the inheritance chain. This is why dontEnum
		// stores a reference to the object on which dontEnum was called for
		// that object. If the value there differs from the one in obj, 
		// it means it was modified somewhere between obj and there.
		// If the entry allows overriding, we return true although _dontEnum
		// lists it.
		var entry;
		return name in obj && (!(entry = obj._dontEnum[name]) ||
				entry.allow && entry.object[name] !== obj[name]);
	}

	Function.prototype.inject = function(src, hide, base) {
		// When called from extend, a third argument is passed, pointing
		// to the base class (the constructor).
		// this variable is needed for inheriting static fields and proper lookups
		// of $super on each call (see bellow)
		// src can either be a function to be called, or a object literal.
		src = typeof src == 'function' ? src() : src;
		// Define new instance fields, and inherit from base, if available.
		// Otherwise inherit from ourself this works for also for functions in the
		// base class, as they are available through this.prototype. But if base
		// is not available, $super will not be looked up each time when it's
		// called (as this would result in an endless recursion). In this case,
		// the super class is "hard-coded" in the wrapper function, and	further
		// changes to it after inheritance are not reflected.
		inject(this.prototype, src, base ? base.prototype : this.prototype, hide);
		// Copy over static fields from base, as prototype-like inheritance is not
		// possible for static fields. If base is null, this does nothing.
		// TODO: This needs fixing for versioning on the server!
		// Do not set dontEnum for static, as otherwise we won't be able to inject 
		// static fields from base next time
		inject(this, base);
		// Define new static fields, and inherit from base.
		return inject(this, src.$static, base);
	};

	Function.prototype.extend = function(src, hide) {
		// The new prototype extends the constructor on which extend is called.
		var ctor = extend(new this(this.dont));
		// fix constructor
		ctor.prototype.constructor = ctor;
		// An object to be passed as the first parameter in constructors
		// when $constructor should not be called. This needs to be a property
		// of the created constructor, so that if .extend is called on native
		// constructors or constructors not created through .extend, this.dont
		// will be undefined and no value will be passed to the constructor that
		// would not know what to do with it.
		ctor.dont = {};
		// and inject all the definitions in src
		// pass inject on before it's executed, in case it was overriden.
		// Needed in Collection.js
		return this.inject.call(ctor, src, hide, this);
	};

	Object.prototype.dontEnum = function(force) {
		// inherit _dontEnum with all its settings from prototype and extend.
		// _dontEnum objects form an own inheritance sequence, in parallel to 
		// the inheritance of the prototypes / objects they belong to. The 
		// sequence is only formed when dontEnum() is called, so there might
		// be problems with empty prototypes that get filled after inheritance
		// (very uncommon).
		// Here we check if getting _dontEnum on the object actually returns the
		// one of the parent. If it does, we create a new one by extending the
		// current one.
		// We cannot call proto._dontEnum.extend in Object.prototype.dontEnum,
		// as this is a dontEnum entry after calling
		// Object.prototype.dontEnum("extend"). Use the private function instead.
		// Each _dontEnum object has a property _object that points to the
		// object it belongs to. This makes it easy to check if we need to extend
		// _dontEnum for any given object dontEnum is called on:
		/*
		// Alternative, when __proto__ works
		if (!this._dontEnum) this._dontEnum = {};
		else if (this.__proto__ && this._dontEnum === this.__proto__._dontEnum)
			this._dontEnum = new (extend(this._dontEnum));
		*/
		/*
		// The code bellow is a compressed form of this:
		if (!this._dontEnum) this._dontEnum = { _object: this };
		else if (this._dontEnum._object != this) {
			this._dontEnum = new (extend(this._dontEnum));
			this._dontEnum._object = this;
		}
		*/
		// note that without the parantheses around extend(d), new would not
		// create an instance of the returned constructor!
		var d = this._dontEnum = !(d = this._dontEnum) ? {} :
				d._object != this ? new (extend(d)) : d;
		d._object = this;
		for (var i = force == true ? 1 : 0; i < arguments.length; i++)
			d[arguments[i]] = { object: this, allow: force != true };
	};

	// First dontEnum the fields that cannot be overridden (wether they change
	// value or not, they're allways hidden, by setting the first argument to true)
	Object.prototype.dontEnum(true, 'dontEnum', '_dontEnum', '__proto__',
			'prototype', 'constructor', '$static');

	// From now on Function inject can be used to enhance any prototype,
	// for example Object:
	Object.inject({
		/**
		 * Returns true if the object contains a property with the given name,
		 * false otherwise.
		 * Just like in .each, objects only contained in the prototype(s) are
		 * filtered.
		 */
		has: function(name) {
			return has(this, name);
		},

		/**
		 * Injects the fields from the given object, adding $super functionality
		 */
		inject: function(src, hide) {
			// src can either be a function to be called, or an object literal.
			return inject(this, typeof src == 'function' ? src() : src, this, hide);
		},

		/**
		 * Returns a new object that inherits all properties from "this", through
		 * proper JS inheritance, not copying.
		 * Optionally, src and hide parameters can be passed to fill in the
		 * newly created object just like in inject(), to copy the behavior
		 * of Function.prototype.extend.
		 */
		extend: function(src, hide) {
			// notice the "new" here: the private extend returns a constructor
			// as it's used for Function.prototype.extend as well. But when 
			// extending objects, we want to return a new object that inherits
			// from "this". In that case, the constructor is never used again,
			// its just created to create a new object with the proper inheritance
			// set and is garbage collected right after.
			return (new (extend(this))).inject(src, hide);
		}
	}, true);
})();

// Retrieve a reference to the global scope, usually window.
global = (function() { return this })();

function $typeof(obj) {
	return obj && (obj._type || typeof obj) || undefined;
}

// TODO: consider moving this somewhere more appropriate
function $random(min, max) {
	return Math.floor(Math.random() * (max - min + 1) + min);
}

Object.inject({
	debug: function() {
		return /^(string|number|function|regexp)$/.test($typeof(this)) ? this
			: this.each(function(val, key) { this.push(key + ': ' + val); }, []).join(', ');
	}
}, true);

////////////////////////////////////////////////////////////////////////////////
// Function

Function.inject(function() {

	return {
		/**
		 * Returns the function's parameter names as an array
		 */
		parameters: function() {
			return this.toString().match(/^\s*function[^\(]*\(([^\)]*)/)[1].split(/\s*,\s*/);
		},

		/**
		 * Returns the function's body as a string, excluding the surrounding { }
		 */
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
// dontEnum these properties, as we iterate through Function.prototype in
// Function.prototype.inject

//#include "lang/Enumerable_apply.js"

////////////////////////////////////////////////////////////////////////////////
// Enumerable

/**
 * A special constant, to be thrown by closures passed to each()
 * Inspired by Prototype.js
 *
 * $continue is not implemented, as the same functionality can achieved
 * by using return in the closure. In prototype, the implementation of $continue
 * also leads to a huge speed decrease, as the closure is wrapped in another
 * closure that does nothing else than handling $continue.
 */
$break = {};

function $each(obj, iter, bind) {
	return obj ? Enumerable.each.call(obj, iter, bind) : bind;
};

/**
 * The Enumerable interface. To add enumerable functionality to any prototype,
 * just use Constructor.inject(Enumerable);
 * This adds the function .each() that can handle both arrays (detected through
 * .length) and dictionaries (if it's not an array, enumerating with for-in).
 *
 * Inspired by Prototype.js.
 */
Enumerable = (function() {
	/**
	 * Converts the argument to an iterator function. If none is specified,
	 * the identity function is returned. 
	 * This supports regular expressions, normal functions, which are
	 * returned unmodified, and values to compare to.
	 * Wherever this private function is used in the Enumerable functions
	 * bellow, a RegExp object, a Function or null may be passed.
	 */
	function iterator(iter) {
		if (!iter) return function(val) { return val };
		switch ($typeof(iter)) {
		case 'function': return iter;
		case 'regexp': return function(val) { return iter.test(val) };
		}
		return function(val) { return val == iter };
	}

	/**
	 * Converts the passed function to an iterate-function.
	 * This is part of an optimization that is based on the observation that
	 * calling the iterator through bind.__iterate is faster than using iter.apply()
	 * each time.
	 * So iterate wraps the passed function in a closure that sets the __iterate
	 * function (defined by name) on the bind object and then calls the original
	 * function, restoring the original state after it returns.
	 * This is used in many places in Enumerable, each place defining its own 
	 * name for the iterate function, so there are no clashes if calls are nested.
	 */
	function iterate(fn, name) {
		// dontEnum all iterators once and for all on browsers:
		Object.prototype.dontEnum(true, name);
		return function(iter, bind) {
			iter = iterator(iter);
			if (!bind) bind = this;
			var prev = bind[name];
			bind[name] = iter;
			// HACK: If the object does not allow setting of values,
			// create a new unsealed object that inherits from it.
			/*
			if (bind[name] != iter) {
				bind = bind.extend();
				bind[name] = iter;
			}
			*/
			// Interesting benchmark observation: The loops seem execute 
			// faster when called on the object (this), so outsource to
			// the above functions each_Array / each_Object here.
			// pass this twice, so it can be recieved as "that" in the iterating
			// functions, to be passed to the iterator (and being able to use 
			// "this" in .each differently)
			try { return fn.call(this, iter, bind, this); }
			finally { bind[name] = prev; }
		};
	}

	var each_Array = Array.prototype.forEach || function(iter, bind) {
		for (var i = 0; i < this.length; i++)
			bind.__each(this[i], i, this);
	};

	var each_Object = function(iter, bind) {
		// We use for-in here, but need to filter out what does not belong
		// to the object itself. This is done by comparing the value with
		// the value of the same name in the prototype. If the value is
		// equal it's defined in one of the prototypes, not the object
		// itself.
		var entries = this._dontEnum || {};
		for (var i in this) {
			var val = this[i], entry = entries[i];
			// added properties. This line here is the same as Object.prototype.has
			if (!entry || entry.allow && entry.object[i] !== this[i])
				bind.__each(val, i, this);
		}
	};

	return {
		/**
		 * The core of all Enumerable functions. TODO: document
		 */
		each: iterate(function(iter, bind) {
			try { (this.length != null ? each_Array : each_Object).call(this, iter, bind); }
			catch (e) { if (e !== $break) throw e; }
			return bind;
		}, "__each"),

		/**
		 * Searches the list for the first element where the condition of the
		 * passed iterator is met and returns its key / value pair as an object:
		 * { key: ... , value: ... }
		 */
		findEntry: iterate(function(iter, bind, that) {
			return this.each(function(val, key) {
				if (bind.__find(val, key, that)) {
					this.found = { key: key, value: val };
					throw $break;
				}
			}, {}).found;
		}, "__find"),

		/**
		 * Searches the list for the first element where the condition of the
		 * passed iterator is met and returns its value.
		 */
		find: function(iter, bind) {
			var entry = this.findEntry(iter, bind);
			return entry && entry.value;
		},

		/**
		 * Returns true if the condition defined by the passed iterator is true
		 * for one or more of the elements, false otherwise.
		 * If no iterator is passed, the value is used directly.
		 * This is compatible with JS 1.5's .some, but adds more flexibility
		 * regarding iterators (as defined in iterator())
		 */
		some: function(iter, bind) {
			// when injecting into Array, there might already be a definition of
			// .some() (Firefox JS 1.5+), so use it as it's faster and does the
			// same, except for the iterator conversion which is handled by
			// iterator() here:
			return this.$super ? this.$super(iterator(iter), bind) :
				this.findEntry(iter, bind) !== undefined;
		},

		/**
		 * Returns true if the condition defined by the passed iterator is true
		 * for	all elements, false otherwise.
		 * If no iterator is passed, the value is used directly.
		 * This is compatible with JS 1.5's .every, but adds more flexibility
		 * regarding iterators (as defined in iterator())
		 */
		every: iterate(function(iter, bind, that) {
			// Just like in .some, use .every if it's there
			return this.$super ? this.$super(iter, bind) : this.findEntry(function(val, i) {
				// as "this" is not used for anything else, use it for bind,
				// so that lookups on the object are faster (according to 
				// benchmarking)
				return this.__every(val, i, that);
			}, bind) === undefined;
		}, "__every"),

		/**
		 * Collects the result of the given iterator applied to each of the
		 * elements in an array and returns it.
		 * If no iterator is passed, the value is used directly.
		 * This is compatible with JS 1.5's .map, but adds more flexibility
		 * regarding iterators (as defined in iterator())
		 */
		map: iterate(function(iter, bind, that) {
			// Just like in .some, use .map if it's there
			return this.$super ? this.$super(iter, bind) : this.each(function(val, i) {
				this.push(bind.__map(val, i, that));
			}, []);
		}, "__map"),

		/**
		 * Collects all elements for which the condition of the passed iterator
		 * or regular expression is true.
		 * This is compatible with JS 1.5's .filter, but adds more flexibility
		 * regarding iterators (as defined in iterator())
		 */
		filter: iterate(function(iter, bind, that) {
			// Just like in .some, use .map if it's there
			return this.$super ? this.$super(iter, bind) : this.each(function(val, i) {
				if (bind.__filter(val, i, that)) this.push(val);
			}, []);
		}, "__filter"),

		/**
		 * Returns the maximum value of the result of the passed iterator
		 * applied to each element.
		 * If no iterator is passed, the value is used directly.
		 */
		max: iterate(function(iter, bind, that) {
			return this.each(function(val, i) {
				val = bind.__max(val, i, that);
				if (val >= (this.max || val)) this.max = val;
			}, {}).max;
		}, "__max"),

		/**
		 * Returns the minimum value of the result of the passed iterator
		 * applied to each element. 
		 * If no iterator is passed, the value is used directly.
		 */
		min: iterate(function(iter, bind, that) {
			return this.each(function(val, i) {
				val = bind.__min(val, i, that);
				if (val <= (this.min || val)) this.min = val;
			}, {}).min;
		}, "__min"),

		/**
		 * Collects the values of the given property of each of the elements
		 * in an array and returns it.
		 */
		pluck: function(prop) {
			return this.map(function(val) {
				return val[prop];
			});
		},

		/**
		 * Sorts the elements depending on the outcome of the passed iterator
		 * and returns the sorted list in an array.
		 * Inspired by Prototype.js
		 */
		sortBy: iterate(function(iter, bind, that) {
			return this.map(function(val, i) {
				return { value: val, compare: this.__sortBy(val, i, that) };
			}, bind).sort(function(left, right) {
				var a = left.compare, b = right.compare;
				return a < b ? -1 : a > b ? 1 : 0;
			}).pluck('value');
		}, "__sortBy"),

		/**
		 * Swaps two elements of the object at the given indices, and returns
		 * the value that is placed at the first index.
		 */
		swap: function(i, j) {
			var tmp = this[j];
			this[j] = this[i];
			this[i] = tmp;
			return tmp;
		},

		/**
		 * Converts the Enumerable to a normal array.
		 */
		toArray: function() {
			return this.map();
		}
	}
})();

////////////////////////////////////////////////////////////////////////////////
// Object

// Do not use inject for the definition of $each and each yet, as it relies
// on them already being there:

Object.inject({
	/**
	 * Copied over from Enumerable.
	 */
	each: Enumerable.each,
	/**
	 * Creates a new object and copies over all name / value pairs from this
	 * associative array. Uses $each to filter out unwanted properties
	 */
	clone: function() {
		return this.each(function(val, i) {
			this[i] = val;
		}, {});
	}
}, true);

////////////////////////////////////////////////////////////////////////////////
// Array

/**
 * Converts the list to an array. various types are supported. 
 * Inspired by Prototype.js
 */
function $A(list, start, end) {
	if (!list) return [];
	if (end == null) end = list.length;
	if (list.toArray && !start && end == list.length)
		return list.toArray();
	if (!start) start = 0;
	var res = [];
	for (var i = start; i < end; i++)
		res[i - start] = list[i];
	return res;
}

// Use a container to mix the functions together, as we're referencing
// Array.prototype.* bellow, which would get overridden otherwise
Array.methods = {};

// Inherit the full Enumerable interface
Array.methods.inject(Enumerable);

// Add some more:
Array.methods.inject({
	// tell $typeof what to return for arrays.
	_type: "array",

	/**
	 * Returns the index of the given object if found, -1 otherwise.
	 */
	indexOf: Array.prototype.indexOf || function(obj, i) {
		i = i || 0;
		if (i < 0) i = Math.max(0, this.length + i);
		for (i; i < this.length; i++) if (this[i] == obj) return i;
		return -1;
	},

	/**
	 * Returns the last index of the given object if found, -1 otherwise.
	 */
	lastIndexOf: Array.prototype.lastIndexOf || function(obj, i) {
		i = i != null ? i : this.length - 1;
		if (i < 0) i = Math.max(0, this.length + i);
		for (i; i >= 0; i--) if (this[i] == obj) return i;
		return -1;
	},

	findEntry: function(iter) {
		// use the faster indexOf in case we're not using iterator functions.
		if (iter && !/^(function|regexp)$/.test($typeof(iter))) {
			var i = this.indexOf(iter);
			return { key: i, value: this[i] };
		}
		return Enumerable.findEntry.call(this, iter);
	},

	remove: function(iter) {
		var entry = this.findEntry(iter);
		if (entry) return this.splice(entry.key, 1)[0];
	},

	/**
	 * Overrides the definition in Enumerable.toArray with a more efficient
	 * version.
	 */
	toArray: function() {
		return this.concat([]);
	},

	/**
	 * Clones the array.
	 */
	clone: function() {
		return this.toArray();
	},

	/**
	 * Clears the array.
	 */
	clear: function() {
		this.length = 0;
	},

	/**
	 * Returns the first element of the array.
	 */
	first: function() {
		return this[0];
	},

	/**
	 * Returns the last element of the array.
	 */
	last: function() {
		return this[this.length - 1];
	},

	/**
	 * Returns a compacted version of the array containing only
	 * elements that are not null.
	 */
	compact: function() {
		return this.filter(function(value) {
			return value != null;
		});
	},

	/**
	 * Appends the elments of the given enumerable object.
	 */
	append: function(obj) {
		// Do not rely on obj to have .each set, as it might come from another
		// frame.
		return $each(obj, function(val) {
			this.push(val);
		}, this);
	},

	/**
	 * adds all elements in the passed array, if they are not contained
	 * in the array already.
	 */
	/* TODO: needed?
	include: function(obj) {
		return $each(obj, function(val) {
			if (this.indexOf(val) == -1) this.push(val);
		}, this);
	},
	*/

	/**
	 * Flattens multi-dimensional array structures by breaking down each
	 * sub-array into the main array.
	 */
	flatten: function() {
		return this.each(function(val) {
			if (val != null && val.flatten) this.append(val.flatten());
			else this.push(val);
		}, []);
	},

	/**
	 * Returns a copy of the array containing the elements in shuffled sequence.
	 */
	shuffle: function() {
		var res = this.clone();
		var i = this.length;
		while (i--) res.swap(i, $random(0, i));
		return res;
	}
});

// now inject the mix
Array.inject(Array.methods, true);

////////////////////////////////////////////////////////////////////////////////
// String

String.inject({
	_type: 'string',

	test: function(exp, param) {
		return new RegExp(exp, param || '').test(this);
	},

	/**
	 * Splits the string into an array of words. This can also be used on any
	 * String through $A as defined in Array.js, to work similarly to $w in Ruby
	 */
	toArray: function() {
		return this ? this.split(/\s+/) : [];
	},

	toInt: function() {
		return parseInt(this);
	},

	toFloat: function() {
		return parseFloat(this);
	},

	camelize: function(separator) {
		return this.replace(new RegExp(separator || '-', 'g'), function(match) {
			return match.charAt(1).toUpperCase();
		});
	},

	uncamelize: function(separator) {
		separator = separator || '-';
		return this.replace(/[a-zA-Z][A-Z0-9]|[0-9][a-zA-Z]/g, function(match) {
			return match.charAt(0) + separator + match.charAt(1);
		});
	},

	hyphenate: function() {
		return this.uncamelize().toLowerCase();
	},

	capitalize: function() {
		return this.replace(/\b[a-z]/g, function(match) {
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

////////////////////////////////////////////////////////////////////////////////
// Number

Number.inject({
	// tell $typeof that number objects are numbers too.
	_type: 'number',
	toInt: String.prototype.toInt,
	toFloat: String.prototype.toFloat
});

////////////////////////////////////////////////////////////////////////////////
// RegExp

RegExp.inject({
	// tell $typeof what to return for regexps.
	_type: "regexp"
});

////////////////////////////////////////////////////////////////////////////////
// Hash

/**
 * Converts the passed object to a hash. 
 * Warning: Does not create a new instance if it is a hash already!
 */
function $H(obj) {
	return $typeof(obj) == 'hash' ? obj : new Hash(obj);
}

/**
 * As Object only defines each and two other basic functions to avoid name
 * clashes in all other prototypes, define a second prototype called Hash,
 * which basically does the same but fully implements Enumberable.
 * Also note the difference to Prototype.js, where Hash does not iterate 
 * in the same way. Instead of creating a new key / value pair object for
 * each element and passing the numerical index of it in the iteration as a
 * second argument, use the key as the index, and the value as the first
 * element. This is much simpler and faster, and I have not yet found out the
 * advantage of how Prototype handles it. 
 */
Hash = Object.extend({
	_type: "hash",

	/**
	 * Constructs a new Hash.
	 */
	$constructor: function(obj) {
		if (obj) this.merge(obj);
	},

	/**
	 * Clones the hash.
	 */
	clone: function() {
		return new Hash(this);
	},

	/**
	 * Merges with the given enumerable object and returns the modifed hash.
	 * Calls merge on value pairs if they are dictionaries.
	 */
	merge: function(obj) {
		return obj ? obj.each(function(val, i) {
			this[i] = $typeof(this[i]) != 'object' ? val : arguments.callee.call(this[i], val);
		}, this) : this;
	},

	/**
	 * Collects the keys of each element and returns them in an array.
	 */
	keys: function() {
		return this.map(function(val, i) {
			return i;
		});
	},

	/**
	 * Does the same as toArray(), but renamed to go together with keys()
	 */
	values: Enumerable.toArray
}, true);

// inject Enumerable into Hash.
Hash.inject(Enumerable, true);

