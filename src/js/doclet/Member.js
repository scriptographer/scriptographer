/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

// Member
Member = Object.extend({

	// Type identifies which list this member is added to. to be overridden 
	// in inerhiting prototypes.
	type: 'field',

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
					title: (this.isStatic() ? this.containingClass().name() + '.' : '') + this.name(),
					text: renderTags({
						doc: param.doc, tags: this.inlineTags()
					})
				}
			}
			var copy = this.tags('copy')[0];
			copy = copy && Member.getByReference(copy.text(), param.doc);
			if (copy) {
				// Render own comment even for copy tags to offer support for grouptitles.
				renderTags({ doc: this.containingClass(), tags: this.inlineTags() });
			}
		 	param.commentObject = copy || this;
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

	/**
	 * Returns the member to use for comments. Allows for easy overriding in
	 * any of the subclasses. Used in Method, for overridden methods with comments.
	 */ 
	getCommentedMember: function() {
		return this.member;
	},

	firstSentenceTags: function() {
		return this.getCommentedMember().firstSentenceTags();
	},

	inlineTags: function() {
		return this.getCommentedMember().inlineTags();
	},

	seeTags: function() {
		return this.getCommentedMember().seeTags();
	},

	throwsTags: function() {
		var commented = this.getCommentedMember();
		return commented.throwsTags ? commented.throwsTags() : null;
	},

	tags: function(tagname) {
		return this.getCommentedMember().tags(tagname);
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

	isCallable: function() {
		return this.isMethod() || this.isConstructor();
	},

	renderSummary: function(doc) {
		return this.renderTemplate('summary', { doc: doc });
	},

	getId: function() {
		return this.name().toLowerCase();
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
		// Do not pass this.member, but this, to allow Method to search
		// for a commented overridden method.
		return Member.isVisible(this);
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

		getByReference: function(reference, doc, strict) {
			var group = MemberGroup.getByReference(reference, doc);
			if (group) {
				var [all, signature] = reference.match(/(\([^)]*\))$/) || [];
				var member = group.getMember(signature, strict);
				if (member)
					return member;
				// If this did not work, use the raw id through members.
				// This is for example needed for methods that turned into beans.
				// This need to produce the same syntax as getId above.
				return this.members[group.qualifiedName() + signature];
			}
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
