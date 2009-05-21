/**
 * JavaScript Doclet
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

importPackage(Packages.com.sun.javadoc);
importPackage(Packages.com.sun.tools.javadoc);
importPackage(Packages.com.scriptographer.script);

include('bootstrap.js');
include('template.js');

function print() {
	java.lang.System.out.println($A(arguments).join(', '));
}

function error() {
	java.lang.System.err.println($A(arguments).join(', '));
}

// Add renderTemplate function with caching to all objects
Object.inject(Template.methods, true);

// A global template writer
var out = new TemplateWriter();

// Enhance String a bit:
String.inject({
	endsWith: function(end) {
		return this.length >= end.length && this.substring(this.length - end.length) == end;
	},

	startsWith: function(start) {
		return this.length >= start.length && this.substring(0, start.length) == start;
	},

	isLowerCase: function() {
		return this.toLowerCase() == this;
	},

	isUpperCase: function() {
		return this.toUpperCase() == this;
	}
});

// Define settings from passed options:
var settings = {
	basePackage:  options.basepackage || '',
	destDir: (options.d + (options.d && !/\/^/.test(options.d) ? '/' : '')) || '',
	docTitle: options.doctitle || '',
	bottom: options.bottom || '',
	author: options.author || '',
	filterClasses: (options.filterclasses || '').trim().split(/\s+/),
	packageSequence: (options.packagesequence || '').trim().split(/\s+/),
	methodFilter: (options.methodfilter || '').trim().split(/\s+/),
	classOrder: (function() {
		var classOrder = new Hash();
		if (options.classorder) {
			var file = new java.io.BufferedReader(new java.io.FileReader(options.classorder));
			var line, count = 1;
			while ((line = file.readLine()) != null) {
				line = line.trim();
				if (line.length)
					classOrder[line.trim()] = count++;
			}
		}
		return classOrder;
	})(),
	templates: options.templates == 'true',
	inherited: options.noinherited != 'true',
	summaries: options.nosummaries != 'true',
	fieldSummary: options.nofieldsummary != 'true',
	constructorSummary: options.noconstructorsummary != 'true',
	hyperref: options.nohyperref != 'true',
	versionInfo: options.version == 'true',
	debug: options.shortinherited == 'true',
	section1Open: options.section1open || '<h1>',
	section1Close: options.section1close || '</h1>',
	section2Open: options.section2open || '<h2>',
	section2Close: options.section2close || '</h2>'
};

// Ehnance some of the javatool classes with usefull methods:

// Class helpers

ClassDocImpl.inject({
	hasSuperclass: function(superclass) {
		// cache results
		if (!this.superCache)
			this.superCache = {};
		var val = this.superCache[superclass];
		if (val !== undefined) return val;
		val = false;
		var doc = this;
		while (doc && !val) {
			if (doc.qualifiedName() == superclass)
				val = true;
			doc = doc.superclass();
		}
		this.superCache[superclass] = val;
		return val;
	},

	hasInterface: function(face) {
		// cache results
		if (!this.interCache)
			this.interCache = {};
		var val = this.interCache[face];
		if (val !== undefined) return val;
		val = false;
		if (this.qualifiedName() == face) {
			val = true;
		} else {
			// if an interface extends another one, its superclass is not
			// set here, but the super interface is simply in the
			// interfaces() list.  strange...
			val = this.interfaces().find(function(f) {
				if (f.hasInterface(face))
					return f;
			});
			if (!val) {
				var cd = this.superclass();
				if (cd) val = cd.hasInterface(face);
			}
		}
		this.interCache[face] = val;
		return val;
		
	},

	isVisible: function() {
		return ClassObject.get(this.qualifiedName()) != null;
	},

	getListType: function() {
		if (this.hasInterface('com.scratchdisk.util.SimpleList'))
			return 'Normal List';
		else if (this.hasInterface('com.scratchdisk.util.StringIndexList'))
			return 'String-index List';
		else if (this.hasInterface('com.scratchdisk.util.ReadOnlyList'))
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

	getSuperclass: function() {
		var sc = this.superclass();
		if (sc && sc.isVisible())
			return sc;
	},

	getSubclasses: function() {
		return root.classes().each(function(cls) {
			if (cls.isVisible() && cls.superclass() == this && !cls.equals(this))
				this.push(cls);
		}, []);
	},

	// This is defined outside renderLink so that even when a Type
	// happens to be its own ClassDoc (as returned by asClassDoc), and therefore
	// overrides renderLink, it can still call the base version.
	renderClassLink: function(name) {
		if (!name) name = this.name();
		var str = '';
		if (this.isVisible()) {
			if (this.isAbstract())
				str += '<i>';
			str += renderLink(this.qualifiedName(), this.name(), '', name);
			if (this.isAbstract())
				str += '</i>';
		} else {
			str = this.name();
		}
		return str;
	},

	renderLink: function(name) {
		return this.renderClassLink(name);
	}
});

// Type helpers

Type = Object.extend({
	// Marks already extended native instance in initialize
	_extended: true,

	initialize: function(type) {
		// Enhance the prototype of the native object with Type.prototype, and return
		// type instead of this!
		// We need to do this because it is not possible to access the native
		// class for types. Sometimes they seem to just be ClassDocImpls
		if (!type._extended)
			type.inject(this.__proto__);
		return type;
	},

	isNumber: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass('java.lang.Number') ||
			/^(short|int|long|double|float)$/.test(this.typeName());
	},

	isBoolean: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass('java.lang.Boolean') ||
			this.typeName() == 'boolean';
	},

	isArray: function() {
		var cd = this.asClassDoc();
		return this.dimension() != ''/* || cd &&
			cd.hasInterface('java.util.Collection')*/;
	},
	
	isMap: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasInterface('java.util.Map');
	},

	isEnum: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass('java.lang.Enum');
	},

	isEnumSet: function() {
		var cd = this.asClassDoc();
		return cd && cd.qualifiedName() == 'java.util.EnumSet';
	},

	isEnumMap: function() {
		var cd = this.asClassDoc();
		return cd && cd.qualifiedName() == 'java.util.EnumMap';
	},
	
	isPoint: function() {
		var cd = this.asClassDoc();
		return cd && (cd.hasSuperclass('com.scriptographer.ai.Point') ||
			cd.hasSuperclass('com.scriptographer.ui.Point'));
	},

	isRectangle: function() {
		var cd = this.asClassDoc();
		return cd && (cd.hasSuperclass('com.scriptographer.ai.Rectangle') ||
			cd.hasSuperclass('com.scriptographer.ui.Rectangle'));
	},

	isFile: function() {
		var cd = this.asClassDoc();
		return cd && (cd.hasSuperclass('java.io.File'));
	},
	
	isCompatible: function(type) {
		var cd1 = this.asClassDoc(), cd2 = type.asClassDoc();
		return this.typeName() == type.typeName() && this.dimension() == type.dimension() ||
			cd1 && cd2 && (cd1.subclassOf(cd2) || cd2.subclassOf(cd1)) ||
			this.isNumber() && type.isNumber() ||
			this.isBoolean() && type.isBoolean() ||
			this.isArray() && type.isArray() ||
			this.isMap() && type.isMap() ||
			this.isPoint() && type.isPoint() ||
			this.isRectangle() && type.isRectangle() ||
			this.isFile() && type.isFile();
	},

	renderLink: function(additional) {
		var str;
		if (this.isNumber()) {
			str = 'Number';
		} else if (this.isBoolean()) {
			str = 'Boolean';
		} else if (this.isArray()) {
			var doc = new Type(this.asClassDoc());
			str = 'Array of ' + (doc
				? doc.renderLink(true)
				: /^(short|int|long|float|double)$/.test(this.typeName())
					? 'Number'
					: this.typeName().capitalize());
		} else if (this.isMap()) {
			str = 'Object';
		} else if (this.isEnum()) {
			str = 'String';
		} else if (this.isEnumSet()) {
			var types = this.typeArguments();
			if (types.length > 0) {
				str = 'Array of String';
			} else {
				str = this.typeName() + this.dimension();
			}
		} else {
			var cls = this.asClassDoc();
			if (cls && cls.isVisible()) {
				str = cls.renderClassLink();
			} else if (this.isPoint()) {
				str = ClassObject.renderLink('Point', 'com.scriptographer.ai.Point');
			} else if (this.isRectangle()) {
				str = ClassObject.renderLink('Rectangle', 'com.scriptographer.ai.Rectangle');
			} else if (this.isFile()) {
				str = ClassObject.renderLink('File', 'com.scriptographer.sg.File');
			} else {
				str = cls ? cls.name() : this.typeName() + this.dimension();
			}
		}
		if (additional) {
			additional = this.renderAdditional();
			if (additional)
				str += ' ' + additional;
		}
		return str;
	},

	getEnumValues: function() {
		var cls = Packages;
		this.qualifiedName().split('.').each(function(part) {
			cls = cls[part];
		});
		return cls.values().map(function(value) {
			return code_filter('"' + EnumUtils.getScriptName(value) + '"');
		}).join(', ');
	},

	renderAdditional: function() {
		if (!this.isArray()) {
			if (this.isEnum()) {
				return '(' + this.getEnumValues() + ')';
			} else if (this.isEnumSet()) {
				var types = this.typeArguments();
				if (types.length == 1) {
					var type = new Type(types[0]);
					return '(' + type.getEnumValues() + ')';
				}
			} else if (this.isEnumMap()) {
				var types = this.typeArguments();
				if (types.length == 2) {
					var keyType = new Type(types[0]);
					var valueType = new Type(types[1]);
					return '(Values: ' + code_filter(valueType.renderLink()) 
						+ ', Keys: ' + keyType.getEnumValues() + ')';
				}
			}
		}
	}
});

// Tag

TagImpl.inject({
	text_macro: function() {
		var text = this.text();
		/*
		if (filterFirstSentence) {
			// cut away ':' and everything that follows:
			int pos = text.indexOf(':');
			if (pos >= 0) {
				text = text.substring(0, pos) + '.';
				more = false;
			}
		}
		*/
		return text;
	}
});

SeeTagImpl.inject({
	text_macro: function(param) {
		var ref = this.referencedMember() || this.referencedClass();
		if (ref) {
			/*
			if (!mem.renderLink) {
				var cls = mem.getClass();
				while (cls)
					cls = cls.getSuperclass();
			}
			*/
			return code_filter(ref.renderLink(param.classDoc));
		} else {
			error(this.position() + ': warning - @link contains undefined reference: ' + this);
			return code_filter(this);
		}
	}
});

// A fake tag, to define own tag lists. Used for bean properties

Tag = Object.extend({
	initialize: function(str) {
		this.str = str;
	},

	text_macro: function() {
		return this.str;
	}
});

// Parameter helpers

ParameterImpl.inject({
	paramType: function() {
		return new Type(this.type());
	}
});

// Member helpers

MemberDocImpl.inject({
	isCompatible: function(member) {
		if (this instanceof ExecutableMemberDoc &&
			member instanceof ExecutableMemberDoc) {
			// rule 1: static or not
			if (this.isStatic() != member.isStatic())
				return false;
			// rule 2: same return type
			if (this instanceof MethodDoc && member instanceof MethodDoc &&
				this.returnType().qualifiedTypeName() != member.returnType().qualifiedTypeName())
				return false;
			var params1 = this.parameters();
			var params2 = member.parameters();
			// rule 3: if not the same amount of params, the types need to be the same:
			var count = Math.min(params1.length, params2.length);
			for (var i = 0; i < count; i++)
				if (!params1[i].paramType().isCompatible(params2[i].paramType()))
					return false;
			return true;
		}
		// Fields cannot be grouped
		return false;
	},

	isVisible: function() {
		return Member.get(this.qualifiedName()) != null;
	},

	renderLink: function(current) {
		var mem = Member.get(this);
		return mem ? mem.renderLink(current) : this.toString();
	}

});

// Member

Member = Object.extend({
	initialize: function(classObject, member) {
		this.classObject = classObject;
		this.member = member;
	},

	init: function() {
		// nothing here, but in the extended classes
	},

	isVisible: function() {
		var hide = this.tags('jshide')[0];
		if (hide) hide = hide.text();
		return !/^(method|all|)$/.test(hide) && (!this.bean || !this.bean.isVisible());
	},

	renderMember: function(classDoc, index, member, containingClass) {
		if (this.isVisible()) {
			if (!member)
				member = this;

			if (!containingClass)
				containingClass = this.classDoc;

			if (index)
				index.push('"' + member.getId() + '": { title: "' + this.name() +
						'", text: "' + encodeJs(renderTags({
							classDoc: this.classDoc, tags: member.inlineTags()
						})) + '" }');

			// Thrown exceptions
			// if (this.member.thrownExceptions)
			//	renderTemplate('exceptions', { exceptions: this.member.thrownExceptions() }, out);
			// Description
			var returnType = this.returnType();
			return this.renderTemplate('member', {
				classDoc: classDoc, member: member, containingClass: containingClass,
				throwsTags: this.member && this.member.throwsTags ? this.member.throwsTags() : null,
				returnType: returnType && !returnType.typeName().equals('void') ? returnType : null
			});
		}
	},

	name: function() {
		return this.member.name();
	},

	firstSentenceTags: function() {
		return this.member.firstSentenceTags();
	},

	isStatic: function() {
		if (this._static == undefined) {
			this._static = this.member.isStatic();
			// A class can define if it does not want to show static methods as static
			var noStatics = this.member.containingClass().tags('jsnostatics')[0];
			if (noStatics) noStatics = noStatics.text();
			if (noStatics == 'true' || noStatics == undefined)
				this._static = false;
		}
		return this._static;
	},

	containingClass: function() {
		return this.member.containingClass();
	},

	containingPackage: function() {
		return this.member.containingPackage();
	},

	inlineTags: function() {
		return this.member.inlineTags();
	},

	seeTags: function() {
		return this.member.seeTags();
	},

	signature: function() {
		return '';
	},

	getNameSuffix: function() {
		return '';
	},

	parameters: function() {
		return null;
	},

	returnType: function() {
		return this.member instanceof FieldDoc ? new Type(this.member.type()) : null;
	},

	tags: function(tagname) {
		return this.member.tags(tagname);
	},

	renderSummary: function(classDoc) {
		return this.renderTemplate('summary', { classDoc: classDoc });
	},

	getId: function() {
		// Convert name + signature to css friendly id:
		return (this.name() + '-' + this.signature()).replace(/\W+/g, function(match) {
			return '-';
		}).trim('-');
	},

	getClass: function(current) {
		// In case the class is invisible, the current class needs to be used instead
		var containing = this.containingClass();
		return containing.isVisible() || current.superclass() != containing ?
				containing : current;
	},

	renderLink: function(current) {
		var cd = this.getClass(current);
		// Dont use mem.qualifiedName(). use cd.qualifiedName() + '.' + mem.name()
		// instead in order to catch the case where functions are moved from
		// invisible classes to visible ones (e.g. AffineTransform -> Matrix)
		return renderLink(cd.qualifiedName(), cd.name(), this.getId(),
				this.name() + this.getNameSuffix());
	},

	isSimilar: function(mem) {
		return this.isStatic() == mem.isStatic() && this.name().equals(mem.name());
	},

	statics: {
		put: function(name, member) {
			this.members[name] = member;
		},

		get: function(param) {
			if (param instanceof MemberDoc)
				param = param.qualifiedName();
			return this.members[param];
		},

		members: new Hash()
	}
});

/**
 * A group of members that are all 'compatible' in a JS way, e.g. have the same
 * amount of parameter with different types each (e.g. setters)
 * or various amount of parameters with default parameter versions, e.g.
 * all com.scriptogrpaher.ai.Pathfinder functions
 */
Method = Member.extend({
	initialize: function(classObject, param) {
		this.base(classObject);
		this.isGrouped = false;
		this.members = [];
		this.map = new Hash();
		if (param instanceof MethodDoc) {
			// used only in renderMember for overriding tags
			this.methodName = param.name();
			this.member = param;
			this.add(param);
		} else {
			this.methodName = param;
		}
	},

	add: function(member) {
		var swallow = true;
		// do not add base versions for overridden functions 
		var signature = member.signature();
		if (this.map[signature])
			swallow = false;
		this.map[signature] = member;
		if (swallow) {
			// see wther the new member fits the existing ones:
			if (this.members.find(function(mem) {
				return !mem.isCompatible(member);
			})) return false;
			this.isGrouped = true;
			this.members.push(member);
		}
		return true;
	},

	init: function() {
		if (this.isGrouped) {
			// see if all elements have the same amount of parameters
			var sameParamCount = true;
			var firstCount = -1;
			this.members.each(function(mem) {
				var count = mem.parameters().length;
				if (firstCount == -1) {
					firstCount = count;
				} else if (count != firstCount) {
					sameParamCount = false;
					throw $break;
				}
			});
			if (sameParamCount) {
				// find the suiting member: take the one with the most documentation
				var maxTags = -1;
				this.members.each(function(mem) {
					var numTags = mem.inlineTags().length;
					if (numTags > maxTags) {
						this.member = mem;
						maxTags = numTags;
					}
				}, this);
			} else {
				// now sort the members by param count:
				this.members = this.members.sortBy(function(mem) {
					return mem.parameters().length;
				});
				this.member = this.members.last;
			}
		} else {
			this.member = this.members.first;
		}
	},

	getNameSuffix: function() {
		return this.renderParameters();
	},

	getOverriddenMethodToUse: function() {
		if (this.member instanceof MethodDoc) {
			if (!this.member.commentText() &&
				!this.member.seeTags().length &&
				!this.member.throwsTags().length &&
				!this.member.paramTags().length) {
				// No javadoc available for this method. Recurse through
				// superclasses
				// and implemented interfaces to find javadoc of overridden
				// methods.
				var overridden = this.member.overriddenMethod();
				if (overridden) {
					var mem = Member.get(overridden);
					// If this method is not wrapped, quickly wrap it just to
					// call renderMember.
					// Prevent endless loops that happen when overriden
					// functions from inivisble classes where moved to the
					// derived class and Member.get lookup points there
					// instead of the overridden version:
					if (mem && mem.member.containingClass() != this.member.overriddenClass())
						mem = null;
					if (!mem)
						mem = new Method(this.classObject, overridden);
					return mem;
				}
			}
		}
	},

	renderSummary: function(classDoc) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden)
			overridden.renderSummary(classDoc);
		else
			this.base(classDoc);
	},

	renderMember: function(cd, index, member) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden)
			overridden.renderMember(cd, index, member);
		else
			return this.base(cd, index, member, this.containingClass());
	},

	getParameters: function() {
		var params = this.member.parameters();
		if (params.length) {
			// Link parameters to original parameter tags:
			var lookup = this.member.paramTags().each(function(tag) {
				this[tag.parameterName()] = tag;
			}, {});
			// Set the links
			params.each(function(param) {
				param.tag = lookup[param.name()];
			});
			return params;
		}
	},

	renderParameters: function() {
		if (!this.renderedParams) {
			var buf = [];
			buf.push('(');
			if (this.isGrouped) {
				var prevCount = 0;
				var closeCount = 0;
				this.members.each(function(mem) {
					var params = mem.parameters();
					var count = params.length;
					if (count > prevCount) {
						if (prevCount)
							buf.push('[');
						for (var i = prevCount; i < count; i++) {
							if (i) buf.push(', ');
							buf.push(params[i].name());
						}
						closeCount++;
						prevCount = count;
					}
				});
				for (var i = 1; i < closeCount; i++)
					buf.push(']');
			} else {
				var params = this.member.parameters();
				for (var i = 0; i < params.length; i++) {
					if (i) buf.push(', ');
					buf.push(params[i].name());
				}
			}
			buf.push(')');
			this.renderedParams = buf.join('');
		}
		return this.renderedParams;
	},
	
	name: function() {
		return this.methodName;
	},
	
	containingClass: function() {
		return this.classObject.classDoc;
	},

	containingPackage: function() {
		return this.classObject.classDoc.containingPackage();
	},

	signature: function() {
		return this.member.signature();
	},

	parameters: function() {
		return this.member.parameters();
	},

	returnType: function() {
		return this.member instanceof MethodDoc ?
				new Type(this.member.returnType()) : null;
	},

	isSimilar: function(obj) {
		if (obj instanceof Method)
			return this.isStatic() == obj.isStatic() &&
				this.methodName.equals(obj.name()) &&
				this.renderParameters().equals(obj.renderParameters());
		return false;
	}
});

/**
 * A virtual field that unifies getter and setter functions, just like Rhino does
 */
BeanProperty = Member.extend({
	initialize: function(classObject, name, getter, setter) {
		this.base(classObject);
		this.property = name;
		this.getter = getter;
		this.setter = setter;
		getter.bean = this;
		if (setter)
			setter.bean = this;

		var tags = getter.tags('jsbean');
		if (!tags.length && setter)
			tags = setter.tags('jsbean');

		this.inlineTagList = [];
		if (!setter)
			this.inlineTagList.push(new Tag('Read-only.'))
		this.inlineTagList.append(tags);
	},

	name: function() {
		return this.property;
	},

	firstSentenceTags: function() {
		return this.inlineTags;
	},

	isVisible: function() {
		var getterHide = this.getter.tags('jshide')[0];
		var setterHide = this.setter && this.setter.tags('jshide')[0];
		if (getterHide) getterHide = getterHide.text();
		if (setterHide) setterHide = setterHide.text();
		return !/^(bean|all|)$/.test(getterHide) && !/^(bean|all|)$/.test(setterHide);
	},

	isStatic: function() {
		return this.getter.isStatic();
	},

	containingClass: function() {
		return this.getter.containingClass();
	},

	inlineTags: function() {
		return this.inlineTagList;
	},

	seeTags: function() {
		return this.seeTagList;
	},

	containingPackage: function() {
		return this.getter.containingPackage();
	},

	modifiers: function() {
		return '';
	},

	tags: function(tagname) {
		return [];
	},

	returnType: function() {
		return this.getter.returnType();
	}
});

/**
 * A list of members that are unified under the same name 
 */
MemberList = Object.extend({
	initialize: function(classObject, name) {
		this.classObject = classObject;
		this.lists = [];
		// used in scanBeanProperties:
		this.name = name;
	},

	add: function(member, lookupName) {
		var mem = null;
		// only group functions
		if (member instanceof ExecutableMemberDoc) {
			var name = member.name();
			if (settings.methodFilter && member instanceof MethodDoc) {
				// filter out methods that are not allowed:
				if (settings.methodFilter.some(function(filter) {
					return filter.equals(name);
				})) return false;
			}

			// See if we can add to an existing Member:
			this.lists.each(function(m) {
				if (m.add(member)) {
					mem = m;
					throw $break;
				}
			});
			// couldn't add to an existing Member, create a new one:
			if (!mem) {
				mem = new Method(this.classObject, name);
				if (mem.add(member))
					this.lists.push(mem);
			}
		} else {
			if (member instanceof Member)
				mem = member;
			else
				mem = new Member(this.classObject, member);
			this.lists.push(mem);
		}
		if (mem) {
			// BeanProperties do not need to be put in the lookup table
			// They are the only ones passed as Member already, so there
			// will not be a qualifiedName funciton anyway...
			if (!lookupName && member.qualifiedName)
				lookupName = member.qualifiedName();
			if (lookupName)
				Member.put(lookupName, mem);
		}
		return mem != null;
	},

	init: function() {
		this.lists.each(function(mem) {
			mem.init();
		});
	},

	appendTo: function(list) {
		return list.append(this.lists);
	},

	extractGetMethod: function() {
		// Inspect the list of all MemberBox for the only one having no
		// parameters
		var name = this.name;
		return this.lists.find(function(method) {
			// Does getter method have an empty parameter list with a return
			// value (eg. a getSomething() or isSomething())?

			// As a convention, only add non static bean properties to
			// the documentation. static properties are all supposed to
			// be uppercae and constants.
			// TODO: Control this through a tag instead!
			if (method.parameters().length == 0 && !method.isStatic() && 
				method.returnType().typeName() != 'void')
					return method;
		});
	},

	extractSetMethod: function(type) {
		// Note: it may be preferable to allow
		// NativeJavaMethod.findFunction()
		//	   to find the appropriate setter; unfortunately, it requires an
		//	   instance of the target arg to determine that.

		// Make two passes: one to find a method with direct type
		// assignment,
		// and one to find a widening conversion.
		for (var pass = 1; pass <= 2; ++pass) {
			var found = this.lists.find(function(method) {
				var params = method.parameters();
				// As a convention, only add non static bean properties to
				// the documentation.
				// Static properties are all supposed to be uppercae and constants
				if (!method.isStatic() &&
					method.returnType().typeName() == 'void' && params.length == 1 &&
					pass == 1 && params[0].typeName() == type.typeName() ||
					pass == 2 /* TODO: && params[0].isAssignableFrom(type) */)
						return method;
			});
			if (found)
				return found;
		}
		return null;
	}
});

/**
 * A list of member lists, accessible by member name:
 */
MemberListGroup = Object.extend({
	initialize: function(classObject) {
		this.classObject = classObject;
		this.groups = new Hash();
		this.methodLookup = new Hash();
	},

	add: function(member) {
		var key = member.name(), name, group;
		if (member instanceof ExecutableMemberDoc) {
			name = key;
			if (member instanceof MethodDoc)
				// For methods, use the return type for grouping as well!
				var type = member.returnType();
				if (type)
					key = type.typeName() + type.dimension() + ' ' + name;

			group = this.groups[key]; 
			if (!group) {
				group = new MemberList(this.classObject, name); 
				this.methodLookup[name] = group;
			}
		} else {
			// Fields won't be grouped, but for simplicty,
			// greate groups of one element for each field,
			// so it can be treated the same as functions:
			group = new MemberList(this.classObject, key); 
		}
		group.add(member);
		this.groups[key] = group;
	},

	addAll: function(members) {
		members.each(function(member) {
			this.add(member);
		}, this);
	},

	init: function() {
		this.groups.each(function(group) {
			group.init();
		});
	},

	contains: function(key) {
		return this.groups[key] != null;
	},

	getFlattened: function() {
		if (!this.flatList) {
			// now sort the lists alphabetically
			var sorted = this.groups.sortBy(function(group) {
				var name = group.name, ch = name[0];
				// Swap the case of the first char so the sorting shows
				// lowercase members first
				if (ch.isLowerCase()) ch = ch.toUpperCase();
				else ch = ch.toLowerCase();
				return ch + name.substring(1);
			});
			// flatten the list of groups:
			this.flatList = sorted.each(function(group) {
				group.appendTo(this);
			}, []);
		}
		return this.flatList;
	},

	scanBeanProperties: function(fields) {
		this.groups.each(function(member) {
			var name = member.name;
			// Is this a getter?
			var m = name.match(/^(get|is)(.*)$/), kind = m && m[1], component = m && m[2];
			if (component && kind == 'get' || kind == 'is') {
				// Find the bean property name.
				var property = component;
				var match = component.match(/^([A-Z])([a-z]+.*|$)/);
				if (match) property = match[1].toLowerCase() + match[2];
				// If we already have a member by this name, don't do this
				// property.
				if (!fields.contains(property)) {
					var getter = member.extractGetMethod(), setter = null;
					if (getter) {
						// We have a getter. Now, do we have a setter?
						var setters = this.methodLookup['set' + component];
						// Is this value a method?
						if (setters)
							setter = setters.extractSetMethod(getter.returnType());
						// Make the property.
						var bean = new BeanProperty(this.classObject, property, getter, setter);
						if (bean.isVisible())
							fields.add(bean);
					}
				}
			}
		}, this);
	},

	contains: function(name) {
		return this.groups.has(name);
	}
});


// ClassObject

ClassObject = Object.extend({
	initialize: function(cd) {
		this.classDoc = cd;
		// for the hierarchy:
		this.children = new Hash();
	},

	init: function() {
		this.fieldLists = new MemberListGroup(this);
		this.methodLists = new MemberListGroup(this);
		this.constructorLists = new MemberListGroup(this);
		this.add(this.classDoc, true);
		var superclass = this.classDoc.superclass();
		// Add the members of direct invisible superclasses to
		// this class for JS documentation:
		while (superclass && !superclass.isVisible() &&
				!superclass.qualifiedName().equals('java.lang.Object')) {
			this.add(superclass, false);
			superclass = superclass.superclass();
		}
		// now scan for beanProperties:
		this.methodLists.init();
		this.constructorLists.init();

		if (this.classDoc.name() != 'global')
			this.methodLists.scanBeanProperties(this.fieldLists);
	},

	add: function(cd, addConstructors) {
		this.fieldLists.addAll(cd.fields(true));
		this.methodLists.addAll(cd.methods(true));
		if (addConstructors)
			this.constructorLists.addAll(cd.constructors(true));
	},

	methods: function() {
		return this.methodLists.getFlattened();
	},

	fields: function() {
		return this.fieldLists.getFlattened();
	},

	constructors: function() {
		return this.constructorLists.getFlattened();
	},

	name: function() {
		var name = this.classDoc.name();
		return name == 'global' ? 'Global Scope' : name;
	},

	qualifiedName: function() {
		return this.classDoc.qualifiedName();
	},

	hasSimilar: function(member) {
		(member instanceof ExecutableMemberDoc ? this.methods()
				: this.fields()).each(function(other) {
			if (member.isSimilar(other))
				return true;
		});
		return false;
	},

	renderClass: function() {
		var cd = this.classDoc, index = null;
		if (settings.templates)
			index = [ '"prototype": { title: "' + cd.name() + '", text: "' +
					encodeJs(renderTags({ classDoc: cd, tags: cd.inlineTags() })) + '" }' ];

		// Determine folder + filename for class file:
		var name = this.name();
		var className = cd.name();
		// name and className might differ, e.g. for global -> Global Scope!
		var path = cd.qualifiedName();
		// cut away name:
		path = path.substring(0, path.length - className.length);
		path = getRelativeIdentifier(path);
		var doc = new Document(path, className, 'document');
		// from now on, the global out writes to doc

		this.renderTemplate('class', {}, out);

		if (cd.isInterface()) {
			var subInterfaces = [];
			var implementingClasses = [];
			root.classes().each(function(cls) {
				if (cls.interfaces().find(function(inter) {
					return inter.equals(cd);
				})) (cls.isInterface() ? subInterfaces : implementingClasses).push(cls);
			});
			this.renderTemplate('class#interface', {
				subInterfaces: subInterfaces,
				implementingClasses: implementingClasses
			}, out);
		}

		var fields = this.fields();
		var constructors = this.constructors();
		var methods = this.methods();

		if (settings.summaries) {
			if (settings.fieldSummary)
				this.renderSummaries(cd, fields, 'Field summary');

			if (settings.constructorSummary)
				this.renderSummaries(cd, constructors, 'Constructor summary');

			this.renderSummaries(cd, methods, 'Method summary');
		}

		// Filter members into static and non-static ones:
		function separateStatic(members) {
			return members.each(function(member) {
				this[member.isStatic() ? 1 : 0].push(member);
			}, [[], []]);
		}

		this.renderMembers({ classDoc: cd, members: constructors, title: 'Constructors', index: index });

		fields = separateStatic(fields);
		this.renderMembers({ classDoc: cd, members: fields[0], title: 'Properties', index: index });
		this.renderMembers({ classDoc: cd, members: fields[1], title: 'Static Properties', index: index });

		methods = separateStatic(methods);
		this.renderMembers({ classDoc: cd, members: methods[0], title: 'Functions', index: index });
		this.renderMembers({ classDoc: cd, members: methods[1], title: 'Static Functions', index: index });

		if (settings.inherited) {
			var first = true;
			var superclass = cd.superclass();

			var classes = [];
			while (superclass && !superclass.qualifiedName().equals('java.lang.Object')) {
				if (superclass.isVisible()) {
					var superProxy = ClassObject.get(superclass);
					fields = separateStatic(superProxy.fields());
					methods = separateStatic(superProxy.methods());
					var inherited = [], that = this;

					function addNonSimilar(members) {
						members.each(function(member) {
							if (member.isVisible() && !that.hasSimilar(member))
								inherited.push(member);
						});
					}
					// First non-static, then static:
					addNonSimilar(fields[1]);
					addNonSimilar(fields[0]);
					addNonSimilar(methods[1]);
					addNonSimilar(methods[0]);

					if (inherited.length)
						classes.push({ classDoc: superclass, members: inherited });
				}
				superclass = superclass.superclass();
			}
			this.renderTemplate('class#inheritance', { classes: classes }, out);
		}
		doc.close();
		return index;
	},

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	renderMembers: function(param) {
		this.renderTemplate('members', param, out);
	},

	/**
	 * Produces a constructor/method summary.
	 * 
	 * @param members The fields to be summarized.
	 * @param title The title of the section.
	 */
	renderSummaries: function(cd, members, title) {
		// Only show each name once, and only if the member is visible:
		var prevName;
		members = members.select(function(member) {
			var name = member.name();
			if (member.isVisible()) {
				var sel = name != prevName;
				prevName = name;
				return sel;
			}
		});
		this.renderTemplate('summaries', {
			members: members, title: title, classDoc: cd
		}, out);
	},

	statics: {
		put: function(root) {
			root.classes().each(function(cd) {
				var add = true;
				var name = cd.qualifiedName();
				if (settings.filterClasses)
					add = !settings.filterClasses.find(function(filter) {
						return filter == name || filter.endsWith('*') &&
							name.startsWith(filter.substring(0, filter.length - 1));
					});
				// Do not add any of Enums, since they are represented
				// as strings in the scripting environment.
				if (cd.hasSuperclass('java.lang.Enum'))
					add = false;
				if (add)
					this.classes[name] = new ClassObject(cd);
			}, this);
			// Now initialize them. init needs all the others to be there,
			// due to bean prop stuff
			this.classes.each(function(mem, i) {
				mem.init();
			});
		},

		get: function(param) {
			if (param instanceof ClassDoc)
				param = param.qualifiedName();
			return this.classes[param]
		},

		renderLink: function(name, qualifiedName) {
			var mem = this.get(qualifiedName);
			// use renderClassLink, as renderLink might have been overridden
			// by new Type(...)
			return mem && mem.classDoc ? mem.classDoc.renderClassLink() : name;
		},

		classes: new Hash()
	},

	addChild: function(mem) {
		this.children[mem.qualifiedName()] = mem;
	},

	removeChild: function(mem) {
		delete this.children[mem.qualifiedName()];
	},

	renderHierarchy: function(prepend) {
		var sorted = this.children.sortBy(function(mem) {
			return settings.classOrder[mem.name()] || Number.MAX_VALUE;
		});
		out.push();
		sorted.each(function(cls) {
			var cd = cls.classDoc;
			var index = cls.renderClass();
			this.renderTemplate('packages#class', {
				index: index ? index.join(', ') : null, cls: cls
			}, out);
			cls.renderHierarchy();
		});
		this.renderTemplate('packages#classes', { classes: out.pop() }, out);
	}
});

Test = Document = Object.extend({
	initialize: function(path, name, template) {
		this.name = name;
		this.template = template;

		// Split into packages and create subdirs:
		var parts = path.split(/\./);
		if (!settings.templates)
			parts.unshift('packages');
		path = '';
		var levels = 0;
		parts.each(function(part) {
			if (part == name || !part)
				throw $break;

			path += part + '/';
			levels++;
			var dir = new java.io.File(settings.destDir + path);
			if (!dir.exists())
				dir.mkdir();
		});

		this.basePath = '';
		for (var j = 1; j < levels; j++)
			this.basePath += '../';

		// Store the previous base
		this.previousBase = Document.basePath;
		// And set the current base
		Document.basePath = this.basePath;

		// Push out so the content for the document can be written to it.
		out.push();

		// Only add extension if it wasn't already
		var fileName = name.indexOf('.') != -1 ? name :
				name + (settings.templates ? '.jstl' : '.html');

		this.writer = new java.io.PrintWriter(
				new java.io.FileWriter(settings.destDir + path + fileName));
	},

	close: function() {
		this.content = out.pop();
		if (this.writer) {
			this.writer.print(this.renderTemplate(this.template));
			this.writer.close();
		}
		// Restore previous base
		Document.basePath = this.previousBase;
	},

	statics: {
		basePath: '',

		getBasePath: function() {
			return this.basePath;
		}
	}
});

/**
 * Produces a table-of-contents for classes and calls layoutClass on each class.
 */
function processClasses(classes) {
	var root = new ClassObject();

	// Loop twice, as in the second loop, superclasses are picked from nodes
	// which is filled in the firs loop
	classes.each(function(cd) {
		var cls = ClassObject.get(cd);
		if (cls) {
			cd.classObj = cls;
			root.addChild(cls);
		}
	});

	classes.each(function(cd) {
		var cls = cd.classObj;
		if (cls && cd.superclass()) {
			var superclass = cd.superclass().classObj;
			if (superclass) {
				root.removeChild(cls);
				superclass.addChild(cls);
			}
		}
	});
	root.renderHierarchy('');
}

function getRelativeIdentifier(str) {
	return str.startsWith(settings.basePackage + '.') ?
			str.substring(settings.basePackage.length + 1) : str;
}

function renderLink(qualifiedName, name, anchor, title) {
	if (settings.hyperref) {
		var str = '<a href="';
		if (qualifiedName) {
			var path = getRelativeIdentifier(qualifiedName).replace('.', '/');
			// Link to the index file for packages
			if (name.charAt(0).isLowerCase() && !name.equals('global'))
				path += '/index';
			if (settings.templates)
				path = '/reference/' + path.toLowerCase() + '/';
			else
				path = Document.getBasePath() + path + '.html';
			str += path;
		}
		if (anchor) {
			str += '#' + anchor;
			str += '" onClick="return toggleMember(\'' + anchor + '\', true);';
		}
		return str + '">' + title + '</a>';
	} else {
	 	return title;
	}
}

function encodeJs(str) {
	return str ? (str = uneval(str)).substring(1, str.length - 1) : str;
}

function encodeHtml(str) {
	// Encode everything
	str = Packages.org.htmlparser.util.Translate.encode(str);
	var tags = {
		code: true, br: true, p: true, b: true, a: true, i: true,
		ol: true, li: true, ul: true, tt: true, pre: true };
	// Now replace allowed tags again.
	return str.replace(/&lt;(\/?)(\w*)(.*?)(\s*\/?)&gt;/g, function(match, open, tag, content, close) {
		tag = tag.toLowerCase();
		return tags[tag] ? '<' + open + tag + content + close + '>' : match;
	});
}

/**
 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
 */
function renderTags(param) {
	return renderTemplate('tags', param);
}

function tags_macro(param) {
	// Do not use prefix / suffix for the tag loop here, as we're in a macro where
	// these are already applied
	delete param.prefix;
	delete param.suffix;
	return renderTags(param);
}

function code_filter(str) {
	return '<tt>' + str + '</tt>';
}

function tags_filter(str) {
	// Replace inline <code></code> with <tt></tt>
	str = str.replace(/<code>[ \t]*([^\n\r]*?)[ \t]*<\/code>/g, function(match, content) {
		return '<tt>' + content + '</tt>';
	});
	// Put code tags on the same line as the content, as white-space: pre is set:
	str = str.replace(/<code>\s*([\s\S]*?)\s*<\/code>/g, function(match, content) {
		return '<code>' + content + '</code>';
	});
	// Automatically put <br /> at the end of sentences.
	str = str.replace(/([.:])\s*(\n|\r\n)/g, function(match, before, lineBreak) {
		return before + '<br />' + lineBreak;
	});
	return str;
}

function stripTags_fitler(str) {
	return str.replace(/<.*?>|<\/.*?>/g, ' ').replace(/\\s+/g, ' ');
}

function main() {
	ClassObject.put(root);

	var packages = new Hash();
	var packageSequence = settings.packageSequence;
	var createSequence = !packageSequence;
	if (createSequence)
		packageSequence = [];

	root.specifiedPackages().each(function(pkg) {
		var name = pkg.name();
		packages[name] = pkg;
		if (createSequence)
			packageSequence[i] = name;
	});

	// Now start rendering:
	var doc = new Document('', settings.templates ? 'packages.js'
			: 'packages.html', 'packages');

	packageSequence.each(function(name) {
		var pkg = packages[name];
		if (pkg) {
			var path = getRelativeIdentifier(name);
			var text = renderTags({ tags: pkg.inlineTags() });
			var first = renderTags({ tags: pkg.firstSentenceTags() });
			// Remove the first sentence from the main text, and use it as a title
			if (first && text.startsWith(first)) {
				text = text.substring(first.length);
				first = first.substring(0, first.length - 1); // cut away dot
			}

			out.push();
			processClasses(pkg.interfaces());
			processClasses(pkg.allClasses(true));
			processClasses(pkg.exceptions());
			processClasses(pkg.errors());

			renderTemplate('packages#package', {
				content: out.pop(), name: name, path: path, text: text
			}, out);

			if (!settings.templates) {
				// Write package file:
				var index = new Document(path, 'index', 'document');
				renderTemplate('package', { title: first, text: text }, out);
				index.close();
			}
		}
	});
	doc.close();
}

main();
