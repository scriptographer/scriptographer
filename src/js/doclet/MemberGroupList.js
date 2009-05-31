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
		this.methodLookup = new Hash();
	},

	add: function(member) {
		var key = member.name(), name, group;
		if (member instanceof ExecutableMemberDoc) {
			name = key;
			if (member instanceof MethodDoc)
				// For methods, use the return type for grouping as well!
				var type = member.returnType();
				if (type)
					key = type.typeName() + type.dimension() + ' ' + name;

			group = this.groups[key];
			if (!group) {
				group = new MemberGroup(this.classObject, name); 
				this.methodLookup[name] = group;
			}
		} else {
			// Fields won't be grouped, but for simplicty,
			// greate groups of one element for each field,
			// so it can be treated the same as functions:
			group = new MemberGroup(this.classObject, key); 
		}
		group.add(member);
		this.groups[key] = group;
	},

	addAll: function(members) {
		members.each(function(member) {
			this.add(member);
		}, this);
	},

	init: function() {
		this.groups.each(function(group) {
			group.init();
		});
	},

	contains: function(key) {
		return this.groups[key] != null;
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

	scanBeanProperties: function(fields) {
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
				if (!fields.contains(property)) {
					var getter = group.extractGetter(), setter = null;
					if (getter) {
						// We have a getter. Now, do we have a setter?
						var setters = this.methodLookup['set' + component];
						// Is this value a method?
						if (setters)
							setter = setters.extractSetter(getter.returnType());
						// Make the property.
						var bean = new BeanProperty(this.classObject, property, getter, setter, setters);
						if (bean.isVisible())
							fields.add(bean);
					}
				}
			}
		}, this);
	},

	contains: function(name) {
		return this.groups.has(name);
	}
});
