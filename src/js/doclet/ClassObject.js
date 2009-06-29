/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

ClassObject = DocObject.extend({
	initialize: function(doc, visible) {
		this.base(doc);
		// For the hierarchy:
		this.visible = visible;
		this.referenceMembers = [];
		this.lists = new Hash({
			field: new MemberGroupList(this),
			method: new MemberGroupList(this),
			constructor: new MemberGroupList(this),
			operator: new MemberGroupList(this)
		});
	},

	init: function() {
		this.addDoc(this.doc, true);
		var superclass = this.doc.superclass();
		// Add the members of direct invisible superclasses to
		// this class for JS documentation.
		// Careful: This should only be done with direct invisible superclasses,
		// Since all the other inheriting classes will link this visible class
		// for inheritance.
		while (superclass && !superclass.isVisible()
				&& superclass.qualifiedName() != 'java.lang.Object') {
			this.addDoc(superclass, false);
			superclass = superclass.superclass();
		}
		var extensions = this.doc.tags('jsextension');
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
					this.referenceMembers.push(ref);
				}
			}
		}
		// Only method and constructor need to call init here for method parameter merging.
		this.lists.method.init();
		this.lists.constructor.init();
	},

	/**
	 * Scans for beanProperties and operators and add these to the 
	 * field / operator lists.
	 */
	scan: function() {
		this.lists.method.scanBeanProperties();
		this.lists.method.scanOperators();
	},

	add: function(member) {
		var list = this.lists[member.type];
		return list && list.add(member);
	},

	hasField: function(name) {
		return this.lists.field.contains(name);
	},

	/**
	 * Resolves reference members after all other things are scanned and
	 * processed, e.g. bean properties and operators.
	 */
	resolve: function() {
		this.ammend();
		this.referenceMembers.each(function(ref) {
			ref.resolve();
		});
	},

	/**
	 * Ammends the current class with more members, to reflect the behavior
	 * of the JS layer.
	 */
	ammend: function() {
		if (this.doc.actsAsArray()) {
			// Support both read-only and normal lists.
			var type = this.doc.hasInterface('com.scratchdisk.list.List')
				? 'com.scratchdisk.list.List'
				: 'com.scratchdisk.list.ReadOnlyList'; 
			var obj = ClassObject.get(type, true);
			if (obj == this) {
				var getter = obj.getMember('size');
				// Try setters too, for normal lists
				if (getter)
					this.add(new BeanProperty(obj, 'length', getter, this.getMember('setSize')));
			} else {
				// Add a reference to the newly added bean above
				this.referenceMembers.push(new ReferenceMember(this, {
					name: 'length',
					reference: type + '#length',
					type: 'field'
				}, this.lists.field));
			}
		}
	},

	addDoc: function(doc, addConstructors) {
		this.lists.field.addAll(doc.fields(true));
		this.lists.method.addAll(doc.methods(true));
		if (addConstructors)
			this.lists.constructor.addAll(doc.constructors(true));
	},

	getGroup: function(name) {
		return this.lists.find(function(list, key) {
			return list.get(name);
		});
	},

	getMember: function(name, signature) {
		var group = this.getGroup(name);
		return group && group.getMember(signature);
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
		var name = this.base();
		return name == 'global' ? 'Global Scope' : name;
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
		var doc = this.doc, index = null;
		if (settings.templates) {
			index = this.index = {
				'class': {
					title: doc.name(),
					text: renderTags({ 
						doc: doc, tags: doc.inlineTags()
					})
				}
			};
		}
		// Determine folder + filename for class file:
		var name = this.name();
		var className = doc.name();
		// Name and className might differ, e.g. for global -> Global Scope!
		var path = doc.qualifiedName();
		// Cut away name:
		path = path.substring(0, path.length - className.length);
		path = DocObject.getRelativeIdentifier(path);
		var document = new Document(path, className, 'document');

		// From now on, the global out writes to doc
		this.renderTemplate('class', {}, out);
		if (doc.isInterface()) {
			var subInterfaces = [];
			var implementingClasses = [];
			root.classes().each(function(cls) {
				if (cls.interfaces().find(function(inter) {
					return inter.equals(doc);
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
				this.renderSummaries(doc, fields, 'Field summary');
			if (settings.constructorSummary)
				this.renderSummaries(doc, constructors, 'Constructor summary');
			this.renderSummaries(doc, methods, 'Method summary');
		}

		// Filter members into static and non-static ones:
		function separateStatic(members) {
			return members.each(function(member) {
				this[member.isStatic() ? 1 : 0].push(member);
			}, [[], []]);
		}

		this.renderMembers({ doc: doc, members: constructors, title: 'Constructors', index: index });
		this.renderMembers({ doc: doc, members: operators, title: 'Operators', index: index });

		fields = separateStatic(fields);
		this.renderMembers({ doc: doc, members: fields[0], title: 'Properties', index: index });
		this.renderMembers({ doc: doc, members: fields[1], title: 'Static Properties', index: index });

		methods = separateStatic(methods);
		this.renderMembers({ doc: doc, members: methods[0], title: 'Functions', index: index });
		this.renderMembers({ doc: doc, members: methods[1], title: 'Static Functions', index: index });

		if (settings.inherited) {
			var first = true;
			var superclass = doc.superclass();

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
						classes.push({ doc: superclass, members: inherited });
				}
				superclass = superclass.superclass();
			}
			this.renderTemplate('class#inheritance', { doc: doc, classes: classes }, out);
		}
		document.close();
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
	renderSummaries: function(doc, members, title) {
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
			members: members, title: title, doc: doc
		}, out);
	},

	statics: {
		process: function(classes) {
			classes.each(function(doc) {
				ClassObject.put(doc);
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

		put: function(doc, force) {
			if (typeof doc == 'string')
				doc = this.docs[doc];
			if (doc) {
				var name = doc.qualifiedName();
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
					|| doc.hasSuperclass('java.lang.Enum')
					// Support @jshide tag for classes
					|| doc.tags('jshide')[0]
				);
				// Always add to docs, even when they're hidden, to be able to
				// do lookups through getClassDoc.
				this.docs[name] = doc;
				if (visible || force) {
					var obj = new ClassObject(doc, visible);
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

		get: function(param, force) {
			var name = param && (typeof param == 'string' ? param : param.qualifiedName());
			var obj = this.classObjects[name];
			if (!obj && force)
				obj = ClassObject.put(param, force);
			return obj;
		},

		getClassDoc: function(name) {
			return this.docs[name];
		},

		renderLink: function(param) {
			param = param || {};
			var mem = this.get(param.name);
			// use renderClassLink, as renderLink might have been overridden
			// by new Type(...)
			return mem && mem.doc
				? mem.doc.renderClassLink({})
				: code_filter(Type.getSimpleName(param.name));
		},

		classObjects: {},
		docs: {}
	}
});
