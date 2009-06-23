/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

function stripTags_filter(str, param) {
	var res = stripTags(str, param.tag);
	return param.trim ? res.trim() : res;
}

function stripCode_filter(str) {
	return stripTags(str, 'tt').trim();
}

function stripParagraphs_filter(str) {
	return stripTags(str, 'p').trim();
}

function code_filter(str) {
	return '<tt>' + str + '</tt>';
}

function heading_filter(str, param, level) {
	var heading = settings.headings[level];
	return heading ? heading.open + str + heading.close : str;
}

function content_filter(str) {
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
		return '<code>' + content.replace(/(\n|\r\n) /mg, function(match, lineBreak) {
			return lineBreak;
		}) + '</code>';
	});
	// Empty lines -> Paragraphs
	str = str.trim().replace(/(\n|\r\n)\s*(\n|\r\n)/g, function(match, lineBreak1, lineBreak2) {
		return '</p>' + lineBreak1 + '<p>';
	});
	// Automatically put </p><p> at the end of sentences with line breaks.
	str = str.trim().replace(/([.:?!;])\s*(\n|\r\n)/g, function(match, before, lineBreak) {
		return before + '</p>' + lineBreak + '<p>';
	});
	// Filter out </p><p> within and around <code> blocks again
	str = str.replace(/((?:<p>\s*|)<code>[\u0000-\uffff]*<\/code>(?:\s*<\/p>|))/g, function(match, code) {
		return stripTags(code, 'p');
	});
	return '<p>' + str + '</p>';
}
