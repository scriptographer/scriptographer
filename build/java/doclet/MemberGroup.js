/**
 * JavaScript Doclet
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A list of members that are unified under the same name 
 */
MemberGroup = Object.extend({
	initialize: function(classObject, name) {
		this.classObject = classObject;
		this.members = [];
		// Used in scanBeanProperties:
		this.name = name;
	},

	add: function(member, lookupName) {
		var mem = null;
		// only group functions
		if (member instanceof ExecutableMemberDoc) {
			var name = member.name();
			if (settings.methodFilter && member instanceof MethodDoc) {
				// filter out methods that are not allowed:
				if (settings.methodFilter.some(function(filter) {
					return filter == name;
				})) return false;
			}

			// See if we can add to an existing Member:
			this.members.each(function(m) {
				if (m.add(member)) {
					mem = m;
					throw $break;
				}
			});
			// couldn't add to an existing Member, create a new one:
			if (!mem) {
				mem = new Method(this.classObject, name);
				if (mem.add(member))
					this.members.push(mem);
			}
		} else {
			if (member instanceof Member)
				mem = member;
			else
				mem = new Member(this.classObject, member);
			this.members.push(mem);
		}
		if (mem) {
			// BeanProperties do not need to be put in the lookup table
			// They are the only ones passed as Member already, so there
			// will not be a qualifiedName funciton anyway...
			if (!lookupName && member.qualifiedName)
				lookupName = member.qualifiedName();
			if (lookupName)
				Member.put(lookupName, mem);
		}
		return mem != null;
	},

	init: function() {
		this.members.each(function(member) {
			member.init();
		});
	},

	appendTo: function(list) {
		return list.append(this.members);
	},

	extractGetter: function() {
		return this.members.find(function(method) {
			if (BeanProperty.isGetter(method))
				return method;
		}, this);
	},

	extractSetter: function(type) {
		// Make two passes: the first to find a method with direct type
		// assignment, and a second one to find a widening conversion.
		var found = null;
		for (var pass = 1; pass <= 2 && !found; ++pass) {
			found = this.members.find(function(method) {
				if (BeanProperty.isSetter(method, type, pass == 2))
					return method;
			}, this);
		}
		return found;
	}
});
