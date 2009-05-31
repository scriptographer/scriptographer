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
include('Type.js');
include('Tag.js');
include('Member.js');
include('Method.js');
include('BeanProperty.js');
include('MemberGroup.js');
include('MemberGroupList.js');
include('ClassObject.js');
include('Document.js');
include('macros.js');
include('filters.js');

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

// Define settings from passed options:
var settings = {
	basePackage:  options.basepackage || '',
	destDir: (options.d + (options.d && !/\/^/.test(options.d) ? '/' : '')) || '',
	docTitle: options.doctitle || '',
	bottom: options.bottom || '',
	author: options.author || '',
	filterClasses: (options.filterclasses || '').trim().split(/\s+/),
	singleClass: options.singleclass || '',
	packageSequence: (options.packagesequence || '').trim().split(/\s+/),
	methodFilter: (options.methodfilter || '').trim().split(/\s+/),
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
	section1Open: options.section1open || '<h1>',
	section1Close: options.section1close || '</h1>',
	section2Open: options.section2open || '<h2>',
	section2Close: options.section2close || '</h2>',
	section3Open: options.section2open || '<h3>',
	section3Close: options.section2close || '</h3>'
};

// A global data object to store global stuff from templates / macros:

var data = {
};

// Enhance String a bit:
String.inject({
	endsWith: function(end) {
		return this.length >= end.length && this.substring(this.length - end.length) == end;
	},

	startsWith: function(start) {
		return this.length >= start.length && this.substring(0, start.length) == start;
	},

	isLowerCase: function() {
		return this.toLowerCase() == this;
	},

	isUpperCase: function() {
		return this.toUpperCase() == this;
	}
});


// Enhance some of the javatool classes with usefull methods:

// Class helpers

// We're injecting Type.prototype into ClassDocImpl, to enhance all ClassDocs
// automatically. It's ok to do so since Rhino doesn't allow to override
// native methods, so e.g. qualifiedName won't loop endlessly.
ClassDocImpl.inject(Hash.merge({
	isVisible: function() {
		return ClassObject.get(this.qualifiedName()) != null;
	},

	// This is defined outside renderLink so that even when a Type
	// happens to be its own ClassDoc (as returned by asClassDoc), and therefore
	// overrides renderLink, it can still call the base version.
	renderClassLink: function(param) {
		var str = '';
		if (this.isVisible()) {
			if (this.isAbstract())
				str += '<i>';
			str += renderLink({
				name: this.qualifiedName(),
				anchor: '',
				title: param.title || code_filter(this.name())
			});
			if (this.isAbstract())
				str += '</i>';
		} else {
			str = this.name();
		}
		return str;
	}
}, Type.prototype));

// Parameter helpers

ParameterImpl.inject({
	paramType: function() {
		return new Type(this.type());
	}
});

// Member helpers

MemberDocImpl.inject({
	isCompatible: function(member) {
		if (this instanceof ExecutableMemberDoc &&
			member instanceof ExecutableMemberDoc) {
			// rule 1: static or not
			if (this.isStatic() != member.isStatic())
				return false;
			// rule 2: same return type
			if (this instanceof MethodDoc && member instanceof MethodDoc &&
				this.returnType().qualifiedTypeName() != member.returnType().qualifiedTypeName())
				return false;
			var params1 = this.parameters();
			var params2 = member.parameters();
			// rule 3: if not the same amount of params, the types and names need to be the same:
			var count = Math.min(params1.length, params2.length);
			for (var i = 0; i < count; i++) {
				if (params1[i].name() != params2[i].name()
					|| !params1[i].paramType().isCompatible(params2[i].paramType()))
					return false;
			}
			return true;
		}
		// Fields cannot be grouped
		return false;
	},

	isVisible: function() {
		return Member.get(this) != null;
	},

	renderLink: function(param) {
		var mem = Member.get(this);
		return mem ? mem.renderLink(param) : this.toString();
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
		var cls = cd.classObj;
		if (cls && cd.superclass()) {
			var superclass = cd.superclass().classObj;
			if (superclass) {
				root.removeChild(cls);
				superclass.addChild(cls);
			}
		}
	});
	root.renderHierarchy('');
}

function getRelativeIdentifier(str) {
	return str.startsWith(settings.basePackage + '.') ?
			str.substring(settings.basePackage.length + 1) : str;
}

function renderLink(param) {
	if (settings.hyperref) {
		var str = '<a href="';
		if (param.name) {
			var path = getRelativeIdentifier(param.name).replace('.', '/');
			// Link to the index file for packages
			var name = Type.getSimpleName(param.name);
			if (name.charAt(0).isLowerCase() && name != 'global')
				path += '/index';
			if (settings.templates)
				path = '/reference/' + path.toLowerCase() + '/';
			else
				path = Document.getBasePath() + path + '.html';
			str += path;
		}
		if (param.anchor) {
			str += '#' + param.anchor;
			str += '" onClick="return toggleMember(\'' + param.anchor + '\', true);';
		}
		return str + '">' + param.title + '</a>';
	} else {
	 	return param.title;
	}
}

function encodeJs(str) {
	return str ? (str = uneval(str)).substring(1, str.length - 1) : str;
}

function encodeHtml(str) {
	// Encode everything
	str = Packages.org.htmlparser.util.Translate.encode(str);
	var tags = {
		code: true, br: true, p: true, b: true, a: true, i: true,
		ol: true, li: true, ul: true, tt: true, pre: true };
	// Now replace allowed tags again.
	return str.replace(/&lt;(\/?)(\w*)(.*?)(\s*\/?)&gt;/g, function(match, open, tag, content, close) {
		tag = tag.toLowerCase();
		return tags[tag] ? '<' + open + tag + content + close + '>' : match;
	});
}

/**
 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
 */
function renderTags(param) {
	return renderTemplate('tags', param);
}

function main() {
	ClassObject.put(root);

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
			var path = getRelativeIdentifier(name);
			var text = renderTags({ tags: pkg.inlineTags() });
			var first = renderTags({ tags: pkg.firstSentenceTags() });
			// Remove the first sentence from the main text, and use it as a title
			if (first && text.startsWith(first)) {
				text = text.substring(first.length);
				first = first.substring(0, first.length - 1); // cut away dot
			}

			out.push();
			processClasses(pkg.interfaces());
			processClasses(pkg.allClasses(true));
			processClasses(pkg.exceptions());
			processClasses(pkg.errors());

			renderTemplate('packages#package', {
				content: out.pop(), name: name, path: path, text: text
			}, out);

			if (!settings.templates) {
				// Write package file:
				var index = new Document(path, 'index', 'document');
				renderTemplate('package', { title: first, text: text }, out);
				index.close();
			}
		}
	});
	doc.close();
}

main();
