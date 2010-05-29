/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://scriptographer.org/ 
 */

ReferenceMember = SyntheticMember.extend({
	initialize: function(classObject, data, list, member) {
		this.base(classObject, data.name, member);
		this.data = data;
		this.reference = data.reference;
		this.type = data.type;
		this.after = data.after;
		this.list = list;
	},

	resolve: function() {
		// Only resolve for members in strict mode, so we find out wether to
		// reference the whole group otherwise:
		var members = null;
		var member = Member.getByReference(this.reference, this.containingClass(), true);
		// Since a group can have more than one member, we set the first one to
		// this ReferenceMember and create more ReferenceMembers for the others
		if (member) {
			members = [ member ];
		} else {
			// Add all members of the group, not just one
			var group = MemberGroup.getByReference(this.reference, this.containingClass());
			if (group) {
				members = group.members;
				// Remove referenced group from list if it's in same classObject,
				// since we're inserting it elsewhere.
				if (group.classObject == this.classObject)
					this.list.remove(group);
			}
		}
		if (members) {
			this.member = members[0];
			// Put it into the lookup
			Member.put(this.member, this);
			if (this.member)
				this.list.addAt(this.after, this);
			// If there's more than one member, add the others after.
			for (var i = 1, l = members.length; i < l; i++) {
				var member = members[i];
				Member.put(member, this);
				this.list.addAt(this.property, new ReferenceMember(this.classObject,
						this.data, this.list, member));
			}
		}
	},

	getId: function() {
		// Replace name part of id with new name and keep whatever follows
		// after...
		var id = this.member.getId();
		var name = this.member.name().toLowerCase();
		if (id.indexOf(name) == 0)
			id = this.base() + id.substring(name.length);
		return id;
	},

	containingClass: function() {
		return this.classObject.doc;
	},

	getVisible: function() {
		return Member.isVisible(this, true);
	},

	isMethod: function() {
		return this.type == 'method';
	},

	isConstructor: function() {
		return this.type == 'constructor';
	},

	signature: function() {
		return this.isCallable() ? this.member.signature() : '';
	},

	getNameSuffix: function() {
		return this.isCallable() ? this.member.getNameSuffix() : '';
	},

	renderParameters: function() {
		return this.isCallable() ? this.member.renderParameters() : '';
	}
});
