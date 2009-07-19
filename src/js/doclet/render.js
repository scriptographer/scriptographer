/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

function renderLink(param) {
	if (settings.hyperref) {
		var str = '<a href="';
		if (param.path) {
			var path = DocObject.getRelativeIdentifier(param.path).replace('.', '/');
			// Link to the index file for packages
			var name = Type.getSimpleName(param.path);
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
			if (param.toggle)
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
	var Translate = Packages.org.htmlparser.util.Translate;
	str = Translate.encode(str);
	if (!encodeHtml.tags) {
		var tags = ['code', 'br', 'p', 'b', 'a', 'i', 'ol', 'li', 'ul', 'tt', 'pre', 'hr'];
		for (var i = 1; i <= 4; i++)
			tags.push('h' + i);
		encodeHtml.tags = tags.each(function(tag) {
			this[tag] = true;
		}, {});
	}
	// Now replace allowed tags again.
	return str.replace(/&lt;(\/?)(\w*)(.*?)(\s*\/?)&gt;/g, function(match, open, tag, content, close) {
		tag = tag.toLowerCase();
		return encodeHtml.tags[tag] ? '<' + open + tag + Translate.decode(content) + close + '>' : match;
	});
}

function encodeAll(str) {
	return Packages.org.htmlparser.util.Translate.encode(str);
}

function stripTags(str, tag) {
	var tag = tag || '.*?'; // Default: all tags
	return str.replace(new RegExp('<' + tag + '>|</' + tag + '>', 'g'), '');
}

/**
 * Prints a sequence of tags obtained from e.g. com.sun.javadoc.Doc.tags().
 */
function renderTags(param) {
	var str = renderTemplate('tags', param);
	// Replace inline <code></code> with <tt></tt>
	str = str.replace(/<code>[ \t]*([^\n\r]*?)[ \t]*<\/code>/g, function(match, content) {
		return '<tt>' + content + '</tt>';
	});
	// Put code and pre tags on the same line as the content, as white-space: pre is set:
	str = str.replace(/(<(?:code|pre)>)\s*([\u0000-\uffff]*?)\s*(<\/(?:code|pre)>)/g, function(match, open, content, close) {
		// Filter out the first white space at the beginning of each line, since
		// that stems from the space after the * in the comment.
		return open + content.replace(/(\n|\r\n) /mg, function(match, lineBreak) {
			return lineBreak;
		}) + close;
	});
	// Empty lines -> Paragraphs
	if (!param.stripParagraphs) {
		str = '<p>' + str.trim() + '</p>';
		str = str.trim().replace(/(\n|\r\n)\s*(\n|\r\n)/g, function(match, lineBreak) {
			return '</p>' + lineBreak + '<p>';
		});
		// Automatically put </p><p> at the end of sentences with line breaks.
		// Match following </p> and <p> tags and swallow them. This happens when
		// the original content contains these.
		str = str.trim().replace(/([.:?!;])\s*(\n|\r\n)(\s*)(<\/p>|<p>|)/g, function(match, before, lineBreak, whiteSpace, after) {
			// Include following whiteSpace as well, since for code blocks they are relevant (e.g. indentation on new line)
			return before + '</p>' + lineBreak + whiteSpace + '<p>';
		});
		// Filter out <p> tags within and around <code> blocks again
		str = str.replace(/((?:<p>\s*|)<code>[\u0000-\uffff]*<\/code>(?:\s*<\/p>|))/g, function(match, code) {
			return stripTags(code, 'p');
		});
		// Filter out empty paragraphs
		str = str.replace(/<p><\/p>/g, '');
	}
	return str;
}
