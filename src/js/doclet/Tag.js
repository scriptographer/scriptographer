/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

// A js Tag class, to define own tag lists and override tag names with special
// render handlers.

Tag = Object.extend(new function() {
	var tags = {};

	// Inject render_macro into native tags. This finds the suitable fake tag
	// based on tag names and passes on the rendering to it.
	[TagImpl, SeeTagImpl].each(function(impl) {
		impl.inject({
			render_macro: function() {
				var name = this.name();
				var tag = tags[name];
				if (tag) {
					// Call method from pseudo tag implementiation on native tag.
					return tag.render.apply(this, arguments);
				} else {
					// Default
					return this.text();
				}
			}
		});
	});

	return {
		initialize: function(str) {
			this.str = str;
		},

		render_macro: function(param) {
			return this.str;
		},

		statics: {
			extend: function(src) {
				return src._names.split(',').each(function(tag) {
					tags[tag] = new this();
				}, this.base(src));
			}
		}
	}
});

LinkTag = Tag.extend({
	_names: '@link,@see',

	render: function(param) {
		var ref = this.referencedMember() || this.referencedClass();
		if (ref) {
			// Link  getter / setters to their beans
			if (ref.synthetic)
				ref = ref.synthetic;
			if (!ref.isVisible())
				error(this.position() + ': warning - ' + this.name() + ' contains reference to invisible object: ' + ref);
			return ref.renderLink({ classDoc: param.classDoc });
		} else {
			error(this.position() + ': warning - ' + this.name() + ' contains undefined reference: ' + this);
		}
	}
});

GroupTag = Tag.extend({
	_names: '@grouptitle,@grouptext',

	render: function(param) {
		data.group[this.name().substring(6)] = this.text();
	}
});

BooleanTag = Tag.extend({
	_names: '@boolean,@true',

	render: function(param) {
		return '<code>true</code> ' + this.text() + ', <code>false</code> otherwise';
	}
});

CodeTag = Tag.extend({
	_names: '@code',

	render: function(param) {
		return '<code>' + this.text() + '</code> ';
	}
});