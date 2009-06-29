/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

importPackage(Packages.com.sun.javadoc);
importPackage(Packages.com.sun.tools.javadoc);
importPackage(Packages.com.scriptographer.script);

// Basics
include('lib/bootstrap.js');
include('lib/Template.js');
include('String.js');

// DocObjects
include('DocObject.js');
include('PackageObject.js');
include('ClassObject.js');

// Members
include('Member.js');
include('Method.js');
include('SyntheticMember.js');
include('ReferenceMember.js');
include('BeanProperty.js');
include('Operator.js');

// Member Grouping
include('MemberGroup.js');
include('MemberGroupList.js');

// Others
include('Type.js');
include('Tag.js');
include('Document.js');

// Rendering
include('render.js');
include('macros.js');
include('filters.js');

include('settings.js');

// Helper functions to print to out / err

function print() {
	java.lang.System.out.println($A(arguments).join(' '));
}

function error() {
	java.lang.System.err.println($A(arguments).join(' '));
}

// Change error handling here to just throw errors. This allows us to catch
// then on execution time, not as error text in the rendered output
Template.inject({
	reportMacroError: function(error, command, out) {
		throw error;
	},

	// Template.directory points to the place where the templates are found.
	// The value options.directory is set by the RhinoDoclet
	statics: {
		directory: options.directory + '/templates/'
	}
});

// Add renderTemplate function with caching to all objects
Object.inject(Template.methods, true);

// A global template writer
var out = new TemplateWriter();

// A global data object to store global stuff from templates / macros:
var data = {};

function main() {
	// Create lookup for all packages
	var packages = new Hash();
	var packageSequence = settings.packageSequence;
	var createSequence = !packageSequence;
	if (createSequence)
		packageSequence = [];

	root.specifiedPackages().each(function(pkg) {
		var name = pkg.name();
		packages[name] = new PackageObject(pkg);
		if (createSequence)
			packageSequence[i] = name;
	});

	// JS-process all classes, meaning marked methods are hidden,
	// bean properties, operators and referenced members produced, etc. 
	ClassObject.process(root.classes());

	// Now start rendering:
	var doc = new Document('', settings.templates ? 'packages.js'
			: 'packages.html', 'packages');

	packageSequence.each(function(name) {
		var pkg = packages[name];
		if (pkg)
			pkg.processPackage();
	});
	doc.close();
}

main();
