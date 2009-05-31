/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 */
SyntheticField = Member.extend({
	initialize: function(classObject, name, member) {
		this.base(classObject);
		this.property = name;
		this.member = member;
		this.inlineTagList = [];
		this.seeTagList = [];
		// Set reference to the field, so operators and getters / setters can
		// automatically be hidden without removing them.
		member.synthetic = this;
	},

	name: function() {
		return this.property;
	},

	qualifiedName: function() {
		return this.classObject.qualifiedName() + '.' + this.property;
	},

	modifiers: function() {
		return '';
	},

	returnType: function() {
		return this.member.returnType();
	},

	getVisible: function() {
		return Member.isVisible(this.member);
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

	firstSentenceTags: function() {
		return this.inlineTagList;
	},

	inlineTags: function() {
		return this.inlineTagList;
	},

	seeTags: function() {
		return this.seeTagList;
	},

	tags: function(tagname) {
		return [];
	}
});
