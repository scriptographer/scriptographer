/*
var l1 = new Layer("One");
var l2 = new Layer("Two");

var l11 = new Layer(l1, "One One");

var p1 = new Path(l11, "One");
var p2 = new Path(l2, "Two");
var g1 = new Group(l11, "Group");

var l111 = new Layer(g1, "One One One");
*/
function walkKids(obj, indent) {
	var str = "";
	for (var i = 0; i < indent; i++)
		str += " ";
	str += obj.name;
	str += " " + obj.selected;
	print(str);
	var children = obj.children;
	for (var i in children) {
		walkKids(children[i], indent + 2);
	}
}

for (var i in layers) {
	walkKids(layers[i], 0);
}