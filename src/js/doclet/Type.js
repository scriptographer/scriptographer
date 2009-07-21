/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

Type = Object.extend(new function() {
	var ineritanceCache = {};

	function getCache(obj, type) {
		var cache = ineritanceCache[type];
		if (!cache)
			cache = ineritanceCache[type] = {};
		var name = obj.toString();
		var hash = cache[name];
		if (!hash)
			hash = cache[name] = {};
		return hash;
	}

	function getCached(first, second, type) {
		var cache = getCache(first, type);
		return cache[second.toString()];
	}

	function setCached(first, second, type, value) {
		var cache = getCache(first, type);
		cache[second.toString()] = value;
	}

	return {
		// Marks already extended native instance in initialize
		_extended: true,

		initialize: function(type) {
			if (!type)
				throw 'Parameter for Type constructor cannot be null';
			// Enhance the prototype of the native object with Type.prototype, and return
			// type instead of this!
			// We need to do this because it is not possible to access the native
			// class for types. Sometimes they seem to just be ClassDocImpls
			if (!type._extended) {
				// Inject only what's not there already, to avoid overriding native
				// functions, e.g. hasSuperclass, subclassOf, qualifiedName, etc
				this.__proto__.each(function(field, key) {
					if (!this[key])
						this[key] = field;
				}, type);
			}
			return type;
		},

		hasSuperclass: function(superclass) {
			var doc = this.asClassDoc();
			if (doc) {
				// Cache results
				var val = getCached(this, superclass, 'superclass');
				if (val !== undefined)
					return val;
				val = false;
				while (doc && !val) {
					if (doc.qualifiedName() == superclass)
						val = true;
					doc = doc.superclass();
				}
				setCached(this, superclass, 'superclass', val);
				return val;
			}
		},

		hasInterface: function(face) {
			var doc = this.asClassDoc();
			if (doc) {
				// Cache results
				var val = getCached(this, face, 'interface');
				if (val !== undefined)
					return val;
				val = false;
				if (doc.qualifiedName() == face) {
					val = true;
				} else {
					// If an interface extends another one, its superclass is not
					// set here, but the super interface is simply in the
					// interfaces() list. Strange...
					val = doc.interfaces().find(function(f) {
						if (f.hasInterface(face))
							return true;
					});
					if (!val) {
						var doc = doc.superclass();
						if (doc) val = doc.hasInterface(face);
					}
				}
				setCached(this, face, 'interface', val);
				return val;
			}
		},

		getSuperclass: function() {
			// Caching
			if (this._superclass === undefined) {
				var doc = this.asClassDoc();
				var sc = doc.superclass();
				while (sc && !sc.isVisible())
					sc = sc.superclass();
				this._superclass = sc;
			}
			return this._superclass;
		},

		getSubclasses: function() {
			var doc = this.asClassDoc();
			var subs = root.classes().each(function(cls) {
				if (cls.isVisible() && cls != doc && cls.getSuperclass() == doc)
					this.push(cls);
			}, []);
			return subs;
		},

		subclassOf: function(other) {
			var doc = this.asClassDoc();
			return doc && other && doc.subclassOf(other) || false;
		},

		qualifiedName: function() {
			var doc = this.asClassDoc();
			return doc && doc.qualifiedName() || '';
		},

		isVisible: function() {
			var obj = ClassObject.get(this.asClassDoc());
			return obj && obj.isVisible();
			return doc && doc.isVisible() || true;
		},

		typeArguments: function() {
			return [];
		},

		isArray: function() {
			return this.dimension() != '';
		},

		isCollection: function() {
			return this.hasInterface('java.util.Collection');
		},

		isList: function() {
			return this.hasInterface('com.scratchdisk.list.ReadOnlyList');
		},

		isNumber: function() {
			return !this.isArray() && (Type.isNumber(this.typeName())
				|| this.hasSuperclass('java.lang.Number'));
		},

		isBoolean: function() {
			return !this.isArray() && (this.typeName() == 'boolean'
				|| this.hasSuperclass('java.lang.Boolean'));
		},

		isMap: function() {
			return !this.isArray() && this.hasInterface('java.util.Map');
		},

		isException: function() {
			return !this.isArray() && this.hasInterface('java.lang.Throwable');
		},

		isEnum: function() {
			return !this.isArray() && this.hasSuperclass('java.lang.Enum');
		},

		isEnumSet: function() {
			return !this.isArray() && this.qualifiedName() == 'java.util.EnumSet';
		},

		isEnumMap: function() {
			return !this.isArray() && this.qualifiedName() == 'java.util.EnumMap';
		},

		isPoint: function() {
			return !this.isArray() && (this.hasSuperclass('com.scriptographer.ai.Point') ||
				this.hasSuperclass('com.scriptographer.ui.Point'));
		},

		isRectangle: function() {
			return !this.isArray() && (this.hasSuperclass('com.scriptographer.ai.Rectangle') ||
				this.hasSuperclass('com.scriptographer.ui.Rectangle'));
		},

		isFile: function() {
			return !this.isArray() && this.hasSuperclass('java.io.File');
		},

		isFunction: function() {
			return !this.isArray() && this.hasSuperclass('com.scratchdisk.script.Callable');
		},

		isCompatible: function(type) {
			var cd1 = this.asClassDoc(), cd2 = type.asClassDoc();
			var type1, type2;
			return this.typeName() == type.typeName() && this.dimension() == type.dimension()
				|| cd2 && this.subclassOf(cd2) || cd1 && type.subclassOf(cd1)
				|| this.isNumber() && type.isNumber()
				|| this.isBoolean() && type.isBoolean()
				|| this.isMap() && type.isMap()
				|| this.isPoint() && type.isPoint()
				|| this.isRectangle() && type.isRectangle()
				|| this.isFile() && type.isFile()
				// Make sure arrays have compatible types
				|| this.isArray() && type.isArray() && (
					cd1 && cd2 && new Type(cd1).isCompatible(new Type(cd2))
					|| this.typeName() == type.typeName()
					|| Type.isNumber(this.typeName()) && Type.isNumber(type.typeName())
				)
				// Treat arrays and lists the same, as long as the component type is compatible
				|| this.actsAsArray() && type.actsAsArray()
					&& ((type1 = this.getComponentType()) && (type2 = type.getComponentType())
						&& type1.isCompatible(type2));
		},

		/**
		 * Returns true for all the types that get converted to an Array
		 * on the JavaScript side.
		 */
		actsAsArray: function() {
			return this.isArray() || this.isList() || this.isCollection();
		},

		/**
		 * Returns the component type of arrays, lists and collections
		 */
		getComponentType: function() {
			// Caching
			if (this._componentType === undefined) {
				this._componentType = null;
				if (this.isArray()) {
					var  doc = this.asClassDoc();
					this._componentType = doc ? new Type(doc) : null;
				} else if (this.isList() || this.isCollection()) {
					// Generics stuff
					var type = this.typeArguments()[0];
					// If there's no typeArgument here, walk up the inheritance chain
					// and see if we can find something there
					if (type && type.extendsBounds) // WildcardType
						type = type.extendsBounds()[0];
					if (type && type.bounds) // TypeVariable
						type = type.bounds()[0];
					if (type)
					 	type = new Type(type);
					// If we did not find it yet, walk through interfaces first,
					// then superclasses and check again.
					if (!type) {
						type = this.interfaceTypes().find(function(face) {
							face = new Type(face);
							if (face.isList())
								return face.getComponentType();
						});
						if (!type && this.superclassType) {
							var sup = this.superclassType();
							if (sup) {
								type = sup.asParameterizedType();
								if (type)
									type = new Type(type).getComponentType();
							}
						}
					}
					this._componentType = type || null;
				}
			}
			return this._componentType;
		},

		getListDescription: function() {
			if (this.isList()) {
				var parts = [];
				if (!this.hasInterface('com.scratchdisk.list.List'))
					parts.push('read-only');
				if (this.hasInterface('com.scratchdisk.list.ReadOnlyStringIndexList'))
					parts.push('also accessible by name');
				return parts.length ? parts.join(', ') : '';
			}
		},

		getType: function() {
			var type;
			if (this.isInterface()) type = 'Interface';
			else if (this.isException()) type = 'Exception';
			else type = 'Prototype';
			if (this.isAbstract()) type = 'Abstract ' + type;
			return type;
		},

		renderLink: function(param) {
			param = param || {};
			var str;
			// Complicated array handling first.
			if (!param.linksOnly && (this.isArray()
					|| (!this.isVisible() || param.arrayOnly) // Only show hidden list types as arrays
							&& (this.isList() || this.isCollection()))) {
				var type = this.getComponentType();
				// Support n-dimensional array the lazy way
				var part = 'Array of ';
				param.arrayOnly = false;
				str = (this.dimension().replace(/\[\]/g, part) || part) + (type
					? type.renderLink(param)
					: code_filter(Type.isNumber(this.typeName())
						? 'Number'
						: this.typeName().capitalize()));
			} else if (!param.arrayOnly) {
				if (this.isNumber()) {
					str = code_filter('Number');
				} else if (this.isBoolean()) {
					str = code_filter('Boolean');
				} else if (this.isMap()) {
					str = code_filter('Object');
				} else if (this.isEnum()) {
					str = code_filter('String');
				} else {
					var cls = this.asClassDoc();
					if (cls && cls.isVisible()) {
						str = cls.renderClassLink(param);
					} else if (this.isPoint()) {
						str = ClassObject.renderLink({
							name: 'com.scriptographer.ai.Point'
						});
					} else if (this.isRectangle()) {
						str = ClassObject.renderLink({
							name: 'com.scriptographer.ai.Rectangle'
						});
					} else if (this.isFile()) {
						str = ClassObject.renderLink({
							name: 'com.scriptographer.sg.File'
						});
					} else if (this.isFunction()) {
						return code_filter('Function');
					} else {
						str = code_filter(Type.getSimpleName(cls 
							? cls.name() 
							: this.typeName() + this.dimension()));
					}
				}
			}
			if (param.description)
				str += param.description;
			if (param.additional) {
				var add = this.renderAdditional();
				if (add)
					str += add;
			}
			return str;
		},

		// This is defined outside renderLink so that even when a Type
		// happens to be its own ClassDoc (as returned by asClassDoc), and therefore
		// overrides renderLink, it can still call the base version.
		renderClassLink: function(param) {
			var str = '';
			if (this.isVisible()) {
				if (this.isAbstract())
					str += '<i>';
				str += renderLink({
					path: this.qualifiedName(),
					anchor: '',
					title: param.title || code_filter(this.name())
				});
				if (this.isAbstract())
					str += '</i>';
			} else {
				str = Type.getSimpleName(this.name());
			}
			return str;
		},

		renderEnumConstants: function() {
			return this.enumConstants().map(function(value) {
				return code_filter("'" + value.name().toLowerCase().split('_').join('-') + "'");
			}).join(', ');
		},

		renderAdditional: function() {
			if (this.isEnum()) {
				return ' (' + this.renderEnumConstants() + ')';
			} else if (this.isEnumMap()) {
				var types = this.typeArguments();
				if (types.length == 2) {
					var keyType = new Type(types[0]);
					var valueType = new Type(types[1]);
					return ' (Values: ' + code_filter(valueType.renderLink()) 
						+ ', Keys: ' + keyType.renderEnumConstants() + ')';
				}
			} else if (this.isList()) {
				var desc = this.getListDescription();
				if (desc)
					return ', ' + desc;
			}
		},

		statics: {
			isNumber: function(typeName) {
				return /^(short|int|long|float|double)$/.test(typeName);
			},

			getSimpleName: function(name) {
				return name.match(/([^.]*)$/)[1];
			}
		}
	}
});

// Extend ClassDocImpl by Type. Only add field to prototype that do not exist
// natively. To check this, we need a actual instance of ClassDoc since right now
// ClassDocImpl.prototype is empty and does not provide references to native fields
// yet.
// TODO: Pick up the Scriptographer java-proto tag again and fix the problems with
// it (calling of native methods on non native instance when acccessing __proto__).
Type.prototype.each(function(value, name) {
	if (this.doc && this.doc[name] === undefined)
		this.docType[name] = value;
}, {
	doc: root.classes().first,
	docType: ClassDocImpl.prototype
});

ParameterImpl.inject({
	paramType: function() {
		return new Type(this.type());
	}
});
