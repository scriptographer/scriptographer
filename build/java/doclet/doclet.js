importPackage(Packages.com.sun.javadoc);
importPackage(Packages.com.sun.tools.javadoc);

include("bootstrap.js");
include("template.js");

function error() {
	java.lang.System.err.println($A(arguments).join(', '));
}

// A global template writer
var out = new TemplateWriter();

// Add renderTemplate function with caching to all objects
Object.inject(function() {
	var templates = new Hash();

	return {
		getTemplate: function(template) {
			var name = template;
			if (!(template instanceof Template)) {
				// Handle sub templates:
				var pos = name.indexOf('#');
				if (pos != -1) {
					template = this.getTemplate(name.substring(0, pos));
					if (template)
						return template.getSubTemplate(name.substring(pos + 1));
				}
				template = templates[name];
			}
			if (!template)
				template = templates[name] = new Template(
					new java.io.File(baseDir + "/templates/" + name + ".jstl"));
			return template;
		},

		renderTemplate: function(template, param, out) {
			try {
				template = this.getTemplate(template);
				if (template)
					return template.render(this, param, out);
			} catch (e) {
				error(e);
			}
		},

		template_macro: function(param, name) {
			if (name[0] == '#') {
				return param.__template__.renderSubTemplate(this, name.substring(1), param);
			} else {
				return this.renderTemplate(name, param);
			}
		}
	}
}, true);

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
	basePackage:  options.basepackage || "",
	destDir: (options.d + (options.d && !/\/^/.test(options.d) ? "/" : "")) || "",
	docTitle: options.doctitle || "",
	bottom: options.bottom || "",
	author: options.author || "",
	filterClasses: (options.filterclasses || "").trim().split(/\s+/),
	packageSequence: (options.packagesequence || "").trim().split(/\s+/),
	methodFilter: (options.methodfilter || "").trim().split(/\s+/),
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
	templates: options.templates == "true",
	inherited: options.noinherited != "true",
	summaries: options.nosummaries != "true",
	fieldSummary: options.nofieldsummary != "true",
	constructorSummary: options.noconstructorsummary != "true",
	hyperref: options.nohyperref != "true",
	versionInfo: options.version == "true",
	debug: options.shortinherited == "true",
	section1Open: options.section1open || "<h1>",
	section1Close: options.section1close || "</h1>",
	section2Open: options.section2open || "<h2>",
	section2Close: options.section2close || "</h2>"
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
				return f.hasInterface(face);
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
		if (this.hasInterface("com.scratchdisk.util.SimpleList"))
			return "Normal List";
		else if (this.hasInterface("com.scratchdisk.util.StringIndexList"))
			return "String-index List";
		else if (this.hasInterface("com.scratchdisk.util.ReadOnlyList"))
			return "Read-only List";
	},

	getType: function() {
		var type;
		if (this.isInterface()) type = "Interface";
		else if (this.isException()) type = "Exception";
		else type = "Prototype";
		if (this.isAbstract()) type = "Abstract " + type;
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
		var str = "";
		if (this.isVisible()) {
			if (this.isAbstract())
				str += "<i>";
			str += renderLink(this.qualifiedName(), this.name(), "", name);
			if (this.isAbstract())
				str += "</i>";
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
	_extended: true,

	$constructor: function(type) {
		// Enhance the prototype of the native object with Type.prototype, and return
		// type instead of this!
		// We need to do this because it is not possible to access the native
		// class for types. Sometimes they seem to just be ClassDocImpls
		if (!type._extended) type.inject(this.__proto__);
		return type;
	},

	isNumber: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass("java.lang.Number") ||
			/^(short|int|double|float)$/.test(this.typeName());
	},
	
	isBoolean: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass("java.lang.Boolean") ||
			this.typeName() == "boolean";
	},

	isArray: function() {
		var cd = this.asClassDoc();
		return /\[.*\]/.test(this.typeName()) || cd &&
			(cd.hasInterface("java.util.Collection") ||
			cd.hasSuperclass("org.mozilla.javascript.NativeArray"));
	},
	
	isMap: function() {
		var cd = this.asClassDoc();
		return cd && (cd.hasInterface("java.util.Map") ||
			cd.hasSuperclass("org.mozilla.javascript.NativeObject"));
	},
	
	isPoint: function() {
		var cd = this.asClassDoc();
		return cd && (cd.hasSuperclass("java.awt.geom.Point2D") ||
			cd.hasSuperclass("java.awt.Dimension"));
	},

	isRectangle: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass("java.awt.geom.Rectangle2D");
	},

	isMatrix: function() {
		var cd = this.asClassDoc();
		return cd && cd.hasSuperclass("java.awt.geom.AffineTransform");
	},
	
	isCompatible: function(type) {
		var cd1 = this.asClassDoc(), cd2 = type.asClassDoc();
		return this.typeName() == type.typeName() ||
			cd1 && cd2 && (cd1.subclassOf(cd2) || cd2.subclassOf(cd1)) ||
			this.isNumber() && type.isNumber() ||
			this.isBoolean() && type.isBoolean() ||
			this.isArray() && type.isArray() ||
			this.isMap() && type.isMap() ||
			this.isPoint() && type.isPoint() ||
			this.isRectangle() && type.isRectangle() ||
			this.isMatrix() && type.isMatrix();
	},

	renderLink: function() {
		if (this.isNumber())
			return "<tt>Number</tt>";
		else if (this.isBoolean())
			return "<tt>Boolean</tt>";
		else if (this.isArray())
			return "<tt>Array</tt>";
		else if (this.isMap())
			return "<tt>Object</tt>";
		else if (this.isPoint())
			return ClassObject.renderLink("Point", "com.scriptographer.ai.Point");
		else if (this.isRectangle())
			return ClassObject.renderLink("Rectangle", "com.scriptographer.ai.Rectangle");
		else if (this.isMatrix())
			return ClassObject.renderLink("Matrix", "com.scriptographer.ai.Matrix");
		else {
			var cls = this.asClassDoc();
			if (cls) {
				if (cls.isVisible())
					return cls.renderClassLink();
				else
					return "<tt>" + cls.name() + "</tt>";
			} else {
				return "<tt>" + this.typeName() + "</tt>";
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
			// cut away ":" and everything that follows:
			int pos = text.indexOf(':');
			if (pos >= 0) {
				text = text.substring(0, pos) + ".";
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
			return ref.renderLink(param.classDoc);
		} else {
			error(this.position() + ": warning - @link contains undefined reference: " + this);
		}
	}
});

// A fake tag, to define own tag lists. Used for bean properties

Tag = Object.extend({
	$constructor: function(str) {
		this.str = str;
	},

	text_macro: function() {
		return this.str;
	}
});

// Parameter helpers

// Fix a bug in the Type returned by Parameter.type(), where toString does not return []
// for arrays and there does not seem to be another way to find out if it's an array or not...

ParameterImpl.inject({
	paramType: function() {
		var type = new Type(this.type());
		var name = this.typeName();
		type.typeName = function() {
			return name;
		}
		return type;
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
		// fields cannot be grouped
		return false;
	},

	isVisible: function() {
		return Member.get(this.qualifiedName()) != null;
	},

	renderLink: function(current) {
		var mem = Member.get(this);
		return mem ? mem.renderLink(current) : "";
	}

});

// Member

Member = Object.extend({
	$constructor: function(classObject, member) {
		this.classObject = classObject;
		this.member = member;
	},

	init: function() {
		// nothing here, but in the extended classes
	},

	isVisible: function() {
		var hide = this.tags("jshide")[0];
		return this.bean && hide == 'false' || !this.bean && (hide == undefined || hide == 'false');
	},

	renderMember: function(classDoc, index, member, containingClass) {
		if (this.isVisible()) {
			if (!member)
				member = this;

			if (!containingClass)
				containingClass = this.classDoc;

			if (index)
				index.push('"' + member.getId() + '": { title: "' + this.name() + '", text: "' + encodeJs(renderTags({ classDoc: this.classDoc, tags: member.inlineTags() })) + '" }');

			// Thrown exceptions
			// if (this.member.thrownExceptions)
			//	renderTemplate("exceptions", { exceptions: this.member.thrownExceptions() }, out);
			// Description
			var returnType = this.returnType();
			return this.renderTemplate("member", {
				classDoc: classDoc, member: member, containingClass: containingClass,
				throwsTags: this.member && this.member.throwsTags ? this.member.throwsTags() : null,
				returnType: returnType && !returnType.typeName().equals("void") ? returnType : null
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
		return this.member.isStatic();
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
		return "";
	},

	getNameSuffix: function() {
		return "";
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
		return this.renderTemplate("summary", { classDoc: classDoc });
	},

	getId: function() {
		return this.name() + this.signature();
	},

	getClass: function(current) {
		// in case the class is invisible, the current class needs to be used instead
		var containing = this.containingClass();
		return (containing.isVisible() || current.superclass() != containing) ? containing : current;
	},

	renderLink: function(current) {
		var cd = this.getClass(current);
		// dont use mem.qualifiedName(). use cd.qualifiedName() + "." + mem.name()
		// instead in order to catch the case where functions are moved from invisible
		// classes to visible ones (e.g. AffineTransform -> Matrix)
		return renderLink(cd.qualifiedName(), cd.name(), this.getId(), this.name() + this.getNameSuffix());
	},

	isSimilar: function(mem) {
		return this.isStatic() == mem.isStatic() && this.name().equals(mem.name());
	},

	$static: {
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
 * A group of members that are all "compatible" in a JS way, e.g. have the same
 * amount of parameter with different types each (e.g. setters)
 * or various amount of parameters with default parameter versions, e.g.
 * all com.scriptogrpaher.ai.Pathfinder functions
 */
Method = Member.extend({
	$constructor: function(classObject, param) {
		this.$super(classObject);
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
				this.member = this.members.last();
			}
		} else {
			this.member = this.members.first();
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
					// prevent endless loops:
					// if this method is not wrapped, quickly wrap it just to call renderMember
					// prevent endless loops that happen when overriden functions from inivisble classes
					// where moved to the derived class and Member.get lookup points there instead of
					// the overridden version:
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
			this.$super(classDoc);
	},

	renderMember: function(cd, index, member) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden)
			overridden.renderMember(cd, index, member);
		else
			return this.$super(cd, index, member, this.containingClass());
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
			buf.push("(");
			if (this.isGrouped) {
				var prevCount = 0;
				var closeCount = 0;
				this.members.each(function(mem) {
					var params = mem.parameters();
					var count = params.length;
					if (count > prevCount) {
						if (prevCount)
							buf.push("[");
						for (var i = prevCount; i < count; i++) {
							if (i) buf.push(", ");
							buf.push(params[i].name());
						}
						closeCount++;
						prevCount = count;
					}
				});
				for (var i = 1; i < closeCount; i++)
					buf.push("]");
			} else {
				var params = this.member.parameters();
				for (var i = 0; i < params.length; i++) {
					if (i) buf.push(", ");
					buf.push(params[i].name());
				}
			}
			buf.push(")");
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
		return this.member instanceof MethodDoc ? new Type(this.member.returnType()) : null;
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
	$constructor: function(classObject, name, getter, setter) {
		this.$super(classObject);
		this.property = name;
		this.getter = getter;
		this.setter = setter;
		getter.bean = this;
		if (setter)
			setter.bean = this;

		var tags = getter.tags("jsbean");
		if (!tags.length && setter)
			tags = setter.tags("jsbean");
		
		this.inlineTagList = [];
		if (!setter)
			this.inlineTagList.push(new Tag("Read-only."))
		this.inlineTagList.append(tags);
	},

	name: function() {
		return this.property;
	},

	firstSentenceTags: function() {
		return this.inlineTags;
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
		return "";
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
	$constructor: function(classObject, name) {
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

			// as a convention, only add non static bean properties to the documentation.
			// static properties are all supposed to be uppercae and constants
			return method.parameters().length == 0 && !method.isStatic() && 
					method.returnType().typeName() != "void"
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
				// as a convention, only add non static bean properties to the documentation.
				// static properties are all supposed to be uppercae and constants
				return !method.isStatic() &&
					method.returnType().typeName() == "void" && params.length == 1 &&
					pass == 1 && params[0].typeName() == type.typeName() ||
					pass == 2 /* TODO: && params[0].isAssignableFrom(type) */;
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
	$constructor: function(classObject) {
		this.classObject = classObject;
		this.groups = new Hash();
		this.methodLookup = new Hash();
	},

	add: function(member) {
		var key = member.name(), name, group;
		if (member instanceof ExecutableMemberDoc) {
			name = key;
			if (member instanceof MethodDoc)
				// for methods, use the return type for grouping as well!
				key = member.returnType().typeName() + " " + name;

			group = this.groups[key]; 
			if (!group) {
				group = new MemberList(this.classObject, name); 
				this.methodLookup[name] = group;
			}
		} else {
			// fields won't be grouped, but for simplicty,
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
				// swap the case of the first char so the sorting shows lowercase members first
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
			if (component && kind == "get" || kind == "is") {
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
						var setters = this.methodLookup["set" + component];
						// Is this value a method?
						if (setters)
							setter = setters.extractSetMethod(getter.returnType());
						// Make the property.
						fields.add(new BeanProperty(this.classObject, property, getter, setter));
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
	$constructor: function(cd) {
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
		// add the members of direct invisible superclasses to this class for JS documentation:
		while (superclass && !superclass.isVisible() &&
				!superclass.qualifiedName().equals("java.lang.Object")) {
			this.add(superclass, false);
			superclass = superclass.superclass();
		}
		// now scan for beanProperties:
		this.methodLists.init();
		this.constructorLists.init();

		if (this.classDoc.name() != "global")
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
		return name == "global" ? "Global Scope" : name;
	},

	qualifiedName: function() {
		return this.classDoc.qualifiedName();
	},

	hasSimilar: function(member) {
		(member instanceof ExecutableMemberDoc ? this.methods() : this.fields()).each(function(other) {
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

		this.renderTemplate("class", {}, out);

		if (cd.isInterface()) {
			var subInterfaces = [];
			var implementingClasses = [];
			root.classes().each(function(cls) {
				if (cls.interfaces().find(function(inter) {
					return (inter.equals(cd))
				})) 
					(cls.isInterface() ? subInterfaces : implementingClasses).push(cls);
			});
			this.renderTemplate("class#interface", { subInterfaces: subInterfaces, implementingClasses: implementingClasses }, out);
		}

		var fields = this.fields();
		var constructors = this.constructors();
		var methods = this.methods();

		if (settings.summaries) {
			if (settings.fieldSummary)
				this.renderSummaries(cd, fields, "Field summary");

			if (settings.constructorSummary)
				this.renderSummaries(cd, constructors, "Constructor summary");

			this.renderSummaries(cd, methods, "Method summary");
		}

		// Filter members into static and non-static ones:
		function separateStatic(members) {
			return members.each(function(member) {
				this[member.isStatic() ? 1 : 0].push(member);
			}, [[], []]);
		}

		this.renderMembers({ classDoc: cd, members: constructors, title: "Constructors", index: index });

		fields = separateStatic(fields);
		this.renderMembers({ classDoc: cd, members: fields[0], title: "Properties", index: index });
		this.renderMembers({ classDoc: cd, members: fields[1], title: "Static Properties", index: index });

		methods = separateStatic(methods);
		this.renderMembers({ classDoc: cd, members: methods[0], title: "Functions", index: index });
		this.renderMembers({ classDoc: cd, members: methods[1], title: "Static Functions", index: index });

		if (settings.inherited) {
			var first = true;
			var superclass = cd.superclass();

			var classes = [];
			while (superclass && !superclass.qualifiedName().equals("java.lang.Object")) {
				if (superclass.isVisible()) {
					var superProxy = ClassObject.get(superclass);
					fields = separateStatic(superProxy.fields());
					methods = separateStatic(superProxy.methods());
					var inherited = [], that = this;

					function addNonSimilar(members) {
						members.each(function(member) {
							if (!that.hasSimilar(member))
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
			this.renderTemplate("class#inheritance", { classes: classes }, out);
		}
		doc.close();
		return index;
	},

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	renderMembers: function(param) {
		this.renderTemplate("members", param, out);
	},

	/**
	 * Produces a constructor/method summary.
	 * 
	 * @param members The fields to be summarized.
	 * @param title The title of the section.
	 */
	renderSummaries: function(cd, members, title) {
		this.renderTemplate("summaries", { members: members, title: title, classDoc: cd }, out);
	},

	$static: {
		put: function(root) {
			root.classes().each(function(cd) {
				var add = true;
				var name = cd.qualifiedName();
				if (settings.filterClasses)
					add = !settings.filterClasses.find(function(filter) {
						return (filter == name || filter.endsWith("*") &&
							name.startsWith(filter.substring(0, filter.length - 1)));
					});
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
			return mem ? mem.classDoc.renderClassLink() : "<tt>" + name + "</tt>";
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
			this.renderTemplate("packages#class", { index: index ? index.join(', ') : null, cls: cls }, out);
			cls.renderHierarchy();
		});
		this.renderTemplate("packages#classes", { classes: out.pop() }, out);
	}
});

Document = Object.extend({
	$constructor: function(path, name, template) {
		this.name = name;
		this.template = template;

		// Split into packages and create subdirs:
		var parts = path.split(/\./);
		if (!settings.templates)
			parts.unshift("packages");
		path = "";
		var levels = 0;
		parts.each(function(part) {
			if (part == name || !part)
				throw $break;

			path += part + "/";
			levels++;
			var dir = new java.io.File(settings.destDir + path);
			if (!dir.exists())
				dir.mkdir();
		});

		this.base = '';
		for (var j = 1; j < levels; j++)
			this.base += '../';

		// Store the previous base
		this.previousBase = Document.base;
		// And set the current base
		Document.base = this.base;

		// Push out so the content for the document can be written to it.
		out.push();

		// Only add extension if it wasn't already
		var fileName = name.indexOf(".") != -1 ? name :
				name + (settings.templates ? ".jstl" : ".html");

		this.writer = new java.io.PrintWriter(new java.io.FileWriter(settings.destDir + path + fileName));
	},

	close: function() {
		this.content = out.pop();
		this.writer.print(this.renderTemplate(this.template));
		this.writer.close();
		// Restore previous base
		Document.base = this.previousBase;
	},

	$static: {
		base: '',

		getBase: function() {
			return this.base;
		}
	}
});

/**
 * Produces a table-of-contents for classes and calls layoutClass on each class.
 */
function processClasses(classes) {
	var root = new ClassObject();

	// loop twice, as in the second loop, superclasses are picked from nodes which is filled in the firs loop
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
	root.renderHierarchy("");
}

/**
 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
 */
function renderTags(param) {
	return renderTemplate("tags", param);
}

function tags_macro(param) {
	return renderTemplate("tags", param);
}

function getRelativeIdentifier(str) {
	return str.startsWith(settings.basePackage + ".") ?
			str.substring(settings.basePackage.length + 1) : str;
}

function renderLink(qualifiedName, name, anchor, title) {
	var str = '<tt>';
	if (settings.hyperref) {
		str += '<a href="';
		if (qualifiedName) {
			var path = getRelativeIdentifier(qualifiedName).replace('.', '/');
			// link to the index file for packages
			if (name.charAt(0).isLowerCase() && !name.equals("global"))
				path += '/index';
			if (settings.templates)
				path = '/Reference/' + path + '/';
			else
				path = Document.getBase() + path + '.html';
			str += path;
		}
		if (anchor) {
			if (settings.templates) str += anchor + '/';
			str += '#' + anchor;
			str += '" onClick="return toggleMember(\'' + anchor + '\', true);';
		}
		str += '">' + title + '</a>';
	} else {
		str += title;
	}
	str += '</tt>';
	return str;
}

function encodeJs(str) {
	return str ? (str = uneval(str)).substring(1, str.length - 1) : str;
}

function stripCodeTags_filter(str) {
	return str.replace(/<tt>|<\/tt>/g, "");
}

function stripTags_fitler(str) {
	return str.replace(/<.*?>|<\/.*?>/g, " ").replace(/\\s+/g, " ");
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

	// now start rendering:
	var doc = new Document("", settings.templates ? "packages.js" : "packages.html", "packages");

	packageSequence.each(function(name) {
		var pkg = packages[name];
		if (pkg) {
			var path = getRelativeIdentifier(name);
			var text = renderTags({ tags: pkg.inlineTags() });
			var first = renderTags({ tags: pkg.firstSentenceTags() });
			// remove the first sentence from the main text
			if (first && text.startsWith(first)) {
				text = text.substring(first.length);
				first = first.substring(0, first.length - 1); // cut away dot
			}

			out.push();
			processClasses(pkg.interfaces());
			processClasses(pkg.allClasses(true));
			processClasses(pkg.exceptions());
			processClasses(pkg.errors());

			renderTemplate("packages#package", { content: out.pop(), name: name, path: path }, out);

			if (!settings.templates) {
				// write package file:
				var doc = new Document(path, 'index', 'document');
				renderTemplate("package", { title: first, text: text });
				doc.close();
			}
		}
	});
	doc.close();
}

main();