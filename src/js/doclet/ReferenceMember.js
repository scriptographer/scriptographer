/**
 * JavaScript Doclet
 * (c) 2005 - 2009, Juerg Lehni, http://www.scratchdisk.com
 *
 * Doclet.js is released under the MIT license
 * http://dev.scriptographer.com/ 
 */

ReferenceMember = SyntheticMember.extend({
	initialize: function(classObject, data, list) {
		this.base(classObject, data.name);
		this.reference = data.reference;
		this.type = data.type;
		this.after = data.after;
		this.list = list;
	},

	resolve: function() {
		var [cls, name] = this.reference.split('#');
		if (!/\./.test(cls))
			cls = this.classObject.classDoc.containingPackage().qualifiedName() + '.' + cls;
		var classObject = ClassObject.get(cls);
		// If it's a hidden class, force creation through ClassObject.put
		if (!classObject)
			classObject = ClassObject.put(cls, true);
		this.member = classObject.getMember(name);
		if (this.member)
			this.list.addAt(this.after, this);
	}
});
