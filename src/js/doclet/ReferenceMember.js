/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

ReferenceMember = SyntheticMember.extend({
	initialize: function(classObject, data, list, member) {
		this.base(classObject, data.name);
		this.data = data;
		this.reference = data.reference;
		this.type = data.type;
		this.after = data.after;
		this.list = list;
		this.member = member;
	},

	resolve: function() {
		var group = MemberGroup.get(this.reference, this.containingClass());
		// Since a group can have more than one member, we set the first one to
		// this ReferenceMember and create more ReferenceMembers for the others
		if (group) {
			this.member = group.members[0];
			if (this.member)
				this.list.addAt(this.after, this);
			for (var i = 1, l = group.members.length; i < l; i++) {
				var member = group.members[i];
				this.list.addAt(name, new ReferenceMember(this.classObject,
						this.data, this.list, member));
			}
		}
	},

	containingClass: function() {
		return this.classObject.classDoc;
	},

	getVisible: function() {
		return Member.isVisible(this.member, true);
	},

	signature: function() {
		return this.member.signature();
	},

	getNameSuffix: function() {
		return this.member.getNameSuffix();
	},

	renderParameters: function() {
		return this.member.renderParameters && this.member.renderParameters();
	}
});
