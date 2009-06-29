/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 */
Operator = SyntheticMember.extend(new function() {
	var operators = {
		add: '+', subtract: '-', multiply: '*', divide: '/', equals: '=='
	};
	var names = {
		add: 'Addition', subtract: 'Subtraction', multiply: 'Multiplication',
		divide: 'Division', equals: 'Comparison'
	};

	return {
		type: 'operator',

		initialize: function(classObject, operators) {
			var operator = operators.members[0];
			this.base(classObject, operator.name(), operator);
			this.operators = operators;
			Member.put(operators, this);
		},

		name: function() {
			return this.member.name();
		},

		getId: function() {
			return this._name;
		},

		renderMember: function(param) {
			// Clone so we're not modifying the globally used param object
			param = param.clone();
			if (this.isVisible()) {
				param.text = this.renderTemplate('operators', param);
				param.collapsedTitle = this.operators.members.map(function(member) {
					return Operator.getTitle(member);
				}).join(', ');
				param.expandedTitle = Operator.getTitle(this.member);
				return this.base(param);
			}
		},

		isField: function() {
			return true;
		},

		statics: {
			isOperator: function(method) {
				// As a convention, only add non static bean properties to
				// the documentation. static properties are all supposed to
				// be uppercae and constants.
				return method.parameters().length == 1 && !method.isStatic() && (
						/^(add|subtract|multiply|divide)$/.test(method.name())
						&& method.containingClass().isCompatible(new Type(method.returnType()))
					) || ( // equals
						method.name() == 'equals'
						&& method.returnType().typeName() == 'boolean'
					);
			},

			getOperator: function(member) {
				return operators[member.name()];
			},

			getName: function(member) {
				return names[member.name()];
			},

			getTitle: function(member) {
				var entry = member.getParameters()[0];
				return '<tt><b>' + Operator.getOperator(member) + '</b> ' + stripTags(entry.param.paramType().renderLink()).trim() + '</tt>';
			}
		}
	}
});
