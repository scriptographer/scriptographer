/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A group of members that are unified under the same name 
 */
MemberGroup = Object.extend({
	initialize: function(classObject, members) {
		this.classObject = classObject;
		this.members = [];
		if (members) {
			members.each(function(member) {
				this.add(member);
			}, this);
		}
	},

	add: function(member) {
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

			// See if we can add to an existing Member, based on compatible
			// variable parameter versions:
			this.members.each(function(m) {
				if (m.add(member)) {
					mem = m;
					throw $break;
				}
			});
			// Couldn't add to an existing Member, create a new one:
			if (!mem) {
				mem = new Method(this.classObject);
				if (mem.add(member))
					return this.add(mem);
			}
		} else {
			if (member instanceof Member)
				mem = member;
			else
				mem = new Member(this.classObject, member);
			mem.group = this;
			this.members.push(mem);
		}
		if (mem) {
			Member.put(mem);
			return true;
		}
	},

	remove: function(member) {
		if (this.members.remove(member)) {
			Member.remove(member);
			if (!this.members.length)
				this.list.remove(this);
			return true;
		}
	},

	/**
	 * Removes all 'sub-methods' of a given method from any of the method members.
	 * Since these can be groups of JS style compatible methods, their members
	 * need to be iterated as well.
	 */
	removeMethod: function(method) {
		if (method && method instanceof Method) {
			method.methods.each(function(meth) {
				this.members.each(function(member) {
					if (member instanceof Method)
						member.remove(meth);
				});
			}, this);
		}
	},

	/**
	 * Removes all methods and sub-methods contained in the given group by
	 * looping through its members and passing them to removeMethod.
	 */
	removeMethods: function(group) {
		if (group && group instanceof MemberGroup) {
			group.members.each(function(member) {
				this.removeMethod(member);
			}, this);
		}
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
		var getter = this.members.find(function(method) {
			return method.extractGetter();
		});
		return getter && new Method(this.classObject, getter);
	},

	extractSetters: function(type) {
		var setters = this.members.each(function(method) {
			this.append(method.extractSetters(type));
		}, []);
		return setters.length && new MemberGroup(this.classObject, setters);
	},

	extractOperators: function() {
		var operators = this.members.each(function(method) {
			this.append(method.extractOperators());
		}, []);
		return operators.length && new MemberGroup(this.classObject, operators);
	}
});
