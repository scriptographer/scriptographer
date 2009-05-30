/**
 * JavaScript Doclet
 * (c) 2005 - 2007, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

function code_filter(str) {
	return '<tt>' + str + '</tt>';
}

function stripCode_filter(str) {
	return str.replace(/<tt>|<\/tt>/g, ' ').replace(/\\s+/g, ' ');
}

function tags_filter(str) {
	// Replace inline <code></code> with <tt></tt>
	str = str.replace(/<code>[ \t]*([^\n\r]*?)[ \t]*<\/code>/g, function(match, content) {
		return '<tt>' + content + '</tt>';
	});
	// Put code tags on the same line as the content, as white-space: pre is set:
	str = str.replace(/<code>\s*([\s\S]*?)\s*<\/code>/g, function(match, content) {
		return '<code>' + content + '</code>';
	});
	// Automatically put <br /> at the end of sentences with line breaks.
	str = str.trim().replace(/([.:?!;])\s*(\n|\r\n)/g, function(match, before, lineBreak) {
		return before + '<br />' + lineBreak;
	});
	return str;
}

function stripTags_filter(str) {
	return str.replace(/<.*?>|<\/.*?>/g, ' ').replace(/\\s+/g, ' ');
}
