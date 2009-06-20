/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

// ClassObject
ClassObject = Object.extend({
	initialize: function(classDoc, visible) {
		this.classDoc = classDoc;
		// For the hierarchy:
		this.visible = visible;
		this.children = new Hash();
		this.refernceMembers = [];
		this.lists = new Hash({
			field: new MemberGroupList(this),
			method: new MemberGroupList(this),
			constructor: new MemberGroupList(this),
			operator: new MemberGroupList(this)
		});
	},

	init: function() {
		this.add(this.classDoc, true);
		var superclass = this.classDoc.superclass();
		// Add the members of direct invisible superclasses to
		// this class for JS documentation.
		// Careful: This should only be done with direct invisible superclasses,
		// Since all the other inheriting classes will link this visible class
		// for inheritance.
		while (superclass && !superclass.isVisible()
				&& superclass.qualifiedName() != 'java.lang.Object') {
			this.add(superclass, false);
			superclass = superclass.superclass();
		}
		var extensions = this.classDoc.tags('jsextension');
		// Loop through the extensions backwards, so insertion sequence through 
		// @after tags is maintained.
		for (var i = extensions.length - 1; i >= 0; i--) {
			var ext = extensions[i];
			var data = {};
			ext.inlineTags().each(function(tag) {
				var name = tag.name();
				if (/^@/.test(name))
					data[name.substring(1)] = tag.text();
			});
			if (data.type && data.name) {
				var list = this.lists[data.type];
				if (list) {
					var ref = new ReferenceMember(this, data, list);
					this.refernceMembers.push(ref);
				}
			}
		}
		// Only method and constructor need to call init for method parameter merging.
		this.lists.method.init();
		this.lists.constructor.init();
	},

	scan: function() {
		// Scan for beanProperties and operators and add these to the 
		// field / operator lists.
		this.lists.method.scanBeanProperties(this.lists.field);
		this.lists.method.scanOperators(this.lists.operator);
	},

	resolve: function() {
		this.refernceMembers.each(function(ref) {
			ref.resolve();
		});
	},

	add: function(cd, addConstructors) {
		this.lists.field.addAll(cd.fields(true));
		this.lists.method.addAll(cd.methods(true));
		if (addConstructors)
			this.lists.constructor.addAll(cd.constructors(true));
	},

	getGroup: function(name) {
		return this.lists.find(function(list, key) {
			return list.get(name);
		});
	},

	methods: function() {
		return this.lists.method.getFlattened();
	},

	fields: function() {
		return this.lists.field.getFlattened();
	},

	constructors: function() {
		return this.lists.constructor.getFlattened();
	},

	operators: function() {
		return this.lists.operator.getFlattened();
	},

	name: function() {
		var name = this.classDoc.name();
		return name == 'global' ? 'Global Scope' : name;
	},

	qualifiedName: function() {
		return this.classDoc.qualifiedName();
	},

	toString: function() {
		return this.classDoc.toString();
	},

	isVisible: function() {
		return this.visible;
	},

	hasCompatible: function(member) {
		return (member.isMethod() && this.methods()
				|| member.isConstructor() && this.constructors()
				|| this.fields()).find(function(other) {
			return member.isCompatible(other);
		});
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

		var constructors = this.constructors();
		var fields = this.fields();
		var methods = this.methods();
		var operators = this.operators();

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
		this.renderMembers({ classDoc: cd, members: operators, title: 'Operators', index: index });

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
			while (superclass && superclass.qualifiedName() != 'java.lang.Object') {
				if (superclass.isVisible()) {
					var superProxy = ClassObject.get(superclass);
					fields = separateStatic(superProxy.fields());
					methods = separateStatic(superProxy.methods());
					var inherited = [], that = this;

					function addNonSimilar(members) {
						members.each(function(member) {
							if (member.isVisible() && !that.hasCompatible(member))
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
			this.renderTemplate('class#inheritance', { classDoc: cd, classes: classes }, out);
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
		scan: function(root) {
			root.classes().each(function(classDoc) {
				ClassObject.put(classDoc);
			});
			// Now initialize them. init needs all the others to be there,
			// due to bean prop stuff
			this.classObjects.each(function(cls) {
				cls.init();
			});
			// Now after all have initialised, scan for bean properties and 
			// operators. This needs to happen in a second step, so first
			// all the methods can be merged and hidden if needed. Only
			// then getter / setters can be converted and removed.
			this.classObjects.each(function(cls) {
				cls.scan();
			});
			// Now call resolve, to resolve references.
			this.classObjects.each(function(cls) {
				cls.resolve();
			});
		},

		put: function(classDoc, force) {
			if (typeof classDoc == 'string')
				classDoc = this.classDocs[classDoc];
			if (classDoc) {
				var name = classDoc.qualifiedName();
				var visible = !(
					// classMatch regular expression
					settings.classMatch && !settings.classMatch.test(name)
					// classFilter matching
					|| settings.classFilter && settings.classFilter.find(function(filter) {
						// Support wildcard matching at the end
						return filter == name || filter.endsWith('*') &&
							name.startsWith(filter.substring(0, filter.length - 1));
					})
					// Do not add any of Enums, since they are represented
					// as strings in the scripting environment.
					|| classDoc.hasSuperclass('java.lang.Enum')
					// Support @jshide tag for classes
					|| classDoc.tags('jshide')[0]
				);
				// Always add to classDocs, even when they're hidden, to be able to
				// do lookups through getClassDoc.
				this.classDocs[name] = classDoc;
				if (visible || force) {
					var obj = new ClassObject(classDoc, visible);
					this.classObjects[name] = obj;
					if (force) {
						// Execute all initialisation steps at once now.
						obj.init();
						obj.scan();
						obj.resolve();
					}
					return obj;
				}
			}
			return null;
		},

		get: function(param) {
			if (param && param.qualifiedName)
				param = param.qualifiedName();
			return this.classObjects[param]
		},

		getClassDoc: function(name) {
			return this.classDocs[name];
		},

		renderLink: function(param) {
			param = param || {};
			var mem = this.get(param.name);
			// use renderClassLink, as renderLink might have been overridden
			// by new Type(...)
			return mem && mem.classDoc
				? mem.classDoc.renderClassLink({})
				: code_filter(Type.getSimpleName(param.name));
		},

		classObjects: {},
		classDocs: {}
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
			if (cls.isVisible()) {
				var cd = cls.classDoc;
				var index = cls.renderClass();
				this.renderTemplate('packages#class', {
					index: index ? index.join(', ') : null, cls: cls
				}, out);
				cls.renderHierarchy();
			}
		});
		this.renderTemplate('packages#classes', { classes: out.pop() }, out);
	}
});
