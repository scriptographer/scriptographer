/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

// Member
Member = Object.extend({
	initialize: function(classObject, member) {
		this.classObject = classObject;
		this.member = member;
		if (member)
			Member.put(member, this);
	},

	init: function() {
		// Do nothing here. Used in Method
	},

	renderMember: function(param) {
		if (this.isVisible()) {
			data.group = {};
			if (param.index) {
				param.index[this.getId()] = {
					title: this.name(),
					text: renderTags({
						doc: param.doc, tags: this.inlineTags()
					})
				}
			}
			// Thrown exceptions
			// if (this.member.thrownExceptions)
			//	renderTemplate('exceptions', { exceptions: this.member.thrownExceptions() }, out);
			// Description
			return this.renderTemplate('member', param);
		}
	},

	name: function() {
		return this.member.name();
	},

	qualifiedName: function() {
		return this.member.qualifiedName();
	},

	signature: function() {
		// Do not forward to this.member.signature, since this.member might
		// be a function e.g. for bean properties. Let Method do the work.
		return '';
	},

	containingClass: function() {
		return this.member.containingClass();
	},

	containingPackage: function() {
		return this.member.containingPackage();
	},

	firstSentenceTags: function() {
		return this.member.firstSentenceTags();
	},

	inlineTags: function() {
		return this.member.inlineTags();
	},

	seeTags: function() {
		return this.member.seeTags();
	},

	throwsTags: function() {
		return this.member.throwsTags ? this.member.throwsTags() : null;
	},

	getNameSuffix: function() {
		return '';
	},

	parameters: function() {
		return null;
	},

	returnType: function() {
		return this.member.isField() ? new Type(this.member.type()) : null;
	},

	tags: function(tagname) {
		return this.member.tags(tagname);
	},

	isStatic: function() {
		return this.member.isStatic();
	},

	isField: function() {
		return this.member.isField();
	},

	isMethod: function() {
		return this.member.isMethod();
	},

	isConstructor: function() {
		return this.member.isConstructor();
	},

	renderSummary: function(doc) {
		return this.renderTemplate('summary', { doc: doc });
	},

	getId: function() {
		// Convert name + signature to css friendly id:
		// Allow [] in id since it's used to distinguish arrays from their component types
		return (this.name() + '-' + this.signature()).replace(/[^\w\[\]]+/g, function(match) {
			return '-';
		}).trim('-');
	},

	renderLink: function(param) {
		param = param || {};
		// In case the class is invisible, the current class needs to be used instead
		var containing = this.containingClass();
		if (!containing.isVisible() && param.doc.superclass() == containing)
			containing = param.doc;
		var sameClass = containing == param.doc;
		return renderLink({
			path: containing.qualifiedName(),
			anchor: this.getId(),
			toggle: sameClass,
			title: param.title || code_filter(
				// Add the class name if the link goes accross classes
				(!sameClass && !param.shortTitle
					? containing.name() + (this.isStatic() ? '.' : '#')
					: '') + this.name() + this.getNameSuffix())
		});
	},

	toString: function() {
		return this.member.toString();
	},

	isCompatible: function(mem) {
		return this.isStatic() == mem.isStatic() && this.name() == mem.name();
	},

	isVisible: function() {
		// Returns visibility with caching, for faster processing time.
		// Override getVisible to handle visibility.
		if (this._visible === undefined)
			this._visible = this.getVisible();
		return this._visible;
	},

	getVisible: function() {
		return Member.isVisible(this.member);
	},

	statics: {
		members: new Hash(),

		getId: function(member) {
			var id = member.qualifiedName();
			if (member.signature)
				id += member.signature();
			return id;
		},

		put: function(member, obj) {
			if (member instanceof MemberGroup || member instanceof Method) {
				member.members.each(function(mem) {
					Member.put(mem, obj);
				});
			} else {
				this.members[Member.getId(member)] = obj;
			}
		},

		remove: function(member, obj) {
			// Only remove this native member from the lookup if it still
			// points to the same wrapper. It could be that it was assigned
			// to a new one in the meantime, e.g. getter method -> bean.
			var id = Member.getId(member);
			var val = this.members[id];
			if (val == obj)
				delete this.members[id];
		},

		get: function(member) {
			return this.members[Member.getId(member)];
		},

		isVisible: function(member, forceAll) {
			var hide = member && member.tags('jshide')[0];
			return !(hide && (!forceAll || hide.text() == 'all') || !member);
		}
	}
});

// Extend native MemberDocImpl with some helpers:
MemberDocImpl.inject({
	isVisible: function() {
		return !!Member.get(this);
	},

	renderLink: function(param) {
		param = param || {};
		var mem = Member.get(this);
		return mem
			? mem.renderLink(param)
			// Invisible members do not get wrapped in Member objects, so they
			// need to at least render something that gives a hint which function
			// they would be. (e.g. when linking to invisible methods using @link)
			: code_filter((this.containingClass() != param.doc
				? this.containingClass().name() + (this.isStatic() ? '.' : '#')
				: '') + this.name() + (this.signature ? this.signature() : ''));
	}
});
