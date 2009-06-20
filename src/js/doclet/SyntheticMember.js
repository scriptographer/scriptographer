/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

SyntheticMember = Member.extend({
	initialize: function(classObject, name, member) {
		this.base(classObject, member);
		this._name = name;
	},

	name: function() {
		return this._name;
	},

	qualifiedName: function() {
		return this.classObject.qualifiedName() + '.' + this._name;
	},

	returnType: function() {
		return new Type(this.member.returnType());
	},

	getVisible: function() {
		return Member.isVisible(this.member);
	},

	containingClass: function() {
		return this.member.containingClass();
	},

	containingPackage: function() {
		return this.member.containingPackage();
	},

	tags: function(tagname) {
		return this.member.tags(tagname);
	},

	toString: function() {
		return this.qualifiedName();
	}
});
