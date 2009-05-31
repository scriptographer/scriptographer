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

	isVisible: function() {
		var hide = this.tags('jshide')[0];
		if (hide) hide = hide.text();
		return !/^(method|all|)$/.test(hide) && (!this.synthetic || !this.synthetic.isVisible());
	},

	renderMember: function(classDoc, index, member, containingClass) {
		data.group = {};
		if (this.isVisible()) {
			if (!member)
				member = this;

			if (!containingClass)
				containingClass = this.containingClass();

			if (index)
				index.push('"' + member.getId() + '": { title: "' + this.name() +
						'", text: "' + encodeJs(renderTags({
							classDoc: classDoc, tags: member.inlineTags()
						})) + '" }');

			// Thrown exceptions
			// if (this.member.thrownExceptions)
			//	renderTemplate('exceptions', { exceptions: this.member.thrownExceptions() }, out);
			// Description
			var returnType = this.returnType();
			return this.renderTemplate('member', {
				classDoc: classDoc, member: member, containingClass: containingClass,
				throwsTags: this.member && this.member.throwsTags ? this.member.throwsTags() : null,
				returnType: returnType && returnType.typeName() != 'void' ? returnType : null
			});
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

	renderLink: function(param) {
		var cd = this.getClass(param.classDoc);
		// Dont use mem.qualifiedName(). use cd.qualifiedName() + '.' + mem.name()
		// instead in order to catch the case where functions are moved from
		// invisible classes to visible ones (e.g. AffineTransform -> Matrix)
		return renderLink({
			name: cd.qualifiedName(),
			anchor: this.getId(),
			title: param.title || code_filter(this.name() + this.getNameSuffix())
		});
	},

	isSimilar: function(mem) {
		return this.isStatic() == mem.isStatic() && this.name() == mem.name();
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
			this.members[Member.getId(member)] = member;
		},

		get: function(member) {
			return this.members[Member.getId(member)];
		},

		isVisible: function(member) {
			var hide = member && member.tags('jshide')[0];
			return hide && !/^(bean|all|)$/.test(hide.text()) || !!member;
		}
	}
});
