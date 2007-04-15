importPackage(Packages.com.sun.javadoc);
importPackage(Packages.com.sun.tools.javadoc);
importPackage(Packages.com.scriptographer.doclets);

include("bootstrap.js");
include("template.js");

function error() {
	java.lang.System.err.println($A(arguments).join(', '));
}

Function.inject({
	toRender: function() {
		var that = this;
		return function() {
			// look at the last argument. if it's null,
			// render into a string.
			var last = arguments.length - 1;
			var asString = !arguments[last];
			if (asString) {
				res.push();
				// wee need to add res as the last argument to the call bellow:
				if (last < 0) { // empty argument list
					// this seems to execute faster than the creation
					// of a new array, and at least in Rhino, it works!
					arguments[0] = res;
					arguments.length = 1;
				} else { // just set the last item
					arguments[last] = res;
				}
			}
			that.apply(this, arguments);
			// return the string if required
			if (asString)
				return res.pop();
		}
	}
});

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

// add renderTemplate function with caching to all objects
Object.inject(function() {
	var templates = new Hash();

	return {
		renderTemplate: function(name, param) {
			var template;
			if (name instanceof Template) {
				template = name;
			} else {
				template = templates[name];
				if (!template)
					template = templates[name] = new Template(
						new java.io.File(baseDir + "/templates/" + name + ".jstl"));
			}
			return template.render(this, param);
		}
	}
}, true);

function renderTemplate(name, param) {
	return Object.prototype.renderTemplate.call(global, name, param);
}

// Define settings from passed options:
var settings = {
	basePackage:  options.basepackage || "",
	destDir: options.d || "",
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
	shortInherited: options.shortinherited == "true",
	debug: options.shortinherited == "true",
	section1Open: options.section1open || "<h1>",
	section1Close: options.section1close || "</h1>",
	section2Open: options.section2open || "<h2>",
	section2Close: options.section2close || "</h2>"
};

if (!/\/^/.test(settings.destDir)) settings.destDir += "/";

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

	// This is defined outside createLink so that even when a Type
	// happens to be its own ClassDoc (as returned by asClassDoc), and therefore
	// overrides createLink, it can still call the base version.
	createClassLink: function(name) {
		if (!name) name = this.name();
		var str = "";
		if (this.isVisible()) {
			if (this.isAbstract())
				str += "<i>";
			str += createLink(this.qualifiedName(), this.name(), "", name);
			if (this.isAbstract())
				str += "</i>";
		} else {
			str = this.name();
		}
		return str;
	},

	createLink: function(name) {
		return this.createClassLink(name);
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

	createLink: function() {
		if (this.isNumber())
			return "<tt>Number</tt>";
		else if (this.isBoolean())
			return "<tt>Boolean</tt>";
		else if (this.isArray())
			return "<tt>Array</tt>";
		else if (this.isMap())
			return "<tt>Object</tt>";
		else if (this.isPoint())
			return ClassObject.createLink("Point", "com.scriptographer.ai.Point");
		else if (this.isRectangle())
			return ClassObject.createLink("Rectangle", "com.scriptographer.ai.Rectangle");
		else if (this.isMatrix())
			return ClassObject.createLink("Matrix", "com.scriptographer.ai.Matrix");
		else {
			var cls = this.asClassDoc();
			if (cls) {
				if (cls.isVisible())
					return cls.createClassLink();
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
			if (!mem.createLink) {
				var cls = mem.getClass();
				while (cls)
					cls = cls.getSuperclass();
			}
			*/
			return ref.createLink(param.classDoc);
		} else {
			error(this.position() + ": warning - @link contains undefined reference: " + this);
		}
	}
});

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

	createLink: function(current) {
		var mem = Member.get(this);
		return mem ? mem.createLink(current) : "";
	}

});

// Member

Member = Object.extend({
	$constructor: function(classProxy, member) {
		this.classProxy = classProxy;
		this.member = member;
	},

	init: function() {
		// nothing here, but in the extended classes
	},

	renderReturnType: function(out) {
		var retType = this.returnType();
		if (retType && !retType.typeName().equals("void")) {
			return this.renderTemplate("returnType", {
				classDoc: this.classProxy.classDoc,
				link: retType.createLink(),
				tags: this.tags("return")
			}, out);
		}
		// TODO: remove
		return "";
	},

	// TODO: rename!
	// TODO: change to passing params in param-object
	printMemberBase: function(writer, indexWriter, cd, id, title, text, tags) {
		writer.print(this.renderTemplate("memberBase", {
			id: id,
			title: title,
			text: text
		}));
		if (settings.templates)
			indexWriter.print(", \"" + id + "\": { title: \"" + this.name() + "\", text: \"" + encodeJs(getTags({ classDoc: cd, tags: tags })) + "\" }");
	},

	printMember: function(writer, indexWriter, cd) {
		var title = "<tt><b>";
		// Static = PROTOTYPE.NAME
		if (this.isStatic())
			title += cd.name() + ".";
		title += this.name() + "</b></tt>";

		var text = new java.io.StringWriter();
		var strWriter = new java.io.PrintWriter(text);

		// Description
		printTags({ classDoc: cd, tags: this.inlineTags(), prefix: "<div class=\"member-paragraph\">", suffix: "</div>" }, strWriter);
		// Return tag
		// TODO:
		strWriter.print(this.renderReturnType());
		// See tags
		printTags({ classDoc: cd, tags: this.seeTags(), prefix: "<div class=\"member-paragraph\"><b>See also:</b>", suffix: "</div>", separator: ", " }, strWriter);

		this.printMemberBase(writer, indexWriter, cd, this.getId(), title.toString(), text.toString(), this.inlineTags());
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

	printSummary: function(writer, cd) {
		writer.println("<li class=\"summary\">");
		writer.print(this.createLink());
//		printTags({ classDoc: cd, tags: firstSentenceTags(), prefix: "<ul><li>", suffix: "</li></ul>" }, writer);
		writer.println("</li>");
	},

	getId: function() {
		return this.name() + this.signature();
	},

	getClass: function(current) {
		// in case the class is invisible, the current class needs to be used instead
		var containing = this.containingClass();
		return (containing.isVisible() || current.superclass() != containing) ? containing : current;
	},

	createLink: function(current) {
		var cd = this.getClass(current);
		// dont use mem.qualifiedName(). use cd.qualifiedName() + "." + mem.name()
		// instead in order to catch the case where functions are moved from invisible
		// classes to visible ones (e.g. AffineTransform -> Matrix)
		return createLink(cd.qualifiedName(), cd.name(), this.getId(), this.name() + this.getNameSuffix());
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
	$constructor: function(classProxy, param) {
		this.$super(classProxy);
		this.isGrouped = false;
		this.members = [];
		this.map = new Hash();
		if (param instanceof MethodDoc) {
			// used only in printMember for overriding tags
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
					// if this method is not wrapped, quickly wrap it just to call printMember
					// prevent endless loops that happen when overriden functions from inivisble classes
					// where moved to the derived class and Member.get lookup points there instead of
					// the overridden version:
					if (mem && mem.member.containingClass() != this.member.overriddenClass())
						mem = null;
					if (!mem)
						mem = new Method(this.classProxy, overridden);
					return mem;
				}
			}
		}
	},

	printSummary: function(writer, cd) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden)
			overridden.printSummary(writer, cd);
		else
			this.$super(writer, cd);
	},

	printMember: function(writer, indexWriter, cd, copiedTo) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden) {
			overridden.printMember(writer, indexWriter, cd, copiedTo || this);
		} else {
			var mbr = copiedTo || this;
			var title = "<tt><b>";
			// Static = PROTOTYPE.NAME
			if (this.isStatic())
				title += cd.name() + ".";
			title += this.name() + "</b>" + this.renderParameters() + "</tt>";

			// Thrown exceptions
			/*
			ClassDoc[] thrownExceptions = member.thrownExceptions();
			if (thrownExceptions != null && thrownExceptions.length) {
				writer.print(" throws <tt>");
				for (int e = 0; e < thrownExceptions.length; e++) {
					if (e)
						writer.print(", ");
					writer.print(thrownExceptions[e].qualifiedName());
				}
				writer.print("</tt>");
			}
			writer.println();
			*/
			
			var text = new java.io.StringWriter();
			var strWriter = new java.io.PrintWriter(text);

			// Description
			var descriptionTags = mbr.inlineTags();
			printTags({ classDoc: this.classProxy.classDoc, tags: descriptionTags, prefix: "<div class=\"member-paragraph\">", suffix: "</div>" }, strWriter);

			// Parameter tags
			this.printParameterTags(strWriter, cd);

			// Return tag
			// TODO:
			strWriter.print(mbr.renderReturnType());

			// Throws or Exceptions tag
			var excp = this.member.throwsTags();
			if (excp.length) {
				strWriter.println("<div class=\"member-paragraph\"><b>Throws:</b>");
				excp.each(function(ex) {
					var str = ex.exceptionName();
					var cd = ex.exception();
					if (cd)
						str = cd.createLink();
					strWriter.print("<div>" + str + " - ");
					printTags({ classDoc: cd, tags: ex.inlineTags() }, strWriter);
					strWriter.print("</div>");
				});
				strWriter.println("</div>");
			}

			// See tags
			printTags({ classDoc: cd, tags: mbr.seeTags(), prefix: "<div class=\"member-paragraph\"><b>See also:</b>", suffix: "</div>", separator: ", " }, strWriter);

			this.printMemberBase(writer, indexWriter, cd, mbr.getId(), title.toString(), text.toString(), descriptionTags);
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
	
	printParameterTags: function(writer, cd) {
		var params = this.member.parameters();
		if (params.length) {
			var origTags = this.member.paramTags();
			var lookup = origTags.each(function(tag) {
				this[tag.parameterName()] = tag;
			}, {});
			writer.println("<div class=\"member-paragraph\"><b>Parameters:</b>");
			params.each(function(param) {
				var name = param.name();
				writer.print("<div><tt>" + name + ":</tt>" + param.paramType().createLink());
				var origTag = lookup[name];
				if (origTag) {
					var inlineTags = origTag.inlineTags();
					printTags({ classDoc: cd, tags: inlineTags, prefix: " - " }, writer);
				}
				writer.println("</div>");
			});
			writer.println("</div>");
		}
		return params.length;
	},
	
	name: function() {
		return this.methodName;
	},
	
	containingClass: function() {
		return this.classProxy.classDoc;
	},

	containingPackage: function() {
		return this.classProxy.classDoc.containingPackage();
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

	$constructor: function(classProxy, name, getter, setter) {
		this.$super(classProxy);
		this.propertyName = name;
		this.getter = getter;
		this.setter = setter;
		var str = "";
		if (!setter)
			str += "Read-only ";
		if (settings.templates)
			str += "<% this.beanProperty %>";
		else
			str += "Bean Property";
		str += ", defined by " + getter.createLink(classProxy.classDoc);
		if (setter)
			str += " and " + setter.createLink(classProxy.classDoc);
		this.inlineTagList = [];
		this.inlineTagList.append(getter.inlineTags());
		if (this.inlineTagList.length)
			str = "<br />" + str;
		this.inlineTagList.push(new Tag(str));
		this.seeTagList = [];
		/*
		if (setter) {
			seeTags = [
				new SeeTag(getter),
				new SeeTag(setter)
			];
		} else {
			seeTags = [
				new SeeTag(getter)
			]
		}
		*/
	},

	name: function() {
		return this.propertyName;
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

	$constructor: function(classProxy, name) {
		this.classProxy = classProxy;
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
				mem = new Method(this.classProxy, name);
				if (mem.add(member))
					this.lists.push(mem);
			}
		} else {
			if (member instanceof Member)
				mem = member;
			else
				mem = new Member(this.classProxy, member);
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
	$constructor: function(classProxy) {
		this.classProxy = classProxy;
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
				group = new MemberList(this.classProxy, name); 
				this.methodLookup[name] = group;
			}
		} else {
			// fields won't be grouped, but for simplicty,
			// greate groups of one element for each field,
			// so it can be treated the same as functions:
			group = new MemberList(this.classProxy, key); 
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
				var propertyName = component;
				// TODO: optimize with regexps:
				var ch0 = component.charAt(0);
				if (ch0.isUpperCase()) {
					if (component.length == 1)
						propertyName = component.toLowerCase();
					else if (!component.charAt(1).isUpperCase())
						propertyName = ch0.toLowerCase() + component.substring(1);
				}
				// If we already have a member by this name, don't do this
				// property.
				if (!fields.contains(propertyName)) {
					var getter = member.extractGetMethod(), setter = null;
					if (getter) {
						// We have a getter. Now, do we have a setter?
						var setters = this.methodLookup["set" + component];
						// Is this value a method?
						if (setters)
							setter = setters.extractSetMethod(getter.returnType());
						// Make the property.
						fields.add(new BeanProperty(this.classProxy, propertyName, getter, setter));
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

	hasSimilar: function(member, method) {
		(method ? this.methods() : this.fields()).each(function(other) {
			if (member.isSimilar(other))
				return true;
		});
		return false;
	},

	printClass: function(indexWriter) {
		var cd = this.classDoc;
		// determine folder + filename for class file:
		var name = this.name();
		var className = cd.name();
		// name and className might differ, e.g. for global -> Global Scope!
		var path = cd.qualifiedName();
		// cut away name:
		path = path.substring(0, path.length - className.length);
		path = getRelativeIdentifier(path);
		var writer = beginDocument(path, className);

		var subclasses = root.classes().each(function(cls) {
			if (cls.isVisible() && cls.superclass() == cd && !cls.equals(cd))
				this.push(cls.createLink());
		}, []).join(', ');

		var type = "Prototype";
		if (cd.isInterface())
			type = "Interface";
		else if (cd.isException())
			type = "Exception";

		if (cd.isAbstract())
			type = "Abstract " + type;

		var superType = null;
		var sc = cd.superclass();
		if (sc && sc.isVisible())
			superType = sc.createLink();

		if (!settings.templates)
			writer.println(settings.section1Open + name + settings.section1Close);

		var listType = cd.getListType();

		if (cd.isAbstract() || superType || listType || subclasses) {
			writer.print("<p>" + type + " " + cd.createLink());
			if (superType)
				writer.print(" extends " + superType);
			if (listType) {
				if (settings.templates)
					listType = "<% this.listType type=\"" + listType + "\" %>";
				if (superType)
					writer.print(",");
				writer.print(" acts as " + listType);
			}
			if (subclasses)
				writer.print("<br />Inherited by " + subclasses);
			writer.println("</p>");
		}

		printTags({ classDoc: cd, tags: cd.inlineTags(), prefix: "<p>", suffix: "</p>" }, writer);
		if (settings.templates)
			indexWriter.print("\"prototype\": { title: \"" + cd.name() + "\", text: \"" +
					encodeJs(getTags({ classDoc: cd, tags: cd.inlineTags() })) + "\" }");

		printTags({ classDoc: cd, tags: cd.seeTags(), prefix: "<p><b>See also:</b> ", suffix: "</p>", separator: ", " }, writer);

		var verTags = cd.tags("version");
		if (settings.versionInfo && verTags.length)
			writer.println(settings.section2Open + "Version" + settings.section2Close + verTags[0].text());

		if (cd.isInterface()) {
			var subintf = "";
			var implclasses = "";
			root.classes().each(function(cls) {
				if (cls.interfaces().find(function(intf) {
					return (intf.equals(cd))
				})) {
					if (cls.isInterface()) {
						if (subintf) subintf += ", ";
						subintf += cls.createLink();
					} else {
						if (implclasses) implclasses += ", ";
						implclasses += cls.createLink();
					}
				}
			});
			if (implclasses)
				writer.println(settings.section2Open +
						"All classes known to implement interface" +
						settings.section2Close + implclasses);
		}

		var fields = this.fields();
		var constructors = this.constructors();
		var methods = this.methods();

		if (settings.summaries) {
			if (settings.fieldSummary)
				this.printSummaries(writer, cd, fields, "Field summary");

			if (settings.constructorSummary)
				this.printSummaries(writer, cd, constructors, "Constructor summary");

			this.printSummaries(writer, cd, methods, "Method summary");
		}

		this.printMembers(writer, indexWriter, cd, constructors, "Constructors", false);

		this.printMembers(writer, indexWriter, cd, fields, "Properties", false);
		this.printMembers(writer, indexWriter, cd, fields, "Static Properties", true);

		this.printMembers(writer, indexWriter, cd, methods, "Functions", false);
		this.printMembers(writer, indexWriter, cd, methods, "Static Functions", true);
	

		if (settings.inherited) {
			var first = true;
			var superclass = cd.superclass();

			while (superclass && !superclass.qualifiedName().equals("java.lang.Object")) {
				if (superclass.isVisible()) {
					var superProxy = ClassObject.get(superclass);
					fields = superProxy.fields();
					methods = superProxy.methods();
					var inheritedMembers = [];
					// first non-static, then static:
					fields.each(function(field) {
						if (!field.isStatic() && !this.hasSimilar(field, false))
						inheritedMembers.push(field);
					}, this);
					fields.each(function(field) {
						if (field.isStatic() && !this.hasSimilar(field, false))
						inheritedMembers.push(field);
					}, this);
					methods.each(function(method) {
						if (!method.isStatic() && !this.hasSimilar(method, true))
						inheritedMembers.push(method);
					}, this);
					methods.each(function(method) {
						if (method.isStatic() && !this.hasSimilar(method, true))
						inheritedMembers.push(method);
					}, this);
					// print only if members available
					// (if class not found because classpath not
					// correctly set, they would be missed)
					if (inheritedMembers.length) {
						if (first)
							writer.print(settings.section2Open + "Inheritance" + settings.section2Close);
						else
							writer.println("<br/>");
						first = false;
						writer.println("<ul class=\"documentation-inherited\">");
						writer.println("<li>");
						writer.print(superclass.createLink());
						writer.println("</li>");
						writer.println("<li>");
						this.printInheritedMembers(writer, superclass, inheritedMembers);
						writer.println("</li>");
						writer.println("</ul>");
					}
				}
				superclass = superclass.superclass();
			}
			if (settings.shortInherited && !first)
				writer.println("<br/><br/>");
		}
		endDocument(writer);
	},

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	printMembers: function(writer, indexWriter, cd, members, title, showStatic) {
		if (members.length) {
			var strBuffer = new java.io.StringWriter();
			var strWriter = new java.io.PrintWriter(strBuffer);
			members.each(function(mem) {
				if (mem && mem.isStatic() == showStatic)
					mem.printMember(strWriter, indexWriter, cd);
			});
			var str = strBuffer.toString();
			if (str.length) {
				writer.println(settings.section2Open + "" + title + "" + settings.section2Close);
				writer.println("<div class=\"documentation-paragraph\">");
				writer.print(str);
				writer.println("</div>");
			}
		}
	},

	/**
	 * Produces a constructor/method summary.
	 * 
	 * @param members The fields to be summarized.
	 * @param title The title of the section.
	 */
	printSummaries: function(writer, cd, members, title) {
		if (members.length) {
			writer.println(settings.section2Open + title + settings.section2Close);
			writer.println("<ul>");
			var prevName;
			members.each(function(mem) {
				var name = mem.name();
				if (name != prevName)
					mem.printSummary(writer, cd);
				prevName = name;
			});
			writer.println("</ul>");
		}
	},

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 */
	printInheritedMembers: function(writer, cd, members) {
		if (members.length && members[0]) {
			writer.println("<ul>");
			members.each(function(mem) {
				if (mem) {
					writer.println("<li>");
					writer.print(mem.createLink(cd));
					writer.println("</li>");
				}
			});
			writer.println("</ul>");
		}
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

		createLink: function(name, qualifiedName) {
			var mem = this.get(qualifiedName);
			// use createClassLink, as createLink might have been overridden
			// by new Type(...)
			return mem ? mem.classDoc.createClassLink() : "<tt>" + name + "</tt>";
		},

		classes: new Hash()
	},

	addChild: function(mem) {
		this.children[mem.qualifiedName()] = mem;
	},

	removeChild: function(mem) {
		delete this.children[mem.qualifiedName()];
	},

	printHierarchy: function(writer, prepend) {
		var sorted = this.children.sortBy(function(mem) {
			return settings.classOrder[mem.name()] || Number.MAX_VALUE;
		});
		if (sorted.length) {
			writer.println(prepend + (settings.templates ? "[" : "<ul>"));
			sorted.each(function(mem) {
				var cd = mem.classDoc;
				if (settings.templates)
					writer.println(prepend + "\t{ name: \"" + mem.name() +
							"\", isAbstract: " + cd.isAbstract() + ", index: { ");
				mem.printClass(writer);
				setBase("");
				if (settings.templates) {
					writer.println(" }},");
				} else {
					writer.println(prepend + "\t<li>" +
							stripCodeTags(cd.createLink(mem.name())) + "</li>");
				}
				mem.printHierarchy(writer, prepend + "\t");
			});
			writer.println(prepend + (settings.templates ? "]," : "</ul>"));
		}
	}
});

/**
 * Produces a table-of-contents for classes and calls layoutClass on each class.
 */
function processClasses(writer, classes) {
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
	root.printHierarchy(writer, "");
}

function beginDocument(path, name) {
	// now split into packages and create subdirs:
	if (!settings.templates)
		path = "packages." + path;
	var parts = path.split(/\./);
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
	var writer = new java.io.PrintWriter(new java.io.FileWriter(settings.destDir + path + name +
			(settings.templates ? ".jstl" : ".html")));

	if (!settings.templates) {
		var base = "";
		for (var j = 1; j < levels; j++)
			base += "../";
		setBase(base);
		writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		writer.println("<html>");
		writer.println("<head>");
		writer.println("<title>" + name + "</title>");
		writer.println("<base target=\"classFrame\">");
		writer.println("<link rel=\"stylesheet\" href=\"../" + base + "resources/style.css\" type=\"text/css\">");
		writer.println("<script src=\"../" + base + "resources/scripts.js\" type=\"text/javascript\"></script>");
		writer.println("</head>");
		writer.println("<body class=\"documentation\">");
	}
	return writer;
}

function endDocument(writer) {
	if (!settings.templates) {
		if (settings.bottom)
			writer.println("<p class=\"footer\">" + settings.bottom + "</p>");
		writer.println("</body>");
		writer.println("</html>");
	}
	writer.close();
}

/**
 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
 */
function printTags(param, writer) {
	writer.print(renderTemplate("tags", param));
}

function getTags(param) {
	return renderTemplate("tags", param);
}

function getRelativeIdentifier(str) {
	return str.startsWith(settings.basePackage + ".") ?
			str.substring(settings.basePackage.length + 1) : str;
}

// TODO: fix this uggly hack (setBase)

var base = "";

function setBase(b) {
	base = b;
}

function getBase() {
	return base;
}

function createLink(qualifiedName, name, anchor, title) {
	var str = "<tt>";
	if (settings.hyperref) {
		str += "<a href=\"";
		if (qualifiedName) {
			var path = getRelativeIdentifier(qualifiedName).replace('.', '/');
			// link to the index file for packages
			if (name.charAt(0).isLowerCase() && !name.equals("global"))
				path += "/index";
			if (settings.templates)
				path = "/Reference/" + path + "/";
			else
				path = getBase() + path + ".html";
			str += path;
		}
		if (anchor) {
			if (settings.templates) str += anchor + "/";
			str += "#" + anchor;
			str += "\" onClick=\"return toggleMember('" + anchor + "', true);";
		}
		str += "\">" + title + "</a>";
	} else {
		str += title;
	}
	str += "</tt>";
	return str;
}

function encodeJs(str) {
	str = uneval(str);
	return str.substring(1, str.length - 1); 
}

function stripCodeTags(str) {
	return str.replace(/<tt>|<\/tt>/g, "");
}

function stripTags(str) {
	return str.replace(/<.*?>|<\/.*?>/g, " ").replace(/\\s+/g, " ");
}

ClassObject.put(root);

var writer = new java.io.PrintWriter(new java.io.FileWriter(
		settings.destDir + (settings.templates ? "packages.js" : "packages/packages.html")));

if (!settings.templates) {
	writer.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
	writer.println("<html>");
	writer.println("<head>");
	if (settings.docTitle)
		writer.println("<title>" + settings.docTitle + "</title>");
	writer.println("<base target=\"classFrame\">");
	writer.println("<link rel=\"stylesheet\" href=\"../resources/style.css\" type=\"text/css\">");
	writer.println("<script src=\"../resources/scripts.js\" type=\"text/javascript\"></script>");
	writer.println("</head>");
	writer.println("<html>");
	writer.println("<body class=\"documentation\">");
	writer.println("<div class=\"documentation-packages\">");
	if (settings.docTitle)
		writer.println(settings.section1Open + settings.docTitle + settings.section1Close);
	if (settings.author)
		writer.println(settings.author);
	writer.println("<ul class=\"documentation-list\">");
}

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

packageSequence.each(function(name) {
	var pkg = packages[name];
	if (pkg) {
		var name = pkg.name();
		setBase("");
		var rel = getRelativeIdentifier(name);
		if (settings.templates) {
			var pkgName = rel.toUpperCase();
			writer.print("createPackage(\"" + pkgName + "\", ");
		} else {
			var pkgName = rel.toUpperCase();
			writer.println("<li><a href=\"#\" onClick=\"return togglePackage('" + pkgName + "', false);\"><img name=\"arrow-" +  pkgName +
					"\" src=\"../resources/arrow-close.gif\" width=\"8\" height=\"8\" border=\"0\"></a><img src=\"../resources/spacer.gif\" width=\"6\" height=\"1\"><b>" +
					stripCodeTags(createLink(name, rel, null, pkgName)) + "</b>");
			writer.println("<ul id=\"package-" + pkgName + "\" class=\"hidden\">");
		}

		processClasses(writer, pkg.interfaces());
		processClasses(writer, pkg.allClasses(true));
		processClasses(writer, pkg.exceptions());
		processClasses(writer, pkg.errors());

		var text = getTags({ tags: pkg.inlineTags() });
		var first = getTags({ tags: pkg.firstSentenceTags() });

		// remove the first sentence from the main text
		if (first && text.startsWith(first)) {
			text = text.substring(first.length);
			first = first.substring(0, first.length - 1); // cut away dot
		}

		if (settings.templates) {
			writer.print("\"");
			writer.print(encodeJs(text.trim()));
			writer.println("\");");
		} else {
			writer.println("</li></ul>");
			// write package file:
			var pkgWriter = beginDocument(rel, "index");
			pkgWriter.println(settings.section1Open + first + settings.section1Close);
			pkgWriter.println(text);
			endDocument(pkgWriter);
		}
	}
});

if (!settings.templates) {
	writer.println("</ul>");
	writer.println("</div>");
	writer.println("</body>");
	writer.println("</html>");
}
writer.close();
