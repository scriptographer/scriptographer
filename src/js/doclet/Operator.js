/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 */
Operator = SyntheticField.extend(new function() {
	var operators = {
		add: '+', subtract: '-', multiply: '*', divide: '/', equals: '=='
	};
	var names = {
		add: 'Addition', subtract: 'Subtraction', multiply: 'Multiplication',
		divide: 'Division', equals: 'Comparison'
	};
	return {
		initialize: function(classObject, operators) {
			var operator = operators.members[0];
			this.base(classObject, operator.getId(), operator);
			this.operators = operators;
			this.title = Operator.getOperator(operator) + ' ' + Operator.getName(operator);
		},

		name: function() {
			return this.title;
		},

		getId: function() {
			return this.property;
		},

		renderMember: function(param) {
			if (this.isVisible()) {
				var text = this.operators.members.map(function(member) {
					var params = member.renderParameters();
					return member.renderTemplate('member#operator', param);
				}).join('');
				return this.base(Hash.merge({ text: text }, param));
			}
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

			getTitle: function(member) {
				var params = member.renderParameters();
				return Operator.getOperator(member) + ' ' + params.substring(1, params.length - 1);
			},

			getOperator: function(member) {
				return operators[member.name()];
			},

			getName: function(member) {
				return names[member.name()];
			}
		}
	}
});
