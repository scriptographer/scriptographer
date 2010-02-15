/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.org/ 
 */

// Define settings from passed options:
var settings = {
	basePackage:  options.basepackage || '',
	destDir: (options.d + (options.d && !/\/^/.test(options.d) ? '/' : '')) || '',
	docTitle: options.doctitle || '',
	bottom: options.bottom || '',
	author: options.author || '',
	methodFilter: (options.methodfilter || '').trim().split(/\s+/),
	classFilter: (options.classfilter || '').trim().split(/\s+/),
	classMatch: options.classmatch.trim() ? new RegExp('(' + options.classmatch.trim().replace(/\s/g, '|') + ')$', 'g') : null,
	packageSequence: (options.packagesequence || '').trim().split(/\s+/),
	classOrder: (function() {
		var classOrder = new Hash();
		if (options.classorder) {
			var file = new java.io.BufferedReader(new java.io.FileReader(options.classorder));
			var line, count = 1;
			while ((line = file.readLine()) != null) {
				line = line.trim();
				if (line.length)
					classOrder[line.trim()] = count++;
			}
		}
		return classOrder;
	})(),
	sortMembers: options.sortmembers == 'true',
	templates: options.templates == 'true',
	inherited: options.noinherited != 'true',
	summaries: options.nosummaries != 'true',
	fieldSummary: options.nofieldsummary != 'true',
	constructorSummary: options.noconstructorsummary != 'true',
	hyperref: options.nohyperref != 'true',
	versionInfo: options.version == 'true',
	debug: options.shortinherited == 'true',
	headings: {}
};

// Section headings
for (var i = 1; i <= 4; i++) {
	settings.headings[i] = { 
		open: options['heading' + i + 'open'] || '<h' + i + '>',
		close: options['heading' + i + 'close'] || '</h' + i + '>'
	}
}
