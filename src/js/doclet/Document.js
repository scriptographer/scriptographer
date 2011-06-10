/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://scriptographer.org/ 
 */

Document = Object.extend({
	initialize: function(path, name, template) {
		this.name = name;
		this.template = template;

		// Split into packages and create subdirs:
		var parts = path.split(/\./);
		if (!settings.templates)
			parts.unshift('packages');
		path = '';
		var levels = 0;
		parts.each(function(part) {
			if (part == name || !part)
				throw $break;

			path += part + '/';
			levels++;
			var dir = new java.io.File(settings.destDir + path);
			if (!dir.exists())
				dir.mkdir();
		});

		this.basePath = '';
		for (var j = 1; j < levels; j++)
			this.basePath += '../';

		// Store the previous base
		this.previousBase = Document.basePath;
		// And set the current base
		Document.basePath = this.basePath;

		// Push out so the content for the document can be written to it.
		out.push();

		// Only add extension if it wasn't already
		var fileName = name.indexOf('.') != -1 ? name : name + '.html';

		this.writer = new java.io.PrintWriter(
				new java.io.FileWriter(settings.destDir + path + fileName));
	},

	close: function() {
		this.content = out.pop();
		if (this.writer) {
			this.writer.print(this.renderTemplate(this.template));
			this.writer.close();
		}
		// Restore previous base
		Document.basePath = this.previousBase;
	},

	statics: {
		basePath: '',

		getBasePath: function() {
			return this.basePath;
		}
	}
});
