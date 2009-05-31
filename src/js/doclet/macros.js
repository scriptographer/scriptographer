/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

function tags_macro(param) {
	// Do not use prefix / suffix for the tag loop here, as we're in a macro where
	// these are already applied
	delete param.prefix;
	delete param.suffix;
	return renderTags(param);
}
