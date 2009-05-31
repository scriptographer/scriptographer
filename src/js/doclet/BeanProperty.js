/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A virtual field that unifies getter and setter functions, just like Rhino does
 */
BeanProperty = SyntheticField.extend({
	initialize: function(classObject, name, getter, setter, setters) {
		this.base(classObject, name, getter); // this.member is the getter
		if (setters) {
			setters.members.each(function(member) {
				// Make sure we're only setting it on real setters.
				// There might be other functions with more than one parameter,
				// which still need to show in the documentation.
				if (BeanProperty.isSetter(member))
					member.synthetic = this;
				// Set setter to the one with the documentation, so isVisible uses it too
				var tags = member.inlineTags();
				if (tags.length)
					setter = member;
			}, this);
		}
		this.setter = setter;
		this.setters = setters;

		var tags = getter.inlineTags();
		// Use the setter that was found to have documentation in the loop above
		if (!tags.length && setter)
			tags = setter.inlineTags();

		this.inlineTagList = [];
		if (!setter)
			this.inlineTagList.push(new Tag('Read-only. '))
		this.inlineTagList.append(tags);
	},

	getVisible: function() {
		// SG Convention: Hide read-only is-getter beans and show is-method instead.
		if (/^is/.test(this.member.name()) && !this.setter)
			return false;
		return this.base() && (!this.setter || Member.isVisible(this.setter));
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
					|| params[0].typeName() == type.typeName()
					// TODO: checking both hasSuperclass and hasInterface is necessary to simulate isAssignableFrom
					// Think of adding this to Type, and calling it here
					|| conversion && (typeClass = type.asClassDoc())
						&& (paramName = params[0].paramType().qualifiedName())
						&& (typeClass.hasSuperclass(paramName) || typeClass.hasInterface(paramName)));
		}
	}
});
