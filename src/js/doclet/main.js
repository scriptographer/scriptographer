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

include('lib/bootstrap.js');
include('lib/Template.js');
include('String.js');
include('Type.js');
include('Tag.js');
include('Member.js');
include('Method.js');
include('SyntheticMember.js');
include('ReferenceMember.js');
include('BeanProperty.js');
include('Operator.js');
include('MemberGroup.js');
include('MemberGroupList.js');
include('ClassObject.js');
include('Document.js');
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

var data = {
	group: {}
};

// Enhance some of the javatool classes with usefull methods:

// Class helpers

// We're injecting Type.prototype into ClassDocImpl, to enhance all ClassDocs
// automatically. It's ok to do so since Rhino doesn't allow to override
// native methods, so e.g. qualifiedName won't loop endlessly.
ClassDocImpl.inject(Type.prototype);

// Parameter helpers

ParameterImpl.inject({
	paramType: function() {
		return new Type(this.type());
	}
});

// Member helpers

MemberDocImpl.inject({
	isVisible: function() {
		return Member.get(this) != null;
	},

	renderLink: function(param) {
		param = param || {};
		var mem = Member.get(this);
		return mem
			? mem.renderLink(param)
			// Invisible members do not get wrapped in Member objects, so they
			// need to at least render something that gives a hint which function
			// they would be. (e.g. when linking to invisible methods using @link)
			: code_filter((this.containingClass() != param.classDoc
				? this.containingClass().name() + (this.isStatic() ? '.' : '#')
				: '') + this.name() + (this.signature ? this.signature() : ''));
	}
});

/**
 * Produces a table-of-contents for classes and calls layoutClass on each class.
 */
function processClasses(classes) {
	var root = new ClassObject();

	// Loop twice, as in the second loop, superclasses are picked from nodes
	// which is filled in the firs loop
	classes.each(function(cd) {
		var cls = ClassObject.get(cd);
		if (cls) {
			cd.classObj = cls;
			root.addChild(cls);
		}
	});
	classes.each(function(cd) {
		var superclass = cd.getSuperclass();
		if (cd.classObj && superclass && superclass.classObj) {
			root.removeChild(cd.classObj);
			superclass.classObj.addChild(cd.classObj);
		}
	});
	root.renderHierarchy('');
}

function getRelativeIdentifier(str) {
	return str.startsWith(settings.basePackage + '.') ?
			str.substring(settings.basePackage.length + 1) : str;
}


function main() {
	ClassObject.scan(root);

	var packages = new Hash();
	var packageSequence = settings.packageSequence;
	var createSequence = !packageSequence;
	if (createSequence)
		packageSequence = [];

	// Create lookup for packages
	root.specifiedPackages().each(function(pkg) {
		var name = pkg.name();
		packages[name] = pkg;
		if (createSequence)
			packageSequence[i] = name;
	});

	// Now start rendering:
	var doc = new Document('', settings.templates ? 'packages.js'
			: 'packages.html', 'packages');

	packageSequence.each(function(name) {
		var pkg = packages[name];
		if (pkg) {
			// Write package file:
			var path = getRelativeIdentifier(name);
			// We need to create document before rendering tags and links, so that
			// the basePath is set correctly.
			var index = !settings.templates && new Document(path, 'index', 'document');
			var first = renderTags({ tags: pkg.firstSentenceTags(), packageDoc: pkg });
			var text = renderTags({ tags: pkg.inlineTags(), packageDoc: pkg });
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

			out.push();
			processClasses(pkg.interfaces());
			processClasses(pkg.allClasses(true));
			processClasses(pkg.exceptions());
			processClasses(pkg.errors());

			// Render list
			var tag = pkg.tags('packagelist')[0];
			var list = tag && renderTags({ tags: tag.inlineTags(), packageDoc: pkg });

			renderTemplate('packages#package', {
				content: out.pop(), name: name, path: path, text: text, list: list
			}, out);
		}
	});
	doc.close();
}

main();
