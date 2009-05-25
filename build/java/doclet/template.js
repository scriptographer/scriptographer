/**
 * JavaScript Template Engine
 * (c) 2005 - 2008, Juerg Lehni, http://www.scratchdisk.com
 *
 * Template.js is released under the MIT license
 * http://dev.helma.org/Wiki/JavaScript+Template+Engine/
 * http://bootstrap-js.net/ 
 */

global = this;

function TemplateWriter() {
	this.buffers = [];
	this.current = [];
}

TemplateWriter.prototype = {
	write: function(what) {
		if (what != null)
			this.current.push(what);
	},

	push: function() {
		this.buffers.push(this.current);
		this.current = [];
	},

	pop: function() {
		var res = this.current.join('');
		this.current = this.buffers.pop();
		return res;
	}
}

function Template(object, name, parent) {
	if (object) {
		if (object instanceof java.io.File) {
			object.getInputStream = function() {
				return new java.io.FileInputStream(this);
			}
			this.resource = object;
			this.resourceName = object.getName();
			this.pathName = object.getPath();
		} else if (typeof object == 'string') {
			this.content = object;
			this.resourceName = name ? name : 'string';
			this.pathName = this.resourceName;
		}
		if (parent) {
			parent.subTemplates[name] = this;
			this.parent = parent;
		}
		this.macroParam = 0;
		this.compile();
	}
}

Template.prototype = {
	render: function(object, param, out) {
		try {
			var parentParam = param && param.__param__;
			if (parentParam) {
				if (parentParam instanceof java.util.Map) {
					var prm = {};
					for (var i in parentParam)
						prm[i] = parentParam[i];
					parentParam = param.__param__ = prm;
				}
				function inherit() {};
				inherit.prototype = parentParam;
				var prm = new inherit();
				for (var i in param)
					prm[i] = param[i];
				param = prm;
			}
			var asString = !out;
			if (asString) (out = new TemplateWriter()).push();
			this.__render__.call(object, param, this, out);
			if (asString) return out.pop();
		} catch (e) {
			if (typeof e != 'string') {
				this.throwError(e);
			} else {
				throw e;
			}
		}
	},

	getSubTemplate: function(name) {
		return this.subTemplates[name];
	},

	renderSubTemplate: function(object, name, param, out) {
		var template = this.subTemplates[name];
		if (!template) throw 'Unknown sub template: ' + name;
		return template.render(object, param, out);
	},

	parse: function(lines) {
		this.tags = []; 
		this.listId = 0; 
		var skipLineBreak = false;
		var skipWhiteSpace = false;
		var tagCounter = 0;
		var templateTag = null;
		var stack = { control: [], loop: {} };
		var buffer = [];
		var code = [ 'this.__render__ = function(param, template, out) {' ];
		var lineBreak = java.lang.System.getProperty('line.separator');
		function append() {
			if (buffer.length) {
				var part = buffer.join('');
				if (part && skipWhiteSpace) {
					part = part.match(/\s*([\u0000-\uffff]*)/);
					if (part)
						part = part[1];
					skipWhiteSpace = false;
				}
				if (part) {
					if (templateTag)
						templateTag.buffer.push(part);
					else 
						code.push('out.write(' + uneval(part) + ');');
				}
				buffer.length = 0;
			}
		}
		try {
			for (var i = 0; i < lines.length; i++) {
				var line = lines[i];
				var start = 0, end = 0;
				while (true) {
					if (tagCounter == 0) {
						start = line.indexOf('<%', end);
						if (start != -1) { 
							if (start > end) 
								buffer.push(line.substring(end, start));
							end = start + 1;
							tagCounter++;
							append();
						} else {
							if (skipLineBreak)
								skipLineBreak = false;
							else
								buffer.push(line.substring(end), lineBreak);
							break;
						}
					} else {
						while (tagCounter != 0) {
							end = line.indexOf('%', end + 1); 
							if (end == -1) break;
							if (line[end - 1] == '<') tagCounter++;
							if (line[end + 1] == '>') tagCounter--;
						}
						if (end != -1) { 
							end += 2; 
							buffer.push(line.substring(start, end));
							var tag = buffer.join('');
							this.tags[code.length] = { lineNumber: i, content: tag };
							if (/^<%\s*[#$][\w_]+\s*[+-]?%>$/.test(tag)) {
								if (templateTag)
									this.parseTemplateTag(templateTag, code);
								templateTag = { tag: tag, buffer: [] };
							} else {
								if (templateTag)
									templateTag.buffer.push(tag);
								else if (tag == '<%-%>')
									skipWhiteSpace = true;
								else if (this.parseMacro(tag, code, stack, true) && end == line.length)
									skipLineBreak = true;
							}
							buffer.length = 0;
						} else {
							buffer.push(line.substring(start), lineBreak);
							break;
						}
					}
				}
			}
			if (tagCounter) { 
				throw 'Tag is not closed';
			} else if (stack.control.length) {
				code.length = stack.control.pop().lineNumber;
				throw 'Control tag is not closed';
			} else {
				append();
				if (templateTag)
					this.parseTemplateTag(templateTag, code);
			}
			for (var i = 0; i < this.renderTemplates.length; i++) {
				var template = this.renderTemplates[i];
				code.splice(1, 0, 'var $' + template.name + ' = template.renderSubTemplate(this, "' +
					template.name + '", param)' + (template.trim ? '.trim()' : ''));
				this.tags.unshift(null);
			}
			code.push('}');
			return code.join(lineBreak);
		} catch (e) {
			this.throwError(e, code.length);
		}
	},

	parseMacroParts: function(tag, code, stack, allowControls) {
		var match = tag.match(/^<%(=?)\s*([\u0000-\uffff]*?)\s*(-?)%>$/);
		if (!match)	return null;
		var isEqualTag = match[1] == '=', content = match[2], swallow = !!match[3];

		var start = 0, pos = 0, end;

		function getPart() {
			if (pos > start) {
				var prev = start;
				start = pos;
				return content.substring(prev, pos);
			}
		}

		function nextPart() {
			while (pos < content.length) {
				var ch = content[pos];
				if (/\s/.test(ch)) {
					var ret = getPart();
					if (ret) return ret;
					var nonWhite = /\S/g;
					nonWhite.lastIndex = pos + 1;
					pos = (end = nonWhite.exec(content)) ? end.index : content.length;
					start = pos;
					continue;
				} else if ((ch == '=' || ch == '|') && content[pos + 1] != ch) {  
					pos++;
					return getPart();
				} else if (/["'([{<]/.test(ch)) { 
					if (ch == '<') {
						if (content[pos + 1] == '%') ch = '<%';
						else ch = null;
					}
					if (ch) {
						var close = ({ '(': ')', '[': ']', '{': '}', '<%': '%>' })[ch], open = null;
						var search = ({ '(': /[()]/g, '[': /[\[\]]/g, '{': /[{}]/g,
								'<%': /<%|%>/g, '"': /"/g, "'": /'/g })[ch];
						if (!close) close = ch;
						else open = ch;
						var count = 1; 
						search.lastIndex = pos + 1;
						while (count && (end = search.exec(content))) {
							if (content[end.index - 1] == '\\') continue;
							if (end == close) count--;
							else if (end == open) count++;
						}
						if (end) pos = end.index + close.length;
						else pos = content.length;
						return getPart();
					}
				} 
				var next = /[\s=|"'([{<]/g;
				next.lastIndex = pos + 1;
				pos = (end = next.exec(content)) ? end.index : content.length;
			}
			if (pos == content.length) {
				var ret = getPart();
				pos++;
				return ret;
			}
		}

		var macros = [], macro = null, isMain = true;

		function nextMacro(next) {
			if (macro) {
				if (!macro.command)
					throw 'Syntax error';
				macro.opcode = macro.opcode.join(' ');
				if (macro.isControl) {
					if (macro.opcode[0] == '(') macro.opcode = macro.opcode.substring(1, macro.opcode.length - 1);
				} else {
					var unnamed = macro.unnamed.join(', ');
					macro.arguments = '[ { ' + macro.param.join(', ') + ' } ' + (unnamed ? ', ' + unnamed : '') + ' ]';
					var match = macro.command.match(/^(.*)\.(.*)$/);
					if (match) {
						macro.object = match[1];
						macro.name = match[2];
					} else { 
						macro.object = 'global';
						macro.name = macro.command;
					}
				}
				macros.push(macro);
				isMain = false;
			}
			if (next) {
				macro = {
					command: next, opcode: [], param: [], unnamed: [],
					values: { prefix: null, suffix: null, 'default': null, encoding: null, separator: null, 'if': null }
				};
				if (isMain) {
					macro.isControl = allowControls && /^(foreach|if|elseif|else|end)$/.test(next);
					macro.isData = isEqualTag;
					macro.isSetter = next[0] == '$'; 
				}
			}
		}

		function nestedMacro(that, value, code, stack) {
			if (/<%/.test(value)) {
				var nested = value;
				value = 'param_' + (that.macroParam++);
				if (/^<%/.test(nested)) {
					code.push('var ' + value + ' = ' + that.parseMacro(nested, code, stack, false, true) + ';');
				} else if (/^['"]/.test(nested)) {
					eval('nested = ' + nested);
					new Template(nested, value, that);
					code.push('var ' + value + ' = template.renderSubTemplate(this, "' + value + '", param);');
				} else {
					throw 'Syntax error: ' + nested;
				}
			}
			return value;
		}

		var part, isFirst = true, append;
		while (part = nextPart()) {
			if (isFirst) {
				nextMacro(part); 
				isFirst = false;
				append = true;
			} else if (/\w=$/.test(part)) { 
				var key = part.substring(0, part.length - 1), value = nextPart();
				value = nestedMacro(this, value, code, stack);
				macro.param.push('"' + key + '": ' + value);
				if (macro.values[key] !== undefined)
					macro.values[key] = value;
				append = false;
			} else if (part == '|') { 
				isFirst = true;
			} else { 
				if (macro.isSetter && !macro.opcode.length && part[0] != '=')
					macro.isSetter = false;
				if (!macro.isData && !macro.isControl && !macro.isSetter) {
					part = nestedMacro(this, part, code, stack);
					macro.unnamed.push(part);
					append = false;
				} else if (append) { 
					if (macro.isSetter) 
						part = nestedMacro(this, part, code, stack);
					macro.opcode.push(part);
				} else {
					throw "Syntax error: '" + part + "'";
				}
			}
		}
		nextMacro();

		macro = macros.shift();
		for (var i = 0; i < macros.length; i++) {
			var m = macros[i];
			macros[i] = '{ command: "' + m.command + '", name: "' + m.name +
				'", object: ' + m.object + ', arguments: ' + m.arguments + ' }';
		}
		var values = macro.values, encoding = values.encoding;
		values.filters = macros.length > 0 ? '[ ' + macros.join(', ') + ' ]' : null;
		if (encoding) {
			values.encoder = 'encode' + encoding.substring(1, encoding.length - 1).capitalize();
			var def = values['default'];
			if (def)
				values['default'] = /^'"/.test(def) ? '"' + global[values.encoder](def.substring(1, def.length - 1)) + '"'
					: values.encoder + '(' + def + ')';
		}
		macro.swallow = swallow || macro.isControl;
		macro.tag = tag;
		return macro;
	},

	parseMacro: function(tag, code, stack, allowControls, toString) {
		if (/^<%--/.test(tag))
			return true;
		var macro = this.parseMacroParts(tag, code, stack, allowControls);
		if (!macro)
			throw 'Invalid tag';
		var values = macro.values, result;
		var condition = values['if'];
		if (condition)
			code.push(								'if (' + condition + ') {');
		var postProcess = !!(values.prefix || values.suffix || values.filters);
		var codeIndexBefore = code.length;
		if (macro.isData) { 
			result = this.parseLoopVariables(macro.opcode
				? macro.command + ' ' + macro.opcode : macro.command, stack);
		} else if (macro.isControl) {
			var open = false, close = false;
			var prevControl = stack.control[stack.control.length - 1];
			if (/^else/.test(macro.command) && (!prevControl || !/if$/.test(prevControl.macro.command))) {
				throw "Syntax error: 'else' requiers 'if' or 'elseif'";
			} else {
				switch (macro.command) {
				case 'foreach':
					var match = macro.opcode.match(/^\s*(\$[\w_]+)\s*in\s*(.+)$/);
					if (!match) throw 'Syntax error';
					open = true;
					var variable = match[1], value = match[2];
					postProcess = postProcess || !!values.separator;
					var suffix = '_' + (this.listId++);
					var list = 'list' + suffix, length = 'length' + suffix;
					var index = 'i' + suffix, first = 'first' + suffix;
					var loopStack = stack.loop[variable] = stack.loop[variable] || [];
					loopStack.push({ list: list, index: index, length: length, first: first });
					macro.variable = variable;
					code.push(						'var ' + list + ' = ' + value + '; ',
													'if (' + list + ') {',
													'	if (' + list + '.length == undefined) ' + list + ' = template.toList(' + list + ');',
													'	var ' + length + ' = ' + list + '.length' + (values.separator ? ', ' + first + ' = true' : '') + ';',
													'	for (var ' + index + ' = 0; ' + index + ' < ' + length + '; ' + index + '++) {',
													'		var ' + variable + ' = ' + list + '[' + index + '];',
						values.separator		?	'		out.push();' : null);
					break;
				case 'end':
					if (macro.opcode) throw 'Syntax error';
					if (!prevControl || !/^else|^if$|^foreach$/.test(prevControl.macro.command))
						throw "Syntax error: 'end' requiers 'if', 'else', 'elseif' or 'foreach'";
					close = true;
					if (prevControl.macro.command == 'foreach') {
						var loop = stack.loop[prevControl.macro.variable].pop();
						var separator = prevControl.postProcess && prevControl.postProcess.separator;
						if (separator)
							code.push(				'		var val = out.pop();',
													'		if (val != null && val !== "") {',
													'			if (' + loop.first + ') ' + loop.first + ' = false;',
													'			else out.write(' + separator + ');',
													'			out.write(val);',
													'		}');
						code.push(					'	}');
					}
					code.push(						'}');
					break;
				case 'elseif':
					close = true;
				case 'if':
					if (!macro.opcode) throw 'Syntax error';
					open = true;
					code.push(						(close ? '} else if (' : 'if (') + this.parseLoopVariables(macro.opcode, stack) + ') {');
					break;
				case 'else':
					if (macro.opcode) throw 'Syntax error';
					close = true;
					open = true;
					code.push(						'} else {');
					break;
				}
				if (close) {
					var control = stack.control.pop();
					if (control.postProcess) {
						values = control.postProcess;
						postProcess = true;
						result = 'out.pop()';
					}
				}
				if (open) {
					stack.control.push({ macro: macro, lineNumber: codeIndexBefore,
					 		postProcess: postProcess ? values : null });
					if (postProcess)
						code.splice(codeIndexBefore, 0,	'out.push();');
				}
			}
		} else { 
			if (macro.opcode) {
				if (macro.isSetter)
					code.push(						'var ' + macro.command + ' ' + this.parseLoopVariables(macro.opcode, stack) + ';');
				else
					throw 'Syntax error'; 
			} else {
				var object = macro.object;
				postProcess = postProcess | macro.swallow;
				code.push(		postProcess		?	'out.push();' : null,
													'var val = template.renderMacro("' + macro.command + '", ' + object + ', "' +
															macro.name + '", param, ' + this.parseLoopVariables(macro.arguments, stack) + ', out);',
								macro.swallow	?	'if (val) val = val.toString().trim()' : null,
								postProcess		?	'template.write(out.pop()' + (macro.swallow ? '.trim()' : '') + ', ' + values.filters + ', ' + values.prefix + ', ' +
															values.suffix + ', null, out);' : null);
				result = 'val';
			}
		}
		if (result) { 
			result = result.match(/^(.*?);?$/)[1];
			if (values.encoder)
				result = values.encoder + '(' + result + ')';
			if (postProcess)
				code.push(							'template.write(' + result + ', ' + values.filters + ', ' + values.prefix + ', ' +
															values.suffix + ', ' + values['default']  + ', out);');
			else {
				if (toString) {
					return result;
				} else {
					if (/[.()\s]/.test(result)) {
						code.push(					'var val = ' + result + ';');
						result = 'val';
					}
					code.push(						'if (' + result + ' != null && ' + result + ' !== "")',
													'	out.write(' + result + ');');
					if (values['default'])
						code.push(					'else',
													'	out.write(' + values['default'] + ');');
				}
			}
		}
		if (toString && postProcess) {
			code.splice(codeIndexBefore, 0,			'out.push();');
			return 'out.pop()';
		}
		if (condition)
			code.push(								'}');
		if (!toString)
			return macro.swallow;
	},

	parseLoopVariables: function(str, stack) {
		return str.replace(/(\$[\w_]+)\#(\w+)/g, function(part, variable, suffix) {
			var loopStack = stack.loop[variable], loop = loopStack && loopStack[loopStack.length - 1];
			if (loop) {
				switch (suffix) {
				case 'index': return loop.index;
				case 'length': return loop.length;
				case 'first': return '(' + loop.index + ' == 0)';
				case 'last': return '(' + loop.index + ' == ' + loop.length + ' - 1)';
				case 'even': return '((' + loop.index + ' & 1) == 0)';
				case 'odd': return '((' + loop.index + ' & 1) == 1)';
				}
			}
			return part;
		});
	},

	parseTemplateTag: function(tag, code) {
		var match = tag.tag.match(/^<%\s*([$#])(\S*)\s*([+-]?)%>$/);
		if (match) {
			var name = match[2], content = tag.buffer.join(''), end = match[3];
			if (!end) content = content.match(/^\s*[\n\r]?([\u0000-\uffff]*)[\n\r]?\s*$/)[1];
			else if (end == '-') content = content.trim();
			new Template(content, name, this);
			if (match[1] == '$')
				this.renderTemplates.push({ name: name, trim: end == '-' });
		} else
			throw 'Syntax error in template';
	},

	write: function(value, filters, prefix, suffix, deflt, out) {
		if (value != null && value !== '') {
			if (filters) {
				for (var i = 0; i < filters.length; i++) {
					var filter = filters[i];
					var func = filter.object && filter.object[filter.name + '_filter'];
					if (func) {
						if (func.apply) 
							value = func.apply(filter.object, [value].concat(filter.arguments));
						else if (func.exec) 
							value = func.exec(value)[0];
					} else {
						out.write('[Filter unhandled: "' + filter.command + '"]');
					}
				}
			}
			if (prefix) out.write(prefix);
			out.write(value);
			if (suffix) out.write(suffix);
		} else if (deflt) {
			out.write(deflt);
		}
	},

	renderMacro: function(command, object, name, param, args, out) {
		var unhandled = false, value;
		if (object) {
			var macro = object[name + '_macro'];
			if (macro) {
				try {
					var prm = args[0];
					if (prm.param)
						for (var i in prm.param)
							if (prm[i] === undefined)
								prm[i] = prm.param[i];
					prm.__template__ = this.parent || this;
					prm.__param__ = param;
					prm.__out__ = out;
					value = macro.apply(object, args);
				} catch (e) {
					var tag = this.getTagFromException(e);
					var message = e.message || e;
					if (tag && tag.content) {
						message += ' (' + e.fileName + '; line ' + tag.lineNumber + ': ' +
							tag.content + ')';
					} else if (e.fileName) {
						message += ' (' + e.fileName + '; line ' + e.lineNumber + ')';
					}
					out.write('[Macro error in ' + command + ': ' + message + ']');
				}
			} else {
				value = object[name];
				if (value === undefined)
					unhandled = true;
			}
		} else {
			unhandled = true;
		}
		if (unhandled)
			out.write('[Macro unhandled: "' + command + '"]');
		return value;
	},

	toList: function(obj) {
		var ret = [];
		if (obj.each)
			obj.each(function(v) { ret.push(v); });
		else
			for (var i in obj) { ret.push(obj[i]); }
		return ret;
	},

	compile: function() {
		try {
			var lines;
			if  (this.resource) {
				var reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(this.resource.getInputStream()));
				lines = [];
				var line;
				while ((line = reader.readLine()) != null)
					lines.push(line);
				reader.close();
				this.lastModified = this.resource.lastModified();
			} else if (this.content) {
				lines = this.content.split(/\r\n|\n|\r/mg);
			} else {
				lines = [];
			}
			this.subTemplates = {};
			this.renderTemplates = [];
			var code = this.parse(lines);
			var cx = Packages.org.mozilla.javascript.Context.getCurrentContext();
			var level = cx.getOptimizationLevel();
			cx.setOptimizationLevel(-1);
			cx.evaluateString(this, code, this.pathName, 0, null);
			cx.setOptimizationLevel(level);
		} catch (e) {
			this.throwError(e);
		}
	},

	throwError: function(error, line) {
		var tag = line ? this.getTagFromCodeLine(line) : this.getTagFromException(error);
		var message = 'Template error in ' + this.pathName;
		if (typeof error == 'string' && error.indexOf(message) == 0)
			throw error;
		if (tag) {
		 	message += ', line: ' + (tag.lineNumber + 1) + ', in ' +
				tag.content;
		}
		if (error) {
			var details = null;
			if (error.fileName && error.fileName != this.pathName) {
				details = 'Error in ' + error.fileName + ', line ' +
					error.lineNumber + ': ' + error;
			} else {
				details = error;
			}
			if (error.javaException) {
				var sw = new java.io.StringWriter();
				error.javaException.printStackTrace(new java.io.PrintWriter(sw));
				details += '\nStacktrace:\n' + sw.toString();
			}
			if (details)
				message += ': ' + details;
		}
		throw message;
	},

	getTagFromCodeLine: function(number) {
		while (number >= 0) {
			var tag = this.tags[number--];
			if (tag) return tag;
		}
	},

	getTagFromException: function(e) {
		if (this.tags && e.lineNumber && e.fileName == this.pathName)
			return this.getTagFromCodeLine(e.lineNumber);
	}
}

Template.methods = new function() {
	var templates = {};

	return {
		getTemplate: function(template) {
			var name = template;
			if (!(template instanceof Template)) {
				var pos = name.indexOf('#');
				if (pos != -1) {
					template = this.getTemplate(name.substring(0, pos));
					if (template)
						return template.getSubTemplate(name.substring(pos + 1));
				}
				template = templates[name];
			}
			if (!template)
				template = templates[name] = new Template(
					new java.io.File(baseDir + '/templates/' + name + '.jstl'));
			return template;
		},

		renderTemplate: function(template, param, out) {
			try {
				template = this.getTemplate(template);
				if (template)
					return template.render(this, param, out);
			} catch (e) {
				error(e);
			}
		},

		template_macro: function(param, name) {
			if (name[0] == '#') {
				return param.__template__.renderSubTemplate(this, name.substring(1), param);
			} else {
				return this.renderTemplate(name, param);
			}
		}
	};
};

