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
		return new Type(this.member.returnType());
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

	tags: function(tagname) {
		return [];
	}
});
