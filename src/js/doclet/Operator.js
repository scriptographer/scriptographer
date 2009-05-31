/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 */
Operator = SyntheticField.extend({
	initialize: function(classObject, name, member, members) {
		this.base(classObject, name, member);
		this.title = Operator.getName(name);
		this.members = members;
		// Set reference to the operator, so members can automatically 
		// be hidden without removing them.
		var tags = [];
		members.members.each(function(member) {
			// Make sure we're only setting it on real setters.
			// There might be other functions with more than one parameter,
			// which still need to show in the documentation.
			if (Operator.isOperator(member)) {
				member.synthetic = this;
				if (!tags.tags)
					tags = member.inlineTags();
			}
		}, this);
		this.inlineTagList = tags;
	},

	name: function() {
		return this.title;
	},

	getId: function() {
		return this.property;
	},

	statics: {
		isOperator: function(method) {
			// As a convention, only add non static bean properties to
			// the documentation. static properties are all supposed to
			// be uppercae and constants.
			return method.parameters().length == 1 && !method.isStatic() && (
					/^(add|subtract|multiply|divide)$/.test(method.name())
					&& method.containingClass().isCompatible(method.returnType())
				) || ( // equals
					method.name() == 'equals'
					&& method.returnType().typeName() == 'boolean'
				);
		},

		getName: function(name) {
			switch (name) {
				case 'add':
					return '+ Addition';
				case 'subtract':
					return '- Subtraction';
				case 'multiply':
					return '* Multiplication';
				case 'divide':
					return '/ Division';
				case 'equals':
					return '== Comparison';
			}
		}
	}
});
