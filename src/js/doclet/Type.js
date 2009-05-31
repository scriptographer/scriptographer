/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

Type = Object.extend({
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
			// TODO: Put cache in __proto__?
			if (!this.superCache)
				this.superCache = {};
			var val = this.superCache[superclass];
			if (val !== undefined) return val;
			val = false;
			while (doc && !val) {
				if (doc.qualifiedName() == superclass)
					val = true;
				doc = doc.superclass();
			}
			this.superCache[superclass] = val;
			return val;
		}
	},

	hasInterface: function(face) {
		var doc = this.asClassDoc();
		if (doc) {
			// Cache results
			// TODO: Put cache in __proto__?
			if (!this.interCache)
				this.interCache = {};
			var val = this.interCache[face];
			if (val !== undefined) return val;
			val = false;
			if (doc.qualifiedName() == face) {
				val = true;
			} else {
				// if an interface extends another one, its superclass is not
				// set here, but the super interface is simply in the
				// interfaces() list.  strange...
				val = doc.interfaces().find(function(f) {
					if (f.hasInterface(face))
						return f;
				});
				if (!val) {
					var cd = doc.superclass();
					if (cd) val = cd.hasInterface(face);
				}
			}
			this.interCache[face] = val;
			return val;
		}
	},

	getSuperclass: function() {
		var cd = this.asClassDoc();
		var sc = cd && cd.superclass();
		if (sc && sc.isVisible())
			return sc;
	},

	getSubclasses: function() {
		var cd = this.asClassDoc();
		return root.classes().each(function(cls) {
			if (cls.isVisible() && cls.superclass() == cd && !cls.equals(cd))
				this.push(cls);
		}, []);
	},

	subclassOf: function(other) {
		var cd = this.asClassDoc();
		return cd && other && cd.subclassOf(other) || false;
	},

	qualifiedName: function() {
		var cd = this.asClassDoc();
		return cd && cd.qualifiedName() || '';
	},

	typeArguments: function() {
		return [];
	},

	isArray: function() {
		return this.dimension() != '';
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
			|| (this.isArray() || this.isList()) && (type.isArray() || type.isList()) && (
				(type1 = this.getComponentType()) && (type2 = type.getComponentType()) &&
				type1.isCompatible(type2)
			);
	},

	/**
	 * Returns the component type of arrays and lists
	 */
	getComponentType: function() {
		if (this.isArray()) {
			var  cd = this.asClassDoc();
			return cd && new Type(cd);
		} else if (this.isList()) {
			// Generics stuff
			var arg = this.typeArguments()[0];
			var type = arg && arg.extendsBounds()[0];
			return type && new Type(type);
		}
		return null;
	},

	getListDescription: function() {
		if (this.hasInterface('com.scratchdisk.list.List'))
			return 'Normal List';
		else if (this.hasInterface('com.scratchdisk.list.StringIndexList'))
			return 'String-index List';
		else if (this.hasInterface('com.scratchdisk.list.ReadOnlyStringIndexList'))
			return 'Read-only String-index List';
		else if (this.hasInterface('com.scratchdisk.list.ReadOnlyList'))
			return 'Read-only List';
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
		var str;
		if (this.isNumber()) {
			str = code_filter('Number');
		} else if (this.isBoolean()) {
			str = code_filter('Boolean');
		} else if (this.isArray()) {
			var doc = this.asClassDoc();
			doc = doc && new Type(doc);
			str = 'Array of ' + (doc
				? doc.renderLink({ additional: true })
				: code_filter(Type.isNumber(this.typeName())
					? 'Number'
					: this.typeName().capitalize()));
		} else if (this.isMap()) {
			str = code_filter('Object');
		} else if (this.isEnum()) {
			str = code_filter('String');
		} else if (this.isEnumSet()) {
			var types = this.typeArguments();
			if (types.length > 0) {
				str = 'Array of ' + code_filter('String');
			} else {
				str = code_filter(this.typeName() + this.dimension());
			}
		} else {
			var cls = this.asClassDoc();
			if (cls && cls.isVisible()) {
				str = cls.renderClassLink({});
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
			} else {
				str = code_filter(cls ? cls.name() : this.typeName() + this.dimension());
			}
		}
		if (param.additional) {
			var add = this.renderAdditional();
			if (add)
				str += ' ' + add;
		}
		return str;
	},

	getEnumConstants: function() {
		return this.enumConstants().map(function(value) {
			return code_filter('"' + value.name().toLowerCase().split('_').join('-') + '"');
		}).join(', ');
	},

	renderAdditional: function() {
		if (!this.isArray()) {
			if (this.isEnum()) {
				return '(' + this.getEnumConstants() + ')';
			} else if (this.isEnumSet()) {
				var types = this.typeArguments();
				if (types.length == 1) {
					var type = new Type(types[0]);
					return '(' + type.getEnumConstants() + ')';
				}
			} else if (this.isEnumMap()) {
				var types = this.typeArguments();
				if (types.length == 2) {
					var keyType = new Type(types[0]);
					var valueType = new Type(types[1]);
					return '(Values: ' + code_filter(valueType.renderLink({})) 
						+ ', Keys: ' + keyType.getEnumConstants() + ')';
				}
			}
		}
	},

	statics: {
		isNumber: function(typeName) {
			return /^(short|int|long|float|double)$/.test(typeName);
		},

		getSimpleName: function(name) {
			return name.substring(name.lastIndexOf('.') + 1);
		}
	}
});
