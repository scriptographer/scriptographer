/**
 * JavaScript Doclet
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A virtual field that unifies getter and setter functions, just like Rhino does
 */
BeanProperty = Member.extend({
	initialize: function(classObject, name, getter, setter, setters) {
		this.base(classObject);
		this.property = name;
		this.getter = getter;
		this.setter = setter;
		this.setters = setters;
		// Set reference to the bean, so getters / setters can automatically 
		// be hidden without removing them.
		getter.bean = this;
		if (setters) {
			setters.members.each(function(setter) {
				// Make sure we're only setting it on real setters.
				// There might be other functions with more than one parameter,
				// which still need to show in the documentation.
				if (BeanProperty.isSetter(setter))
					setter.bean = this;
			}, this);
		}

		var tags = getter.inlineTags();
		if (!tags.length && setter)
			tags = setter.inlineTags();

		this.inlineTagList = [];
		if (!setter)
			this.inlineTagList.push(new Tag('Read-only. '))
		this.inlineTagList.append(tags);
	},

	name: function() {
		return this.property;
	},

	firstSentenceTags: function() {
		return this.inlineTags;
	},

	isVisible: function() {
		// SG Convention: Hide read-only is-getter beans and show is-method instead.
		if (/^is/.test(this.getter.name()) && !this.setter)
			return false;
		var getterHide = this.getter.tags('jshide')[0];
		var setterHide = this.setter && this.setter.tags('jshide')[0];
		if (getterHide) getterHide = getterHide.text();
		if (setterHide) setterHide = setterHide.text();
		return !/^(bean|all|)$/.test(getterHide) && !/^(bean|all|)$/.test(setterHide);
	},

	isStatic: function() {
		return this.getter.isStatic();
	},

	containingClass: function() {
		return this.getter.containingClass();
	},

	inlineTags: function() {
		return this.inlineTagList;
	},

	seeTags: function() {
		return this.seeTagList;
	},

	containingPackage: function() {
		return this.getter.containingPackage();
	},

	modifiers: function() {
		return '';
	},

	tags: function(tagname) {
		return [];
	},

	returnType: function() {
		return this.getter.returnType();
	},

	statics: {
		isGetter: function(method) {
			// As a convention, only add non static bean properties to
			// the documentation. static properties are all supposed to
			// be uppercae and constants.
			// TODO: Do the same on Rhino through filtering
			return method.parameters().length == 0 && !method.isStatic()
				&& method.returnType().typeName() != 'void';
		},

		isSetter: function(method, type, conversion) {
			var params = method.parameters(), typeClass, paramName;
			var param = params.length == 1 && params[0];
			return !method.isStatic()
				&& method.returnType().typeName() == 'void' && params.length == 1
				&& (!type
					|| params[0].typeName() == type.typeName()
					// TODO: checking both hasSuperclass and hasInterface is necessary to simulate isAssignableFrom
					// Think of adding this to Type, and calling it here
					|| conversion && (typeClass = type.asClassDoc())
						&& (paramName = params[0].paramType().qualifiedName())
						&& (typeClass.hasSuperclass(paramName) || typeClass.hasInterface(paramName)));
		}
	}
});
