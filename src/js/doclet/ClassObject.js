/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

// ClassObject
ClassObject = Object.extend({
	initialize: function(cd) {
		this.classDoc = cd;
		// for the hierarchy:
		this.children = new Hash();
	},

	init: function() {
		this.fieldLists = new MemberGroupList(this);
		this.methodLists = new MemberGroupList(this);
		this.constructorLists = new MemberGroupList(this);
		this.operatorLists = new MemberGroupList(this);
		this.add(this.classDoc, true);
		var superclass = this.classDoc.superclass();
		// Add the members of direct invisible superclasses to
		// this class for JS documentation:
		while (superclass && superclass.qualifiedName() != 'java.lang.Object') {
			if (!superclass.isVisible())
				this.add(superclass, false);
			superclass = superclass.superclass();
		}
		this.methodLists.init();
		this.constructorLists.init();
		// now scan for beanProperties and operators:
		if (this.classDoc.name() != 'global') {
			this.methodLists.scanBeanProperties(this.fieldLists);
			this.methodLists.scanOperators(this.operatorLists);
		}
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

	operators: function() {
		return this.operatorLists.getFlattened();
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
				if (settings.classMatch)
					add = settings.classMatch.test(name);
				if (add && settings.classFilter)
					add = !settings.classFilter.find(function(filter) {
						return filter == name || filter.endsWith('*') &&
							name.startsWith(filter.substring(0, filter.length - 1));
					});
				// Do not add any of Enums, since they are represented
				// as strings in the scripting environment.
				if (add && cd.hasSuperclass('java.lang.Enum'))
					add = false;
				var hide = cd.tags('jshide')[0];
				if (hide && /^(bean|all|)$/.test(hide.text()))
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

		renderLink: function(param) {
			param = param || {};
			var mem = this.get(param.name);
			// use renderClassLink, as renderLink might have been overridden
			// by new Type(...)
			return mem && mem.classDoc
				? mem.classDoc.renderClassLink({})
				: code_filter(Type.getSimpleName(param.name));
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
