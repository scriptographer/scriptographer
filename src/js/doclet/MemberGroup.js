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
		if (member instanceof Member) {
			// This only adds object that are instanceof Member. For adding
			// native types, use #add() above.
			member.group = this;
			this.members.push(member);
			Member.put(member);
			return true;
		} else if (member.isField()) {
			return this.add(new Member(this.classObject, member));
		} else if (member.isMethod() || member.isConstructor()) {
			// Only group functions
			var name = member.name();
			if (settings.methodFilter && member.isMethod()) {
				// filter out methods that are not allowed:
				if (settings.methodFilter.some(function(filter) {
					return filter == name;
				})) return false;
			}

			// See if we can add to an existing Member, based on compatible
			// variable parameter versions:
			if (!this.members.find(function(existing) {
				return existing.add(member);
			})) {
				// Couldn't add to an existing Member, try creating a new one:
				var mem = new Method(this.classObject);
				if (mem.add(member))
					return this.add(mem);
			}
		}
		return false;
	},

	isEmpty: function() {
		return !this.members.length;
	},

	remove: function(member) {
		if (member) {
			// Remove a member from this group, and the group from its parent
			// list if its empty.
			if (this.members.remove(member)) {
				Member.remove(member);
				if (this.isEmpty())
					this.remove();
				return true;
			}
		} else if (this.list) {
			// Remove this group form its parent list
			this.list.remove(this);
		}
	},

	/**
	 * Removes all 'sub-methods' of a given method from any of the method members.
	 * Since these can be groups of JS style compatible methods, their members
	 * need to be iterated as well.
	 * synthetic is an optional second parameter, to let the removed members
	 * know the cause of their removal (a synthetic member that they are converted
	 * into). This is used for example for @link.
	 */
	removeMethod: function(method, synthetic) {
		if (method && method instanceof Method) {
			method.methods.each(function(meth) {
				this.members.each(function(member) {
					if (member instanceof Method && member.remove(meth))
						meth.synthetic = synthetic;
				});
			}, this);
		}
	},

	/**
	 * Removes all methods and sub-methods contained in the given group by
	 * looping through its members and passing them to removeMethod.
	 */
	removeMethods: function(group, synthetic) {
		if (group && group instanceof MemberGroup) {
			group.members.each(function(member) {
				this.removeMethod(member, synthetic);
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
	},

	statics: {
		get: function(reference, classDoc) {
			var [cls, name] = reference.split('#');
			if (classDoc && !/\./.test(cls))
				cls = classDoc.containingPackage().qualifiedName() + '.' + cls;
			var classObject = ClassObject.get(cls);
			// If it's a hidden class, force creation through ClassObject.put
			if (!classObject)
				classObject = ClassObject.put(cls, true);
			return classObject.getGroup(name);
		}
	}
});
