/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A list of member groups, accessible by member name:
 */
MemberGroupList = Object.extend({
	initialize: function(classObject) {
		this.classObject = classObject;
		this.groups = new Hash();
		this.methods = new Hash();
	},

	add: function(member) {
		var name = member.name(), key = name, group;
		if (member.isMethod() || member.isConstructor()) {
			if (member.isMethod()) {
				// For methods, use the return type for grouping as well!
				var type = member.returnType();
				if (type)
					key = type.typeName() + type.dimension() + ' ' + name;
			}
			group = this.groups[key];
			if (!group) {
				group = new MemberGroup(this.classObject); 
				this.methods[name] = group;
			}
		} else {
			// Fields won't be grouped, but for simplicty,
			// greate groups of one element for each field,
			// so it can be treated the same as functions:
			group = new MemberGroup(this.classObject); 
		}
		if (group.add(member)) {
			if (!this.groups[key]) {
				// Used in scanBeanProperties:
				group.name = name;
				// Reference the list so it can remove itself
				group.list = this;
				this.groups[key] = group;
				return true;
			}
		} else if (group.isEmpty()) {
			// Remove empty groups again since nothing could be added to them.
			group.remove();
		}
		return false;
	},

	addAt: function(name, member) {
		var oldGroups = this.groups;
		// In order to insert into the hash at a certain place, simply create
		// a new hash, add to it up to this point, then add the member, then
		// keep adding.
		this.groups = new Hash();
		var added = false;
		oldGroups.each(function(group, key) {
			this.groups[key] = group;
			if (/\s/.test(key))
				key = key.match(/(\w*)$/)[1];
			if (key == name)
				added = this.add(member);
		}, this);
		if (!added)
			added = this.add(member);
		return added;
	},

	addAll: function(members) {
		members.each(function(member) {
			this.add(member);
		}, this);
	},

	remove: function(group) {
		return !!this.groups.remove(group);
	},

	init: function() {
		this.groups.each(function(group) {
			group.init();
		});
	},

	get: function(name) {
		return this.groups[name] || this.methods[name];
	},

	contains: function(name) {
		return !!this.get(name);
	},

	getFlattened: function() {
		if (!this.flattened) {
			// Now sort the groups alphabetically
			var groups = settings.sortMembers ? this.groups.sortBy(function(group) {
				var name = group.name, ch = name[0];
				// Swap the case of the first char so the sorting shows
				// lowercase members first
				if (ch.isLowerCase()) ch = ch.toUpperCase();
				else ch = ch.toLowerCase();
				return ch + name.substring(1);
			}) : this.groups;
			// Flatten the list of groups:
			this.flattened = groups.each(function(group) {
				group.appendTo(this);
			}, []);
		}
		return this.flattened;
	},

	scanBeanProperties: function() {
		this.groups.each(function(group) {
			var name = group.name;
			// Is this a getter?
			var m = name.match(/^(get|is)(.*)$/), kind = m && m[1], component = m && m[2];
			if (kind && component) {
				// Find the bean property name.
				var property = component;
				var match = component.match(/^([A-Z])([a-z]+.*|$)/);
				if (match) property = match[1].toLowerCase() + match[2];
				// If we already have a member by this name, don't do this
				// property.
				if (!this.classObject.hasField(property)) {
					var setterGroup = this.get('set' + component);
					var bean = new BeanProperty(this.classObject, property, group, setterGroup);
					if (bean.isVisible()) {
						bean.removeMethods(group, setterGroup);
						this.classObject.add(bean);
					}
				}
			}
		}, this);
	},

	scanOperators: function() {
		this.groups.each(function(group) {
			var operators = group.extractOperators();
			if (operators) {
				var operator = new Operator(this.classObject, operators);
				if (operator.isVisible()) {
					group.removeMethods(operators);
					this.classObject.add(operator);
				}
			}
		}, this);
	},

	contains: function(name) {
		return this.groups.has(name);
	}
});
