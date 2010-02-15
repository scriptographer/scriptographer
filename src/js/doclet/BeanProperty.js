/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.org/ 
 */

/**
 * A virtual field that unifies getter and setter functions, just like Rhino does
 */
BeanProperty = SyntheticMember.extend({

	type: 'field',

	initialize: function(classObject, name, getter, setters) {
		if (getter instanceof MemberGroup)
			getter = getter.extractGetter();
		if (getter) {
			if (setters instanceof MemberGroup)
				setters = setters.extractSetters(getter.returnType());
			this.base(classObject, name, getter); // this.member is the getter
			this.setters = setters;
			if (setters) {
				Member.put(setters, this);
				// Set setter to the one with the documentation, so isVisible uses it too
				this.setter = setters && (setters.members.find(function(member) {
					var tags = member.inlineTags();
					if (tags.length)
						return member;
				}) || setters.members.first);
			}

			var tags = getter.inlineTags();
			// Use the setter that was found to have documentation in the loop above
			if (!tags.length && this.setter)
				tags = this.setter.inlineTags();

			this.seeTagList = [];
			this.inlineTagList = [];
			this.inlineTagList.append(tags);
			if (!this.setter)
				this.inlineTagList.push(new Tag(Template.lineBreak + Template.lineBreak + 'Read-only.'))
		}
	},

	removeMethods: function(getterGroup, setterGroup) {
		if (getterGroup)
			getterGroup.removeMethod(this.member);
		if (setterGroup)
			setterGroup.removeMethods(this.setters);
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

	isField: function() {
		return true;
	},

	getAcceptTypes: function() {
		// Caching
		if (this._acceptTypes === undefined) {
			var types = [];
			if (this.setters && this.setters.members.length > 1) {
				this.setters.members.each(function(setter) {
					var type = setter.parameters()[0].paramType();
					if (!types.find(function(other) {
						return other.isCompatible(type)
					})) {
						types.push(type);
					}
				});
			}
			// Only display accept types if they are more than one.
			this._acceptTypes = types.length > 1 ? types : null;
		}
		return this._acceptTypes;
	},

	getVisible: function() {
		// SG Convention: Hide read-only is-getter beans and show is-method instead.
		if (!this.member || /^is/.test(this.member.name()) && !this.setters)
			return false;
		return this.base() && (!this.setters || Member.isVisible(this.setter));
	},

	statics: {
		isGetter: function(method) {
			// As a convention, only add non static bean properties to
			// the documentation. static properties are all supposed to
			// be uppercae and constants.
			return method.parameters().length == 0 && !method.isStatic()
				&& method.returnType().typeName() != 'void';
		},

		isSetter: function(method, type, conversion) {
			var params = method.parameters(), typeClass, paramName;
			var param = params.length == 1 && params[0];
			return !method.isStatic()
				&& method.returnType().typeName() == 'void' && params.length == 1
				&& (!type
					|| param.typeName() == type.typeName()
					|| conversion/* && param.paramType().isCompatible(type)*/);
		}
	}
});
