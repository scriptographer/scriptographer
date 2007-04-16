/**
 * JavaScript Template Engine for Helma
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * http://dev.helma.org/Wiki/JavaScript+Template+Engine/
 * 
 * Revisions:
 * 
 * 0.22  More clean-ups in macro parsing code, distinction between control tags
 *       and macro tags is now only on the handling level, not parsing level.
 *       This adds feature like prefix / suffix to foreach and if / elseif, and
 *       resulted in cleaner code. 
 *       Additionally, the separator parameter can now be set in foreach.
 *
 * 0.21  Major rewrite and refactoring of most parts, adaption of Hannes' latest
 *       additions to Helma skins (sub templates, nested macros, filters),
 *       varios design changes (e.g. only allow to define and set $-variables for
 *       inside templates).
 *
 * 0.20  Refactored the code rendering code to be more readibly and smaller.
 *       Removed Helma dependency, by using preprocessing macros.
 *
 * 0.19  Added support for sub templates and a special macro
 *       for both rendering sub templates and external templates.
 * 
 * 0.18  Added support for encoding parameter, fixed a bug in <% else %>
 * 
 * 0.17  Many clean-ups and simplifications of regular expressions and parsing.
 *       At parsing time, no code is evaluated any longer, except the final
 *       result. This leads to further speed improvements by about 1.5.
 * 
 * 0.16  Fixed issues with the display of errors. The correct line numbers
 *       should be reported now. If a macro call results in an exception
 *       The exception is caught and repported just like in Helma skins now.
 * 
 * 0.15  Added the possibility for macro tags to swallow the following line
 *       separator, if there is any, by adding a minus at the end: <% macro -%>.
 *       Control tags like if, else, elseif, end, foreach, set and comments 
 *       (<%-- --%>) automatically swallow the following line seperator.
 *
 * 0.14  Added support for res.handlers.
 *       Switched to java.util.regex for the tag parser.
 *       Fixed a bug with escaped quotes in macro parameters.
 *       Reformated template generating code to be more readable.
 * 
 * 0.13  Added support for properties in <% %>-tags (not only macros)
 *       and fixed an incompatibility with the Rhino Debugger and jstl templates.
 * 
 * 0.12  Fixed various bugs that were introduced in the 0.11 rewrite.
 * 
 * 0.11  Replaced all the dirty hacks for keeping track of template line numbers 
 *       and linking them to code line numbers by a clean implementation of the
 *       same functionality.
 *       The result is a faster and less resource hungry parser.
 *       Cleaned up the code, seperated tag parser form line parser.
 * 
 * 0.10  More speed improvements, parsing is now around 6-7 times faster
 *       than in 0.8
 * 
 * 0.9   Various speed improvements, leading to an overall decrease of parsing
 *       time by more than factor 3.
 * 
 * 0.8   Added support for HopObject collections in foreach
 *       Removed regexp filter for if() expressions that sometimes seemed
 *       to deadlock
 *       Fixed a bug with finding the right template in the inheritance chain
 *       if it was overriden by another one.
 * 
 * 0.7   Fixed problems with error reports. Full stacktraces are now printed,
 *       and errors caused in macros called from the template are now properly
 *       detected and reported.
 *       Switched to using Context.evaluateString() instead of eval(),
 *       for improve of speed and reception of proper exceptions.
 * 
 * 0.6   fixed a bug that caused macros in  objects other than 'this' and
 *       'root' to fail.
 * 
 * 0.5   first public release.
 */

// Retrieve a reference to the global scope.
global = (function() { return this })();

// Define TemplateWriter that copies Helma's ResponseTrans
function TemplateWriter() {
	this.buffers = [];
	this.current = [];
}

TemplateWriter.prototype = {
	write: function(what) {
		if (what != null)
			this.current.push(what);
	},

	writeln: function(what) {
		if (what != null)
			this.current.push(what);
		this.current.push('\n');
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

/**
 * Constructor
 *
 * @object a file to read the template from, or an object to retrieve a
 * template with the given name from
 * @name the name of the resource in the object
 */
function Template(object, name) {
	if (object) {
		if (object instanceof java.io.File) {
			// Add getInputStream to java.io.File object.
			object.getInputStream = function() {
				return new java.io.FileInputStream(this);
			}
			this.resource = object;
			this.resourceName = object.getName();
			this.pathName = object.getPath();
		} else if (typeof object == "string") {
			this.content = object;
			this.resourceName = name ? name : "string";
			this.pathName = this.resourceName;
		}
		this.compile();
	}
}

Template.prototype = {
	render: function(object, param, out) {
		try {
			// Inherit from param.__param__ if it is set:
			if (param && param.__param__) {
				function inherit() {};
				inherit.prototype = param.__param__;
				var prm = new inherit();
				// and copy over from param:
				for (var i in param)
					prm[i] = param[i];
				param = prm;
			}
			// If out is null, render to a string and return it
			var asString = !out;
			if (asString) (out = new TemplateWriter()).push();
			this.__render__.call(object, param, this, out);
			if (asString) return out.pop();
		} catch (e) {
			// In case the exception happened in a finished template,
			// output the error for the template
			if (typeof e != "string") {
				this.throwError(e);
			} else {
				// Just throw it, for debugging of renderTemplate
				throw e;
			}
		}
	},

	/**
	 * Returns the sub template, if it exists. The name is specified without
	 * the trailing #
	 */
	getSubTemplate: function(name) {
		return this.subTemplates[name];
	},

	/**
	 * Renders the sub template on object. The name is specified without
	 * the trailing #
	 */
	renderSubTemplate: function(object, name, param, out) {
		var template = this.subTemplates[name];
		if (!template) throw "Unknown sub template: " + name;
		return template.render(object, param, out);
	},

	/**
	 * Parses the passed template lines and returns the JS code
	 * for the render function
	 */
	parse: function(lines) {
		this.tags = []; // Keep track of tags at any given code line (= array index)
		this.listId = 0; // An id for generated list names
		// Walk through the lines and keep track of tags and the text between them
		// this supports multiline tags, such as <% ...>...\n...</%>
		// the finding of closing tags counts nested tags, to make sub templates
		// work
		var buffer = []; // line buffer
		var lineBreak = java.lang.System.getProperty("line.separator");
		var skipLineBreak = false;
		var tagCounter = 0;
		var templateTag = null;
		// Container for the generated code lines.
		var code = [ "this.__render__ = function(param, template, out) {" ];
		// Stack for control tags and loops
		var stack = { control: [], loop: {} };
		var last = null;
		try {
			function append() {
				if (buffer.length > 0) {
					// Write out text lines
					var part = buffer.join('');
					if (templateTag)
						templateTag.buffer.push(part);
					else
						code.push('out.write("' + this.encodeJs(part) + '");');
					buffer.length = 0;
				}
			}
			for (var i = 0; i < lines.length; i++) {
				var line = lines[i];
				var start = 0, end = 0;
				while (true) {
					// Search start and end of macro tag, keep lines:
					if (tagCounter == 0) {
						start = line.indexOf("<%", end);
						if (start != -1) { // Found the begining of a macro
							if (start > end) // There was some text before it
								buffer.push(line.substring(end, start));
							// Skip <, the % is skiped bellow in line.indexOf("%", end + 1);
							end = start + 1;
							tagCounter++;
							append();
							// Now buffer collects tag lines
						} else {
							if (skipLineBreak)
								skipLineBreak = false;
							else
								buffer.push(line.substring(end), lineBreak);
							break;
						}
					} else {
						while (tagCounter != 0) {
							end = line.indexOf("%", end + 1); // skip %
							if (end == -1) break;
							if (line[end - 1] == '<') tagCounter++;
							if (line[end + 1] == '>') tagCounter--;
						}
						if (end != -1) { // found the end of the macro
							end += 2; // include tag end as well
							buffer.push(line.substring(start, end));
							// parse it:
							var tag = buffer.join('');
							// Keep track of line numbers. this.tags links code line numbers
							// to template line numbers
							this.tags[code.length] = { lineNumber: i, content: tag };
							// If this is a template tag, change the state to
							// finding the end of the template. Do a thourough check
							// as this tag might also be the setting of a variable
							// or the call of a mcro on a scope variable.
							if (/^<%\s*[#$][\w_]+\s*[+-]?%>$/.test(tag)) {
								if (templateTag)
									this.parseTemplateTag(templateTag, code);
								templateTag = { tag: tag, buffer: [] };
							} else {
								if (templateTag)
									templateTag.buffer.push(tag);
								else if (this.parseMacro(tag, code, stack, true) && end == line.length)
									skipLineBreak = true;
							}
							// Now buffer collects lines between tags
							buffer.length = 0;
						} else {
							buffer.push(line.substring(start), lineBreak);
							break;
						}
					}
				}
			}
			if (tagCounter) { // Report the tag that was left open
				throw "Tag is not closed";
			} else if (stack.control.length) {
				// Resize code back to the error, so throwError() picks
				// the right line in the catch block bellow.
				code.length = stack.control.pop().lineNumber;
				throw "Control tag is not closed";
			} else {
				// Write out the rest
				append();
				if (templateTag)
					this.parseTemplateTag(templateTag, code);
			}
			// Render the sub templates that were defined with $ into variables.
			for (var i = 0; i < this.renderTemplates.length; i++) {
				var template = this.renderTemplates[i];
				// Trim at render-time, if required:
				code.splice(1, 0, "var $" + template.name + " = template.renderSubTemplate(this, '" +
					template.name + "', param)" + (template.trim ? ".trim()" : ""));
				// Shift tags as well, so line numbers are still right
				this.tags.unshift(null);
			}
			code.push('}');
			return code.join(lineBreak);
		} catch (e) {
			this.throwError(e, code.length);
		}
	},

	/**
	 * Parses the different parts of the macro and returns a datastructure
	 * that then can be used to produce the macro code.
	 */
	parseMacroParts: function(tag, code, stack, allowControls) {
		var match = tag.match(/^<%(=?)\s*(.*?)\s*(-?)%>$/);
		if (!match)	return null;
		// iI the tag ends with -%>, the line break after it should be swallowed,
		// if there is any. By default all control macros swallow line breaks.
		var isEqualTag = match[1] == '=', content = match[2], swallow = match[3];

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
					// find end of white space using regexp
					var nonWhite = /\S/g;
					nonWhite.lastIndex = pos + 1;
					pos = (end = nonWhite.exec(content)) ? end.index : content.length;
					start = pos;
					continue;
				} else if (ch == '=' || (ch == '|' && content[pos + 1] != '|')) { // Named parameter / filter
					// The check above discovers || as a logical parameter and does not 
					// count the first | as a single item.
					// Named parameters and start of filters can be handled the same
					// as the sign itself is included, and nothing else needs to be done
					// = is included as a clue that this is going to be a named param.
					pos++;
					return getPart();
				} else if (/["'([{<]/.test(ch)) { // Groups: "" '' () [] {} <%%>
					if (ch == '<') {
						// cheat a little bit to also include <% %>, which is more
						// than one char
						if (content[pos + 1] == '%') ch = '<%';
						else ch = null;
					}
					if (ch) {
						// find the end, using regexps. 
						var close = ({ '(': ')', '[': ']', '{': '}', '<%': '%>' })[ch], open = null;
						var search = ({ '(': /[()]/g, '[': /[\[\]]/g, '{': /[{}]/g,
								'<%': /<%|%>/g, '"': /"/g, "'": /'/g })[ch];
						// " and ' cannot be nested:
						if (!close) close = ch;
						else open = ch;
						var count = 1; // count the opening char already
						search.lastIndex = pos + 1;
						while (count && (end = search.exec(content))) {
							// skip escaped chars:
							if (content[end.index - 1] == '\\') continue;
							if (end == close) count--;
							else if (end == open) count++;
						}
						if (end) pos = end.index + close.length;
						else pos = content.length;
						return getPart();
					}
				} 
				// skip to the the next interesting position:
				var next = /[\s=|"'([{<]/g;
				next.lastIndex = pos + 1;
				pos = (end = next.exec(content)) ? end.index : content.length;
			}
			// the last bit
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
					throw "Syntax error";
				macro.opcode = macro.opcode.join(' ');
				if (macro.isControl) {
					// Strip away ()
					if (macro.opcode[0] == '(') macro.opcode = macro.opcode.substring(1, macro.opcode.length - 1);
				} else {
					// Finish previous macro and push it onto list
					// convert param and unnamed to a arguments array that can directly be used
					// when calling the macro. param comes first, unnamed after.
					macro.arguments = '[ { ' + macro.param.join(', ') + ' }, ' + macro.unnamed.join(', ') + ' ]';
					// Split object and property / macro name
					var match = macro.command.match(/^([^.]*)\.(.*)$/);
					if (match) {
						macro.object = match[1];
						macro.name = match[2];
					} else { // If no object name is given, we're in global
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
					// Values needed on code rendering time
					values: { prefix: null, suffix: null, 'default': null, encoding: null, separator: null }
				};
				// Control and data macros are only allowed for first macro in chain (main)
				if (isMain) {
					// Is this a control macro?
					macro.isControl = allowControls && /^(foreach|if|elseif|else|end)$/.test(next);
					// Is this a data macro?
					macro.isData = isEqualTag;
				}
			}
		}

		// Now do the main parsing of the parts
		var part, isFirst = true, append;
		var macroParam = 0;
		while (part = nextPart()) {
			if (isFirst) {
				nextMacro(part); // add new macro
				isFirst = false;
				// Appending is allowed as long as no named or unnamed parameter
				// is specified.
				append = true;
			} else if (/.=$/.test(part)) { // named param
				// TODO: Calling nextPart here should only return values, nothing else!
				// add error handling...
				var key = part.substring(0, part.length - 1), value = nextPart();
				if (/^<%/.test(value)) {
					// A nested macro: render it, then set the result to a variable
					var nested = value;
					value = "param_" + (macroParam++) + "";
					code.push("var " + value + " = " + this.parseMacro(nested, code, stack, false, true) + ";");
				}
				macro.param.push('"' + key + '": ' + value);
				// Override defaults only:
				if (macro.values[key] !== undefined)
					macro.values[key] = value;
				// Appending to macro command not allowed after first parameter
				append = false;
			} else if (part == '|' && !macro.isControl) { // start a filter
				isFirst = true;
			} else { // unnamed param
				// Unnamed parameters are not allowed in <%= tags
				// allowed groups for unnamed params: '' "" [] {}
				if (!macro.isData && !macro.isControl && (/^['"[{]/.test(part))) {
					macro.unnamed.push(part);
					// Appending to macro opcode not allowed after first parameter
					append = false;
				} else if (append) {
					macro.opcode.push(part);
				} else {
					throw "Syntax error: '" + part + "'";
				}
			}
		}
		// Add last macro
		nextMacro();

		// Retrieve first macro
		macro = macros.shift();
		// Convert other macros to filter strings:
		for (var i = 0; i < macros.length; i++) {
			var m = macros[i];
			macros[i] = '{ command: "' + m.command + '", name: "' + m.name + '", object: ' + m.object + ', arguments: ' + m.arguments + ' }';
		}
		macro.filters = macros.length > 0 ? '[ ' + macros.join(', ') + ' ]' : null;
		var values = macro.values, encoding = values.encoding;
		if (encoding) {
			// Convert encoding to encoder function:
			values.encoder = 'encode' + encoding.substring(1, encoding.length - 1).capitalize();
			// If default is set, encode it now:
			if (values['default'])
				values['default'] = global[values.encoder](values['default']);
		}
		// All control macros swallow line breaks:
		macro.swallow = swallow || macro.isControl;
		macro.tag = tag;
		return macro;
	},

	/**
	 * Parses the tag and reports possible syntax errors.
	 * This is the core of the template parser
	 */
	parseMacro: function(tag, code, stack, allowControls, toString) {
		// only process if it is not a comment or a swallow line break tag.
		// return true tells parse() to swallow line break.
		if (/^<%--/.test(tag) || tag == '<%-%>') return true;
		// <%= tags cannot have unnamed parameters
		var macro = this.parseMacroParts(tag, code, stack, allowControls);
		if (!macro)
			throw "Invalid tag";
		var values = macro.values, result;
		var postProcess = values.prefix || values.suffix || macro.filters;
		var codeIndexBefore = code.length;
		if (macro.isData) { // param, response, request, session, or a <%= %> tag
			result = this.parseLoopVariables(macro.command + " " + macro.opcode, stack);
		} else if (macro.isControl) {
			var open = false, close = false;
			var prevControl = stack.control[stack.control.length - 1];
			// Only allow else with and if beforehand
			if (/^else/.test(macro.command) && (!prevControl || !/if$/.test(prevControl.macro.command))) {
				throw "Syntax error: 'else' requiers 'if' or 'elseif'";
			} else {
				switch (macro.command) {
				case "foreach":
					var match = macro.opcode.match(/^\s*(\$[\w_]+)\s*in\s*(.+)$/);
					if (!match) throw "Syntax error";
					open = true;
					var variable = match[1], value = match[2];
					// separator means post processing too:
					postProcess = postProcess || values.separator;
					var suffix = '_' + (this.listId++);
					var list = "list" + suffix, length = "length" + suffix;
					var index = "i" + suffix, first = "first" + suffix;
					// Use stacks per variable name, in case two loops use the same variable name
					var loopStack = stack.loop[variable] = stack.loop[variable] || [];
					loopStack.push({ list: list, index: index, length: length, first: first });
					// Store variable in macro, so it can be retrieved from
					// the control stack in "end".
					macro.variable = variable;
					code.push(						"var " + list + " = " + value + "; ",
													"if (" + list + ") {",
													"	var " + length + " = " + list + ".length" + (values.separator ? ", " + first + " = true" : "") + ";",
													"	for (var " + index + " = 0; " + index + " < " + length + "; " + index + "++) {",
													"		var " + variable + " = " + list + "[" + index + "];",
						values.separator		?	"		out.push();" : null);
					break;
				case "end":
					if (macro.opcode) throw "Syntax error";
					if (!prevControl || !/^else|^if$|^foreach$/.test(prevControl.macro.command))
						throw "Syntax error: 'end' requiers 'if', 'else', 'elseif' or 'foreach'";
					close = true;
					if (prevControl.macro.command == 'foreach') {
						// Pop the current loop from the stack.
						var loop = stack.loop[prevControl.macro.variable].pop();
						// If the loop defines a separator, process it now.
						// The first part of this (out.push()) happen in "foreach"
						var separator = prevControl.postProcess && prevControl.postProcess.separator;
						if (separator)
							code.push(				"		var val = out.pop();",
													"		if (val != null && val !== '') {",
													"			if (" + loop.first + ") " + loop.first + " = false;",
													"			else out.write(" + separator + ");",
													"			out.write(val);",
													"		}");
						code.push(						"}");
					}
					code.push(						"}");
					break;
				case "elseif":
					close = true;
					// More in "if" (no break here)
				case "if":
					if (!macro.opcode) throw "Syntax error";
					open = true;
					code.push(						(close ? "} else if (" : "if (") + this.parseLoopVariables(macro.opcode, stack) + ") {");
					break;
				case "else":
					if (macro.opcode) throw "Syntax error";
					close = true;
					open = true;
					code.push(						"} else {");
					break;
				}
				if (close) {
					// A closing control structure. See if the previous one
					// defined post processing, and if so, execute it now.
					var control = stack.control.pop();
					if (control.postProcess) {
						values = control.postProcess;
						postProcess = true;
						result = "out.pop()";
					}
				}
				if (open) {
					// An opening control structure. Push it onto the stack:
					stack.control.push({ macro: macro, lineNumber: codeIndexBefore,
					 		postProcess: postProcess ? values : null });
					// Add out.push() before the block if it needs postProcessing
					if (postProcess)
						code.splice(codeIndexBefore, 0,	"out.push();");
				}
			}
		} else { // A normal <% %> macro
			if (macro.opcode) {
				// Setting a value? If not, this is a syntax error.
				if (macro.command[0] == '$' && macro.opcode[0] == '=')
					code.push(						"var " + macro.command + " " + this.parseLoopVariables(macro.opcode, stack) + ";");
				else
					throw "Syntax error"; // No opcodes allowed in macros
			} else {
				var object = macro.object;
				// Macros can both write to res and return a value. prefix / suffix / filter applies to both,
				// encoding / default only to the value returned.
				// TODO: Compare with Helma, to see if that is really true. E.g. what happens when default is set
				// and the macro returns no value, but does write to res?
				code.push(		postProcess		?	"out.push();" : null,
													"var val = template.renderMacro('" + macro.command + "', " + object + ", '" +
															macro.name + "', param, " + macro.arguments + ", out);",
								postProcess		?	"template.write(out.pop(), " + macro.filters + ", " + values.prefix + ", " +
															values.suffix + ", null, out);" : null);
				result = "val";
			}
		}
		if (result) { // Write the value out
			// Strip away possible ; at the end:
			result = result.match(/^(.*?);?$/)[1];
			if (values.encoder)
				result = values.encoder + "(" + result + ")";
			// Optimizations: only call template.write if necessary:
			if (postProcess)
				code.push(							"template.write(" + result + ", " + macro.filters + ", " + values.prefix + ", " +
															values.suffix + ", " + values['default']  + ", out);");
			else {
				if (toString) {
					// This is needed for nested macros. Whenn there is no post processing,
					// we can actually simply return the value
					return result;
				} else {
					// Dereference to local variable if it's a call, a lookup, or a more complex construct (containg whitespaces)
					if (/[.()\s]/.test(result)) {
						code.push(					"var val = " + result + ";");
						result = "val";
					}
					code.push(						"if (" + result + " != null && " + result + " !== '')",
													"	out.write(" + result + ");");
					if (values['default'])
						code.push(					"else",
													"	out.write(" + values['default'] + ");");
				}
			}
		}
		if (toString && postProcess) {
			// This is needed for nested macros. Insert out.push() before the 
			// rendering code and return out.pop(). Due to the post processing
			// we cannot simply return a variable...
			code.splice(codeIndexBefore, 0,		"out.push();");
			return "out.pop()";
		}
		// Tell parse() wether to swallow the line break or not.
		if (!toString)
			return macro.swallow;
	},

	/**
	 * Parses the passed string for loop variable related calls (e.g. $entry#isFirst)
	 * and replaces them with the proper code.
	 */
	parseLoopVariables: function(str, stack) {
		return str.replace(/(\$[\w_]+)\#(\w+)/, function(part, variable, suffix) {
			// Use the last loop.
			var loopStack = stack.loop[variable], loop = loopStack && loopStack[loopStack.length - 1];
			if (loop) {
				switch (suffix) {
				case "index": return loop.index;
				case "length": return loop.length;
				case "isFirst": return "(" + loop.index + " == 0)";
				case "isLast": return "(" + loop.index + " == " + loop.length + " - 1)";
				case "even": return "((" + loop.index + " & 1) == 0)";
				case "odd": return "((" + loop.index + " & 1) == 1)";
				}
			}
			return part;
		});
	},

	/**
	 * Parses a tempate tag and creates a new sub tempalte in the subTemplates
	 * array. Also handles the case where a template needs to directly be rendered
	 * to a varible.
	 */
	parseTemplateTag: function(tag, code) {
		var match = tag.tag.match(/^<%\s*([$#])(\S*)\s*([+-]?)%>$/);
		if (match) {
			var name = match[2], content = tag.buffer.join(''), end = match[3];
			// If the tag ends with -%>, trim the whole content.
			// If it does not end with +%>, cut away first and last empty line:
			// If it ends with +%>, keep the whitespaces.
			if (!end) content = content.match(/^\s*[\n\r]?([\s\S]*)[\n\r]?\s*$/)[1];
			else if (end == '-') content = content.trim();
			var template = this.subTemplates[name] = new Template(content, name);
			template.parent = this;
			// If it is a variable, push it onto renderTemplates, so it is
			// rendered at the beginning of the generated render function.
			if (match[1] == '$')
				this.renderTemplates.push({ name: name, trim: end == '-' });
		} else
			throw "Syntax error in template";
	},

	/**
	 * Writes out the value and handles prefix, suffix, default, and filter chains.
	 * This is called at rendering time, not parsing time.
	 */
	write: function(value, filters, prefix, suffix, deflt, out) {
		if (value != null && value !== '') {
			if (filters) {
				// Walk through the filters, if defined.
				for (var i = 0; i < filters.length; i++) {
					var filter = filters[i];
					var func = filter.object && filter.object[filter.name + "_filter"];
					if (func) {
						if (func.apply) // filter function
							value = func.apply(filter.object, [value].concat(filter.arguments));
						else if (func.exec) // filter regexp
							value = func.exec(value)[0];
					} else {
						out.write('[Filter unhandled: "' + filter.command + '"]');
					}
				}
			}
			// TODO: where do we need to add the prefixes? compare with Helma
			if (prefix) out.write(prefix);
			out.write(value);
			if (suffix) out.write(suffix);
		} else if (deflt) {
			// write out default value if it's defined and if
			// the returned value was not set
			out.write(deflt);
		}
	},

	/**
	 * Renders the macro with the given name on object, passing it the arguments.
	 * This is called at rendering time, not parsing time.
	 */
	renderMacro: function(command, object, name, param, args, out) {
		var unhandled = false, value;
		if (object) {
			// see  if there's a macro with that name,
			// and if not, assume a property
			var macro = object[name + "_macro"];
			if (macro) {
				try {
					// Add a reference to this template and the param
					// object of the template as the parent to inherit from.
					args[0].__template__ = this.parent || this;
					args[0].__param__ = param;
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

	/**
	 * Encodes the passed string by escaping ",',\n,\r so it can be used
	 * within a JS String.
	 */
	encodeJs: function(str) {
		return str ? (str = uneval(str)).substring(1, str.length - 1) : str;
	},

	/**
	 * Reads and parses the template, compiles the generated render function
	 * and stores it in the template object.
	 */
	compile: function() {
		var lines;
		try {
			if  (this.resource) {
				// use java.io.BufferedReader for reading the lines into a line array,
				// as this is much faster than the regexp above
				var reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(this.resource.getInputStream()));
				lines = [];
				var line;
				while ((line = reader.readLine()) != null)
					lines.push(line);
				reader.close();
				this.lastModified = this.resource.lastModified();
			} else if (this.content) {
				lines = this.content.split(/[\r\n]/mg);
			} else {
				lines = [];
			}
			this.subTemplates = {};
			// Keep a reference to all sub templates to be
			// rendered into variable names (as defined by <% $name %> tags...)
			this.renderTemplates = [];
			var code = this.parse(lines);
			// Now evalute the template code.
			// This sets this.__render__ to the generated function
			// don't use eval() but Rhino's evaluateString instead, because this
			// throws propper traceable exceptions if something goes wrong.
			// switch optimization level for the compilation of this routine,
			// as java bytecode methods have a maximum size that is too small
			// for templates.
			var cx = Packages.org.mozilla.javascript.Context.getCurrentContext();
			var level = cx.getOptimizationLevel();
			cx.setOptimizationLevel(-1);
			cx.evaluateString(this, code, this.pathName, 0, null);
			cx.setOptimizationLevel(level);
		} catch (e) {
			this.throwError(e);
		}
		this.lastChecked = new Date().getTime();
	},

	/**
	 * Reports a template error and prints the line causing the error
	 */
	throwError: function(error, line) {
		var tag = line ? this.getTagFromCodeLine(line) : this.getTagFromException(error);
		var message = "Template error in " + this.pathName;
		// if this error already comes from throwError, do not generate message again
		if (typeof error == "string" && error.indexOf(message) == 0)
			throw error;
		if (tag) {
		 	message += ", line: " + (tag.lineNumber + 1) + ', in ' +
				tag.content;
		}
		if (error) {
			var details = null;
			if (error.fileName && error.fileName != this.pathName) {
				details = "Error in " + error.fileName + ", line " +
					error.lineNumber + ": " + error;
			} else {
				details = error;
			}
			// now generate the stacktrace:
			if (error.javaException) {
				var sw = new java.io.StringWriter();
				error.javaException.printStackTrace(new java.io.PrintWriter(sw));
				details += '\nStacktrace:\n' + sw.toString();
			}
			if (details)
				message += ": " + details;
		}
		throw message;
	},

	getTagFromCodeLine: function(number) {
		// walk up the code lines until there's a line number
		// linking the code line to a template line
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

/*
Object.prototype.renderTemplate = function(template, param) {
	if (!(template instanceof Template)) {
		if (/\.jstl$/.test(template))
			template = new Template(new java.io.File(template));
		else
			template = new Template(template);
	}
	return template.render(this, param);
}

Object.prototype.dontEnum("renderTemplate");
*/

