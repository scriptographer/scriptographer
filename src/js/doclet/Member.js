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
	},

	init: function() {
		// nothing here, but in the extended classes
	},

	renderMember: function(param) {
		if (this.isVisible()) {
			data.group = {};
			if (param.index) {
				param.index.push('"' + this.getId() + '": { title: "' + this.name() + '", text: "'
					+ encodeJs(renderTags({
						classDoc: param.classDoc, tags: this.inlineTags()
					}))
					+ '" }'
				);
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

	renderSummary: function(classDoc) {
		return this.renderTemplate('summary', { classDoc: classDoc });
	},

	getId: function() {
		// Convert name + signature to css friendly id:
		return (this.name() + '-' + this.signature()).replace(/\W+/g, function(match) {
			return '-';
		}).trim('-');
	},

	renderLink: function(param) {
		param = param || {};
		// In case the class is invisible, the current class needs to be used instead
		var containing = this.containingClass();
		if (!containing.isVisible() && param.classDoc.superclass() == containing)
			containing = param.classDoc;
		var sameClass = containing == param.classDoc;
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

		put: function(member) {
			// Store id in member so we do not have to rely on Memer.getId
			// in Member.remove, since that will not work anymore as its
			// methods list might be empty at that point already, from removing
			// all methods from the group.
			var id = Member.getId(member);
			member.id = id;
			this.members[id] = member;
		},

		get: function(member) {
			return this.members[Member.getId(member)];
		},

		remove: function(member) {
			delete this.members[member.id || Member.getId(member)];
		},

		isVisible: function(member) {
			var hide = member && member.tags('jshide')[0];
			return hide ? !/^(bean|all|)$/.test(hide.text()) : !!member;
		}
	}
});
