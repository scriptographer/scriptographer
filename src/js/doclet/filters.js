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
