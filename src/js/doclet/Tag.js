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
					return name && name[0] == '@' ? name.substring(1) + ' ' + this.text() : this.text();
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
		return '<tt>true</tt> ' + this.text() + ', <tt>false</tt> otherwise';
	}
});

CodeTag = Tag.extend({
	_names: '@code',

	render: function(param) {
		return '<tt>' + this.text() + '</tt> ';
	}
});

DefaultTag = Tag.extend({
	_names: '@default',

	render: function(param) {
		var value = this.text();
		if (/^([^a-z]|true|false|null|undefined)/.test(value))
			value = '<tt>' + value + '</tt>';
		data.defaultValue = 'optional, default: ' + value;
	}
});