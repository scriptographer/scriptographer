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

// Define global encodeHtml and encodeAll, used for encoding
if (!global.encodeHtml)
	encodeHtml = format;
if (!global.encodeAll)
	encodeAll = encode;

/**
 * Constructor
 *
 * @object a file to read the template from, or an object to retrieve a
 * template with the given name from
 * @name the name of the resource in the object
 */
function Template(object, name) {
	if (object) {
		if (object instanceof File)
			object = new java.io.File(object.getPath());
		if (object instanceof java.io.File) {
			if (!object.exists())
				throw "Cannot find template " + object;
			// it's a file, so create the resource directly here:
			this.resource = new Packages.helma.framework.repository.FileResource(object);
			this.resourceName = this.resource.getShortName();
			this.pathName = this.resource.getName();
		} else if (typeof object == "string") {
			this.content = object;
			this.resourceName = name ? name : "string";
			this.pathName = this.resourceName;
		} else {
			this.resourceContainer = object;
			this.resourceName = name + ".jstl";
			this.findResource();
		}
		this.compile();
	}
}

Template.prototype = {
	render: function(object, param, out) {
		try {
			// inherit from param.__param__ if it is set:
			if (param && param.__param__) {
				function inherit() {};
				inherit.prototype = param.__param__;
				var prm = new inherit();
				// and copy over from param:
				for (var i in param)
					prm[i] = param[i];
				param = prm;
			}
			// if out is null, render to a string and return it
			var asString = !out;
			if (asString) (out = res).push();
			this.__render__.call(object, param, this, out);
			if (asString) return out.pop();
		} catch (e) {
			// in case the exception happened in a finished template,
			// output the error for the template
			if (typeof e != "string") {
				this.throwError(e);
			} else {
				// just throw it, for debugging of renderTemplate
				throw e;
			}
		}
	},

	getSubTemplate: function(name) {
		return this.subTemplates[name];
	},

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
		var newLine = java.lang.System.getProperty("line.separator");
		var tagOpenLine; // the line number of the last open tag
		var skipNewLine = false;
		var tagCounter = 0;
		var templateTag = null;
		// Container for the generated code lines.
		var code = [ "this.__render__ = function(param, template, out) {" ];
		// Stack for control tags
		var controls = [];
		for (var i = 0; i < lines.length; i++) {
			var line = lines[i];
			var start = 0, end = 0;
			while (true) {
				// Search start and end of macro tag, keep lines:
				if (tagCounter > 0) {
					while (tagCounter != 0) {
						end = line.indexOf("%", end + 1); // skip %
						if (end == -1)
							break;
						if (line[end - 1] == '<') tagCounter++;
						if (line[end + 1] == '>') tagCounter--;
					}
					if (end != -1) { // found the end of the macro
						end += 2; // include tag end as well
						buffer.push(line.substring(start, end));
						// parse it:
						var tag = buffer.join('');
						this.tags[code.length] = { lineNumber: i, content: tag };
						// If this is a template tag, change the state to
						// finding the end of the template. Do a thourough check
						// as this tag might also be the setting of a variable
						// or the call of a mcro on a scope variable.
						if (/^<%\s*[#$]\w*\s*\+?%>$/.test(tag)) {
							templateTag = tag;
						} else {
							// Keep track of line numbers. this.tags links code line numbers
							// to template line numbers
							if (this.parseMacro(tag, code, controls) && end == line.length)
								skipNewLine = true;
						}
						// Now buffer collects lines between tags
						buffer.length = 0;
					} else {
						buffer.push(line.substring(start), newLine);
						break;
					}
				} else {
					start = line.indexOf("<%", end);
					if (start != -1) { // Found the begining of a macro
						tagOpenLine = i;
						if (start > end) // There was some text before it
							buffer.push(line.substring(end, start));
						// Skip <, the % is skiped above in line.indexOf("%", end + 1);
						end = start + 1;
						// Now buffer collects tag lines
						if (templateTag) {
							// find the end of the template tag = the start of a new tag
							// Avoid calling substring, but this might actually be
							// slowler? TODO: find out.
							// var templateStart = /<%\s*[$#]/g;
							// templateStart.lastIndex = start;
							// var end = templateStart.exec(line);
							// if (end && end.index == start) {
							// 10 chars should be enough for <% $, even with a lot of whitespace
							if (/^<%\s*[$#]/.test(line.substring(start, 10))) {
								this.parseTemplateTag(templateTag, code, buffer.join(''));
								buffer.length = 0;
								tagCounter++;
							} else {
								// Just a normal tag. add it to the template buffer
								// and continue with the next line
								buffer.push(line.substring(start), newLine);
								break;
							};
						} else {
							tagCounter++;
							if (buffer.length > 0) { // Write out text lines
								code.push('out.write("' + this.encodeJs(buffer.join('')) + '");');
								buffer.length = 0;
							}
						}
					} else {
						if (skipNewLine)
							skipNewLine = false;
						else
							buffer.push(line.substring(end), newLine);
						break;
					}
				}
			}
		}
		if (tagCounter) { // Report the tag that was left open
			this.throwError("Tag not closed", code.length);
		} else if (controls.length) {
			this.throwError("Control tag is not closed", null, controls.pop().macro.tag);
		} else { // Write out the rest
			if (templateTag) // Parse the last template tag
				this.parseTemplateTag(templateTag, code, buffer.join(''));
			else
				code.push('out.write("' + this.encodeJs(buffer.join('')) + '");');
		}
		for (var i = 0; i < this.renderTemplates.length; i++) {
			var template = this.renderTemplates[i];
			code.splice(1, 0, "var $" + template.name + " = template.renderSubTemplate(this, '" + template.name + "', param);");
			// Shift tags as well, so line numbers are still right
			this.tags.unshift(null);
		}
		code.push('}');
		return code.join(newLine);
	},

	/**
	 * Parses the 
	 */
	parseMacroParts: function(tag, code, controls) {
		var match = tag.match(/^<%(=?)\s*(.*?)\s*(-?)%>$/);
		if (!match)	return null;
		// if the tag ends with -%>, the newline after it should be swallowed,
		// if there is any. By default all control macros swallow newlines.
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
				} else if (/[=|]/.test(ch)) { // Named parameter / filter
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
					this.throwError("Syntax error", code.length);
				macro.opcode = macro.opcode.join(' ');
				if (macro.isControl) {
					// strip away () if there.
					macro.opcode = macro.opcode.match(/^\(?(.*?)\)?$/)[1];
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
					macro.isControl = controls && /^(foreach|if|elseif|else|end)$/.test(next);
					// Is this a data macro?
					var data = macro.command.match(/^(param|response|request|session)\.(.*)$/);
					if (data) {
						// allow lookup to session.user, everything else goes to session.data
						if (!/^session\.user\b/.test(macro.command))
							macro.command = {
								response: "res.data.", request: "req.data.", 
								session: "session.data.", param: "param."
							}[data[1]] + data[2];
					}
					// Tell the parseMacro code to simply output the value
					macro.isData = isEqualTag || data;
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
			} else if (part == '|') { // start a filter
				isFirst = true;
			} else if (/.=$/.test(part)) { // named param
				// TODO: Calling nextPart here should only return values, nothing else!
				// add error handling...
				var key = part.substring(0, part.length - 1), value = nextPart();
				if (/^<%/.test(value)) {
					// A nested macro: render it, then set the result to a variable
					var nestedTag = value;
					value = "param_" + (macroParam++) + "";
					code.push("var " + value + " = " + this.parseMacro(nestedTag, code, null, true) + ";");
				}
				macro.param.push('"' + key + '": ' + value);
				// Override defaults only:
				if (macro.values[key] !== undefined)
					macro.values[key] = value;
				// Appending to macro command not allowed after first parameter
				append = false;
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
					this.throwError("Syntax error: '" + part + "'", code.length);
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
		// All control macros swallow newlines:
		macro.swallow = swallow || macro.isControl;
		macro.tag = tag;
		return macro;
	},

	/**
	 * Parses the tag and reports possible syntax errors.
	 * This is the core of the template parser
	 */
	parseMacro: function(tag, code, controls, toString) {
		// only process if it is not a comment or a swallow newline tag.
		// return true tells parse() to swallow newline.
		if (/^<%--/.test(tag) || tag == '<%-%>') return true;
		// <%= tags cannot have unnamed parameters
		var macro = this.parseMacroParts(tag, code, controls);
		if (!macro)
			this.throwError("Invalid tag", code.length);
		var values = macro.values, result;
		var postProcess = values.prefix || values.suffix || macro.filters;
		var codeIndexBefore = code.length;
		var error = false;
		if (macro.isData) { // param, response, request, session, or a <%= %> tag
			result = macro.command + macro.opcode;
		} else if (macro.isControl) {
			var open = false, close = false, separator;
			// For control macros, the default is: Syntax error
			// Code bellow erases this if all went well.
			error = "Syntax error";
			var prevControl = controls[controls.length - 1];
			// Only allow else with and if beforehand
			if (/^else/.test(macro.command) && (!prevControl || !/if$/.test(prevControl.macro.command))) {
				error = "Syntax error: 'else' requiers 'if' or 'elseif'";
			} else {
				switch (macro.command) {
				case "foreach":
					var match = macro.opcode.match(/^\s*(\$\w*)\s*in\s*(.*)$/);
					if (!match) break;
					open = true;
					separator = values.separator;
					postProcess = postProcess || separator;
					var list = "list_" + this.listId;
					var counter = "i_" + (this.listId++);
					var value = match[2];
					code.push(						"var " + list + " = " + value + (separator ? ", first = true" : "") + ";",
						// The check for HopObject is only necessary if it's a
						// variable reference and not an explicit string / array / etc.
						!(/^["'[]/.test(value))	?	"if (" + list + " instanceof HopObject) " + list + " = " + list + ".list();" : null,
													"for (var " + counter + " = 0; " + counter + " < " + list + ".length; " + counter + "++) {",
													"	var " + match[1] + " = " + list + "[" + counter + "];",
						separator				?	"	out.push();" : null);

					error = false;
					break;
				case "elseif":
					close = true;
					// More in "if" (no break here)
				case "if":
					if (macro.opcode) {
						open = true;
						code.push(					(macro.command == "if" ? "if (" : "} else if (") + macro.opcode + ") {");
						error = false;
					}
					break;
				case "else":
					if (!macro.opcode) {
						close = true;
						open = true;
						code.push(					"} else {");
						error = false;
					}
					break;
				case "end":
					if (!macro.opcode) {
						if (!prevControl || !/^else|^if$|^foreach$/.test(prevControl.macro.command)) {
							error = "Syntax error: 'end' requiers 'if', 'else', 'elseif' or 'foreach'";
						} else {
							close = true;
							if (prevControl.separator)
								code.push(			"	var val = out.pop();",
													"	if (val != null && val !== '') {",
													"		if (first) first = false;",
													"		else out.write(" + prevControl.separator + ");",
													"		out.write(val)",
													"	}");
							code.push(				"}");
							error = false;
						}
					}
					break;
				}
				if (!error) {
					if (close) {
						var control = controls.pop();
						if (control.postProcess) {
							values = control.postProcess;
							postProcess = true;
							result = "out.pop()";
						}
					}
					if (open) {
						controls.push({ macro: macro, separator: separator,
								postProcess: postProcess ? values : null });
						// Add out.push() before the block if it needs postProcessing
						if (postProcess)
							code.splice(codeIndexBefore, 0,	"out.push();");
					}
				}
			}
		} else { // A normal <% %> macro
			if (macro.opcode) {
				// Setting a value? If not, this is a syntax error.
				if (macro.command[0] == '$')
					code.push(						"var " + macro.command + macro.opcode + ";");
				else
					error = "Syntax error"; // No opcodes allowed in macros
			} else {
				var object = macro.object;
				// At runtime, first determine the object.
				// it might be a res.handler.
				if (!/^(global|this|root)$/.test(object))
					code.push(						"try {",
													"	var obj = " + object + ";",
													"} catch (e) {",
													"	var obj = res.handlers['" + object + "'];",
													"}");
				else
					code.push(						"var obj = " + object + ";");
				object = "obj";
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
		if (error)
			this.throwError(error, code.length);
		if (result) { // Write the value out
			// strip away possible ; at the end:
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
					// dereference to local variable if it's a call or a lookup:
					if (/[.()]/.test(result)) {
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
		// tell parse() wether to swallow the newline character or not.
		if (!toString)
			return macro.swallow;
	},

	parseTemplateTag: function(tag, code, content) {
		var match = tag.match(/^<%\s*([$#])(\S*)\s*(\+?)%>$/);
		if (match) {
			var name = match[2];
			// if the tag does not end with +%>, cut away first and last empty line:
			// this is not exactly the opposite of swallow line above, but makes
			// sense that way.
			if (!match[3]) content = content.match(/^\s*[\n\r]?([\s\S]*)[\n\r]\s*?$/)[1];
			var template = this.subTemplates[name] = new Template(content, name);
			// If it is a variable, push it onto renderTemplates, so it is
			// rendered at the beginning of the generated render function.
			if (match[1] == '$')
				this.renderTemplates.push({ name: name, template: template });
		} else
			this.throwError("Syntax error in template", code.length);
	},

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
					args[0].__template__ = this;
					args[0].__param__ = param;
					value = macro.apply(object, args);
				} catch (e) {
					var tag = this.getTagFromException(e);
					var message = e.message || e;
					if (tag && tag.content) {
						message += ' (' + e.fileName + '; line ' + tag.lineNumber + ': ' +
							// encode errors, as they are passed through to Jetty
							// and appear garbled otherwise
							encode(tag.content) + ')';
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
			 	// var content = this.resource.getContent(getProperty("skinCharset"));
			 	// store the original lines:
			 	// var lines = content.split(/[\r\n]/mg);
				// use java.io.BufferedReader for reading the lines into a line array,
				// as this is much faster than the regexp above
				var charset = getProperty("skinCharset");
				var reader = new java.io.BufferedReader(
					charset ? new java.io.InputStreamReader(this.resource.getInputStream(), charset) :
						new java.io.InputStreamReader(this.resource.getInputStream())
				);
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
			// keep a reference to all sub templates to be
			// rendered into variable names (as defined by <% $name %> tags...)
			this.renderTemplates = [];
			var code = this.parse(lines);
			// now evalute the template code.
			// this sets this.__render__ to the generated function
			this.evaluate(this, code);
		} catch (e) {
			this.throwError(e);
		}
		this.lastChecked = new Date().getTime();
	},

	evaluate: function(scope, code) {
		// don't use eval() but Rhino's evaluateString instead, because this
		// throws propper traceable exceptions if something goes wrong.
		// switch optimization level for the compilation of this routine,
		// as java bytecode methods have a maximum size that is too small
		// for templates.
		var ctx = Packages.org.mozilla.javascript.Context.getCurrentContext();
		var level = ctx.getOptimizationLevel();
		ctx.setOptimizationLevel(-1);
		var ret = ctx.evaluateString(scope, code, this.pathName, 0, null);
		ctx.setOptimizationLevel(level);
		return ret;
	},

	/**
	 * Tries to find the resource in resourceContainer.
	 */
	findResource: function() {
		var container = this.resourceContainer;
		if (container) {
			this.resource = container.getResource(this.resourceName);
			if (!this.resource)
				throw 'Cannot find template "' + this.resourceName + '" in "' + 
					(container._prototype ? container._prototype : container) + '".';
			this.lastModified = 0; // force compile
			this.tags = null;
			this.pathName = this.resource.getName();
		}
	},

	/**
	 * Checks the resource's lastModified value and compiles again
	 * in case it has changed. Only checks every second.
	 */
	checkResource: function() {
		var now = new Date().getTime();
		// only check for modifications every second.
		if (now - this.lastChecked > 1000) {
			this.lastChecked = now;
			if (!this.resource || !this.resource.exists())
				this.findResource();
			if  (this.lastModified != this.resource.lastModified())
				this.compile();
		}
	},

	/**
	 * Reports a template error and prints the line causing the error
	 */
	throwError: function(error, line, construct) {
		var tag = line ? this.getTagFromCodeLine(line) : this.getTagFromException(error);
		if (tag) {
			line = tag.lineNumber;
			construct = tag.content;
		}
		var message = "Template error in " + this.pathName;
		if (line)
		 	message += ", line: " + (parseInt(line) + 1);
		if (construct)
			// encode errors, as they are passed through to Jetty
			// and appear garbled otherwise
		 	message += ', in ' + encode(construct);
		if (error) {
			var details;
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
		if (e.fileName == this.pathName &&
			e.lineNumber && this.tags) {
			return this.getTagFromCodeLine(e.lineNumber);
		}
		return null;
	}
}

HopObject.prototype.getTemplate = function(template) {
	var name = template;
	if (!(template instanceof Template)) {
		// Handle sub templates:
		var pos = name.indexOf('#');
		if (pos != -1) {
			template = this.getTemplate(name.substring(0, pos));
			if (template)
				return template.getSubTemplate(name.substring(pos + 1));
		}
		// Use a hashtable in __proto__.constructor as a cache for
		// template objects
		// __proto__ alone would not work, as it would be passed down
		// the heritance chain, and all templates would be filled in one
		// common cache in HopObject
		var ctor = this.__proto__.constructor, cache = ctor.__templates__;
		if (!cache) cache = ctor.__templates__ = {};
		template = cache[name];
	}
	if (!template) {
		// TODO: there might already be a cached template for this resource
		// in one of super prototypes... unfortunatelly, currently this engine
		// parses and caches the template again for each prototype, as we don't
		// know if getResource is returning a resource from the current
		// prototypes or one of the super prototypes
		template = cache[name] = new Template(this, name);
	} else {
		// If it was created before, check for modifications now
		template.checkResource();
	}
	return template;
}

/**
 * HopObject's renderTemplate function that is to be used from Helma.
 */
HopObject.prototype.renderTemplate = function(template, param, out) {
	template = this.getTemplate(template);
	if (template)
		return template.render(this, param, out);
}

HopObject.prototype.template_macro = function(param, name) {
	if (name[0] == '#') {
		return param.__template__.renderSubTemplate(this, name.substring(1), param);
	} else {
		return this.renderTemplate(name, param);
	}
}

