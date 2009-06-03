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
	initialize: function(classObject, name, operators) {
		// Set operator to the one with the documentation, so isVisible uses it too
		var operator = operators.members.find(function(method) {
			var tags = method.inlineTags();
			if (tags.length)
				return method;
		}) || operators.members.first;
		this.base(classObject, name, operator);
		this.title = Operator.getName(name);
		this.operators = operators;
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
