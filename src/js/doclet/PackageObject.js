/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.org/ 
 */

PackageObject = DocObject.extend({
	initialize: function(doc) {
		this.base(doc);
	},

	renderPackage: function() {
		var name = this.doc.name();
		print('Rendering Package ' + name);
		// Write package file:
		var path = DocObject.getRelativeIdentifier(name);
		// We need to create document before rendering tags and links, so that
		// the basePath is set correctly.
		var index = !settings.templates && new Document(path, 'index', 'document');
		var first = renderTags({ tags: this.doc.firstSentenceTags(), packageDoc: this.doc, wrapInParagraphs: false });
		var text = renderTags({ tags: this.doc.inlineTags(), packageDoc: this.doc, wrapInParagraphs: false });
		// Remove the first sentence from the main text, and use it as a title
		if (first && text.startsWith(first)) {
			text = text.substring(first.length);
			first = stripParagraphs_filter(first);
			if (/\.$/.test(first))
				first = first.substring(0, first.length - 1); // cut away dot
		}
		if (index) {
			renderTemplate('package', {
				title: first,
				text: text
			}, out);
			index.close();
		}

		this.renderClasses(this.doc.interfaces());
		this.renderClasses(this.doc.allClasses(true));
		this.renderClasses(this.doc.exceptions());
		this.renderClasses(this.doc.errors());
		// Now produce the hierarchy:
		var hierarchy = this.renderHierarchy(true);
		// And render the classes list if it exists
		var classes = this.doc.tags('classes')[0];
		classes = classes && renderTags({
			tags: classes.inlineTags(), packageDoc: this.doc,
			linksOnly: true, stripParagraphs: true
		}).split(/\r\n|\n|\r/mg);
		renderTemplate('packages#package', {
			hierarchy: hierarchy, name: name, path: path, text: text,
			classes: classes
		}, out);
	},

	/**
	 * Uses this ClassObject as a root for classes to be processed and rendered. 
	 * Orders the classes according to their inheritance 
	 * and calls renderClass on each of the visible ones.
	 */
	renderClasses: function(classes) {
		// Loop twice, as in the second loop, superclasses are picked from nodes
		// which is filled in the firs loop
		classes.each(function(doc) {
			var cls = ClassObject.get(doc);
			if (cls) {
				doc.classObj = cls;
				this.addChild(cls);
				// Render this class
				print('Rendering ' + cls);
				cls.renderClass();
			}
		}, this);
		// Order according inheritance
		classes.each(function(doc) {
			var superclass = doc.getSuperclass();
			if (doc.classObj && superclass && superclass.classObj) {
				this.removeChild(doc.classObj);
				superclass.classObj.addChild(doc.classObj);
			}
		}, this);
	}
});