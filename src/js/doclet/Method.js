/**
 * JavaScript Doclet
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

/**
 * A group of members that are all 'compatible' in a JS way, e.g. have the same
 * amount of parameter with different types each (e.g. setters)
 * or various amount of parameters with default parameter versions, e.g.
 * all com.scriptogrpaher.ai.Pathfinder functions
 */
Method = Member.extend({
	initialize: function(classObject, member) {
		this.base(classObject);
		this.isGrouped = false;
		this.members = [];
		this.map = new Hash();
		if (member)
			this.add(member);
	},

	add: function(member) {
		var swallow = true;
		// do not add base versions for overridden functions 
		var signature = member.signature();
		if (this.map[signature])
			swallow = false;
		this.map[signature] = member;
		if (swallow) {
			// see wther the new member fits the existing ones:
			if (this.members.find(function(mem) {
				return !mem.isCompatible(member);
			})) return false;
			this.isGrouped = true;
			this.members.push(member);
		}
		// Just point member to the first of the members, for name, signature, etc.
		if (!this.member)
			this.member = member;
		return true;
	},

	init: function() {
		if (this.isGrouped) {
			// see if all elements have the same amount of parameters
			var sameParamCount = true;
			var firstCount = -1;
			this.members.each(function(mem) {
				var count = mem.parameters().length;
				if (firstCount == -1) {
					firstCount = count;
				} else if (count != firstCount) {
					sameParamCount = false;
					throw $break;
				}
			});
			if (sameParamCount) {
				// find the suiting member: take the one with the most documentation
				var maxTags = -1;
				this.members.each(function(mem) {
					var numTags = mem.inlineTags().length;
					if (numTags > maxTags) {
						this.member = mem;
						maxTags = numTags;
					}
				}, this);
			} else {
				// now sort the members by param count:
				this.members = this.members.sortBy(function(mem) {
					return mem.parameters().length;
				});
				this.member = this.members.last;
			}
		} else {
			this.member = this.members.first;
		}
	},

	getNameSuffix: function() {
		return this.renderParameters();
	},

	getOverriddenMethodToUse: function() {
		if (this.member instanceof MethodDoc) {
			if (!this.member.commentText() &&
				!this.member.seeTags().length &&
				!this.member.throwsTags().length &&
				!this.member.paramTags().length) {
				// No javadoc available for this method. Recurse through
				// superclasses
				// and implemented interfaces to find javadoc of overridden
				// methods.
				var overridden = this.member.overriddenMethod();
				if (overridden) {
					print('OR', this.member, overridden);
					var mem = Member.get(overridden);
					if (mem) {
						print(mem.member);
					}
					// Prevent endless loops that happen when overriden
					// functions from inivisble classes where moved to the
					// derived class and Member.get lookup points there
					// instead of the overridden version:
					if (mem && mem.member.containingClass() != this.member.overriddenClass()) {
						mem = null;
					}
					// If this method is not wrapped, quickly wrap it just to
					// call renderMember.
					if (!mem)
						mem = new Method(this.classObject, overridden);
					return mem;
				}
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

	renderMember: function(cd, index, member) {
		var overridden = this.getOverriddenMethodToUse();
		if (overridden)
			return overridden.renderMember(cd, index, member);
		else
			return this.base(cd, index, member, this.containingClass());
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
				var prevCount = 0;
				var closeCount = 0;
				this.members.each(function(mem) {
					var params = mem.parameters();
					var count = params.length;
					if (count > prevCount) {
						if (prevCount)
							buf.push('[');
						for (var i = prevCount; i < count; i++) {
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
		return this.member instanceof MethodDoc ?
				new Type(this.member.returnType()) : null;
	},

	isSimilar: function(obj) {
		if (obj instanceof Method)
			return this.isStatic() == obj.isStatic() &&
				this.name() == obj.name() &&
				this.renderParameters() == obj.renderParameters();
		return false;
	}
});
