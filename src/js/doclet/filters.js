/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
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
	// Repace pre with code
	str = str.replace(/<pre>([\u0000-\uffff]*)<\/pre>/g, function(match, content) {
		return '<code>' + content + '</code>';
	});
	// Put code tags on the same line as the content, as white-space: pre is set:
	str = str.replace(/<code>\s*([\u0000-\uffff]*?)\s*<\/code>/g, function(match, content) {
		// Filter out the first white space at the beginning of each line, since that stems
		// from the space after the * in the comment.
		return '<code>' + content.replace(/(\r\n|\n|\r) /mg, function(match, newLine) {
			return newLine;
		}) + '</code>';
	});
	// Automatically put <br /> at the end of sentences with line breaks.
	str = str.trim().replace(/([.:?!;])\s*(\n|\r\n)/g, function(match, before, lineBreak) {
		return before + '<br />' + lineBreak;
	});
	// Filter out <br /> within <code> blocks again
	str = str.replace(/(<code>[\u0000-\uffff]*<\/code>)/g, function(match, content) {
		return content.replace(/<br \/>/g, '');
	});
	return str;
}

function stripTags_filter(str) {
	return str.replace(/<.*?>|<\/.*?>/g, ' ').replace(/\\s+/g, ' ');
}
