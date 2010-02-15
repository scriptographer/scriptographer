/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.org/ 
 */

DocObject = Object.extend({
	initialize: function(doc) {
		this.doc = doc;
		this.children = new Hash();
	},

	addChild: function(mem) {
		this.children[mem.qualifiedName()] = mem;
	},

	removeChild: function(mem) {
		delete this.children[mem.qualifiedName()];
	},

	name: function() {
		return this.doc.name();
	},

	qualifiedName: function() {
		return this.doc.qualifiedName();
	},

	toString: function() {
		return this.doc.toString();
	},

	/**
	 * Produces a table-of-contents for classes and calls renderClass on each
	 * class.
	 */
	renderHierarchy: function(first) {
		var classes = this.children.sortBy(function(mem) {
			return settings.classOrder[mem.name()] || Number.MAX_VALUE;
		});
		return this.renderTemplate('packages#hierarchy', {
			classes: classes,
			first: first
		});
	},

	statics: {
		getRelativeIdentifier: function(str) {
			return str.startsWith(settings.basePackage + '.') ?
					str.substring(settings.basePackage.length + 1) : str;
		}
	}
});