/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
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
		// Rule 3: If this is one of the has 0 parameters and we're looking at constructors,
		// do not count it as compatible
		if (mem1.isConstructor() && !count)
			return false;
		// Rule 4: If not the same amount of params, the types and names need to be the same:
		for (var i = 0; i < count; i++) {
			if (params1[i].name() != params2[i].name()
				|| !params1[i].paramType().isCompatible(params2[i].paramType()))
				return false;
		}
		return true;
	}

	return {
		initialize: function(classObject, method) {
			this.base(classObject);
			this.isGrouped = false;
			this.methods = [];
			this.added = {};
			if (method)
				this.add(method);
		},

		add: function(method) {
			// Do not add base versions for overridden functions 
			var signature = method.signature();
			if (!this.added[signature]) {
				// See wether the new method fits the existing ones:
				if (this.methods.find(function(mem) {
					return !isCompatible(mem, method);
				})) return false;
				// Filter out abstract methods
				if (method.isMethod() && method.isAbstract())
					return false;
				// Filter out methods that do not define a concrete generic
				var type = method.isMethod() && method.returnType();
				if (type && type.bounds && type.bounds().length == 0) {
					return false;
				}
				this.isGrouped = true;
				this.methods.push(method);
				// Just point method to the first of the methods, for name, signature, etc.
				// This is corrected in init(), if grouping occurs.
				if (!this.member)
					this.member = method;
				this.added[signature] = true;
			}
			// Always return true, even if this was added before, to 'swallow'
			// identical methods from superclasses and not have MemerGroup
			// create a new Method for it instead.
			return true;
		},

		remove: function(method) {
			if (this.methods.remove(method)) {
				if (this.member == method)
					this.member = this.methods.first;
				if (this.member) {
					this.init();
				} else {
					this.group.remove(this);
				}
			}
		},

		init: function() {
			if (this.isGrouped) {
				// See if all elements have the same amount of parameters
				var sameParamCount = true;
				var firstCount = -1;
				this.methods.each(function(mem) {
					var count = mem.parameters().length;
					if (firstCount == -1) {
						firstCount = count;
					} else if (count != firstCount) {
						sameParamCount = false;
						throw $break;
					}
				});
				if (sameParamCount) {
					// Find the suiting method: take the one with the most documentation
					var maxTags = -1;
					this.methods.each(function(mem) {
						var numTags = mem.inlineTags().length;
						if (numTags > maxTags) {
							this.member = mem;
							maxTags = numTags;
						}
					}, this);
				} else {
					// Now sort the methods by param count:
					this.methods = this.methods.sortBy(function(mem) {
						return mem.parameters().length;
					});
					this.member = this.methods.last;
				}
			} else {
				this.member = this.methods.first;
			}
		},

		signature: function() {
			return this.member.signature();
		},

		getNameSuffix: function() {
			return this.renderParameters();
		},

		getOverriddenMethodToUse: function() {
			function isEmpty(member) {
				return !member.commentText() &&
					!member.seeTags().length &&
					!member.throwsTags().length &&
					!member.paramTags().length;
			}
			if (this.member.isMethod() && this.isVisible() && isEmpty(this.member)) {
				// No javadoc available for this method. Recurse through
				// superclasses
				// and implemented interfaces to find javadoc of overridden
				// methods.
				var overridden = this.member.overriddenMethod();
				if (overridden && !isEmpty(overridden)) {
					var mem = Member.get(overridden);
					// Prevent endless loops that happen when overriden
					// functions from inivisble classes where moved to the
					// derived class and Member.get lookup points there
					// instead of the overridden version:
					if (mem && mem.member.containingClass() != this.member.overriddenClass())
						mem = null;
					// If this method is not wrapped, quickly wrap it just to
					// call renderMember.
					if (!mem)
						mem = new Method(this.classObject, overridden);
					return mem;
				}
			}
		},

		renderSummary: function(classDoc) {
			var overridden = this.getOverriddenMethodToUse();
			if (overridden)
				return overridden.renderSummary(classDoc);
			else
				return this.base(classDoc);
		},

		renderMember: function(param) {
			var overridden = this.getOverriddenMethodToUse();
			if (overridden) {
				return overridden.renderMember(param);
			} else {
				return this.base(param);
			}
		},

		getParameters: function() {
			var params = this.member.parameters();
			if (params.length) {
				// Link parameters to original parameter tags:
				var lookup = this.member.paramTags().each(function(tag) {
					this[tag.parameterName()] = tag;
				}, {});
				// Set the links
				params.each(function(param) {
					param.tag = lookup[param.name()];
				});
				return params;
			}
		},

		renderParameters: function() {
			if (!this.renderedParams) {
				var buf = [];
				buf.push('(');
				if (this.isGrouped) {
					var prevCount = -1;
					var closeCount = 0;
					this.methods.each(function(mem) {
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
			return this.classObject.classDoc;
		},

		containingPackage: function() {
			return this.classObject.classDoc.containingPackage();
		},

		parameters: function() {
			return this.member.parameters();
		},

		returnType: function() {
			// Let clone report the right class type, not basic java Object
			if (this.member instanceof MethodDoc) {
				if (this.member.name() == 'clone') {
					return this.containingClass();
				} else {
					return new Type(this.member.returnType());
				}
			}
		},

		isSimilar: function(obj) {
			if (obj instanceof Method) {
				return this.isStatic() == obj.isStatic() &&
					this.name() == obj.name() &&
					this.renderParameters() == obj.renderParameters();
			}
			return false;
		},

		isEmpty: function() {
			return !this.methods.length;
		},

		extractGetter: function() {
			return this.methods.find(function(method) {
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
				this.methods.each(function(method) {
					if (!added[method.qualifiedName() + method.signature()]
							&& BeanProperty.isSetter(method, type, pass == 2))
						setters.push(method);
				});
			}
			return setters;
		},

		extractOperators: function() {
			return this.methods.collect(function(method) {
				if (Operator.isOperator(method))
					return method;
			});
		}
	}
});
