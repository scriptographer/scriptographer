/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://scriptographer.org/ 
 */

/**
 * A group of methods that are all 'compatible' in a JS way, e.g. have the same
 * amount of parameter with different types each (e.g. setters)
 * or various amount of parameters with default parameter versions, e.g.
 * all com.scriptogrpaher.ai.Pathfinder functions
 */
Method = Member.extend(new function() {

	// Checks wether two 'native' methods are compatible in the JS sense, using
	// vararg rules.
	function isCompatible(mem1, mem2) {
		// Rule 1: Same 'staticness'
		if (mem1.isStatic() != mem2.isStatic())
			return false;
		// Rule 2: Same return type
		if (mem1.isMethod() && mem1.returnType().qualifiedTypeName()
			!= mem2.returnType().qualifiedTypeName())
			return false;
		var params1 = mem1.parameters();
		var params2 = mem2.parameters();
		var count = Math.min(params1.length, params2.length);
		// Rule 3: If not the same amount of params, the types and names need to be the same:
		for (var i = 0; i < count; i++) {
			if (params1[i].name() != params2[i].name()
				|| !params1[i].paramType().isCompatible(params2[i].paramType()))
				return false;
		}
		return true;
	}

	function getCommentedMethod(member, classObject) {
		if (member.isMethod() && !member.getRawCommentText()) {
			// No javadoc available for this method. Recurse through
			// superclasses and implemented interfaces to find javadoc of
			// overridden methods.
			var overridden = member.overriddenMethod();
			if (!overridden) {
				// If we can't find it this way, scan through interfaces manually.
				overridden = member.containingClass().interfaces().find(function(face) {
					return face.methods(true).find(function(method) {
						if (member.overrides(method))
							return method;
					});
				});
			}
			if (overridden) {
				if (overridden.getRawCommentText()) {
					var mem = Member.get(overridden);
					// Prevent endless loops that happen when overriden
					// functions from inivisble classes where moved to the
					// derived class and Member.get lookup points there
					// instead of the overridden version:
					if (mem && (mem.member.containingClass() != member.overriddenClass()
							|| !(mem instanceof Method)))
						mem = null;
					// If this method is not wrapped, quickly wrap it just to
					// call renderMember.
					if (!mem)
						mem = new Method(classObject, overridden);
					if (!mem.isEmpty())
						return mem;
				}
				return getCommentedMethod(overridden, classObject);
			}
		}
	}

	return {

		type: 'method',

		initialize: function(classObject, method) {
			this.base(classObject);
			this.isGrouped = false;
			this.members = [];
			this.added = {};
			if (method)
				this.add(method, true);
		},

		add: function(method, force) {
			// Do not add superclass versions for overridden methods 
			var signature = method.signature();
			if (!this.added[signature]) {
				this.added[signature] = true;
				if (!force) {
					// See wether the new method fits the existing ones:
					if (this.members.find(function(mem) {
						return !isCompatible(mem, method);
					})) return false;
					// Filter out methods that do not define a concrete generic
					var type = method.isMethod() && method.returnType();
					if (type && type.bounds && type.bounds().length == 0)
						return false;
				}
				this.isGrouped = true;
				this.members.push(method);
				Member.put(method, this);
				// Just point method to the first of the methods, for name, signature, etc.
				// This is corrected in init(), if grouping occurs.
				if (!this.member)
					this.member = method;
			}
			// Always return true, even if this was added before, to 'swallow'
			// identical methods from superclasses and not have MemerGroup
			// create a new Method for it instead.
			return true;
		},

		remove: function(method) {
			if (this.members.remove(method)) {
				Member.remove(method, this);
				if (this.member == method)
					this.member = this.members.first;
				if (this.member) {
					this.init();
				} else {
					this.group.remove(this);
				}
				return true;
			}
		},

		init: function() {
			if (this.isGrouped) {
				// See if all elements have the same amount of parameters
				var sameParamCount = true;
				var firstCount = -1;
				var minCount = Number.MAX_VALUE;
				this.members.each(function(mem) {
					var count = mem.parameters().length;
					minCount = Math.min(count, minCount);
					if (firstCount == -1) {
						firstCount = count;
					} else if (count != firstCount) {
						sameParamCount = false;
					}
				}, this);
				this.minCount = minCount;
				if (sameParamCount) {
					// Find the suiting method: take the one with the most documentation
					var maxLength = -1;
					this.member = null;
					var found = null;
					this.members.each(function(mem) {
						var length = mem.getRawCommentText().length;
						if (length > maxLength) {
							found = mem;
							if (Member.isVisible(mem))
								this.member = mem;
							maxLength = length;
						}
					}, this);
					// If we have not found a visible one with documentation,
					// use the jshide one then for the whole group.
					if (!this.member)
						this.member = found;
				} else {
					// Now sort the methods by param count:
					this.members = this.members.sortBy(function(mem) {
						return mem.parameters().length;
					});
					this.member = this.members.last;
				}
			} else {
				this.member = this.members.first;
			}
		},

		signature: function() {
			return this.member.signature();
		},

		getNameSuffix: function() {
			return this.renderParameters();
		},

		getCommentedMember: function() {
			if (this._commented === undefined)
				this._commented = getCommentedMethod(this.member, this.classObject)
						|| this.member;
			return this._commented;
		},

		getParameters: function() {
			var params = this.member.parameters();
			// Link parameters to original parameter tags:
			var lookup = this.member.paramTags().each(function(tag) {
				this[tag.parameterName()] = tag;
			}, {});
			// Set the links
			return params.map(function(param, i) {
				return {
					param: param,
					tag: lookup[param.name()],
					optional: i >= this.minCount
				}
			}, this);
		},

		getId: function() {
			var params = this.member.parameters().collect(function(param, i) {
				if (i >= this.minCount)
					throw Base.stop;
				return param.name();
			}, this).join('-');
			var id = (this.name() + (params ? '-' + params : '')).toLowerCase();
			// Add the class name to the id for static methods, in case there's
			// a non static one with the same arguments
			if (this.isStatic())
				id = this.containingClass().name().toLowerCase() + '-' + id;
			return id;
		},

		renderParameters: function() {
			if (!this.renderedParams) {
				var buf = [];
				buf.push('(');
				if (this.isGrouped) {
					var prevCount = -1;
					var closeCount = 0;
					this.members.each(function(mem) {
						var params = mem.parameters();
						var count = params.length;
						if (count > prevCount) {
							if (prevCount >= 0)
								buf.push('[');
							for (var i = Math.max(0, prevCount); i < count; i++) {
								if (i) buf.push(', ');
								buf.push(params[i].name());
							}
							closeCount++;
							prevCount = count;
						}
					});
					for (var i = 1; i < closeCount; i++)
						buf.push(']');
				} else {
					var params = this.member.parameters();
					for (var i = 0; i < params.length; i++) {
						if (i) buf.push(', ');
						buf.push(params[i].name());
					}
				}
				buf.push(')');
				this.renderedParams = buf.join('');
			}
			return this.renderedParams;
		},

		containingClass: function() {
			return this.classObject.doc;
		},

		containingPackage: function() {
			return this.classObject.doc.containingPackage();
		},

		parameters: function() {
			return this.member.parameters();
		},

		returnType: function() {
			// Let clone report the right class type, not basic java Object
			if (this.member.isMethod()) {
				if (this.member.name() == 'clone') {
					return this.containingClass();
				} else {
					return new Type(this.member.returnType());
				}
			} else if (this.member.isConstructor()) {
				return this.containingClass();
			}
		},

		isCompatible: function(obj) {
			if (obj instanceof Method) {
				// Loop through each single 'native' method and call isCompatible
				// on it agian.
				return obj.members.find(function(mem) {
					return this.isCompatible(mem);
				}, this);
			} else {
				return this.members.find(function(mem) {
					return isCompatible(mem, obj);
				}, this);
			}
		},

		isEmpty: function() {
			return !this.members.length;
		},

		getVisible: function() {
			// Check if the method is overriding another, and if that one was
			// already added to a visible superclass.
			// Only do so if it does not have a comment, in which case
			// it might do something differently and should show.
			if (this.member.isMethod() && !this.member.getRawCommentText()) {
				var overridden = this.member.overriddenMethod();
				if (overridden && overridden.containingClass().isVisible())
					return false;
			}
			return this.base();
		},


		extractGetter: function() {
			return this.members.find(function(method) {
				if (BeanProperty.isGetter(method))
					return method;
			});
		},

		extractSetters: function(type) {
			// Make two passes: the first to find a method with direct type
			// assignment, and a second one to find a widening conversion.
			var setters = [];
			var added = {};
			for (var pass = 1; pass <= 2; ++pass) {
				this.members.each(function(method) {
					if (!added[method.qualifiedName() + method.signature()]
							&& BeanProperty.isSetter(method, type, pass == 2))
						setters.push(method);
				});
			}
			return setters;
		},

		extractOperators: function() {
			return this.members.collect(function(method) {
				if (Operator.isOperator(method))
					return method;
			});
		}
	}
});
