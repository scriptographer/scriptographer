//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = { size: 10 };

var components = {
	size: { label: 'Size', type: 'number', steppers: true }
};

var palette = new Palette('Gridder', components, values);

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

var point, path;

function getPos(pt) {
	return (pt / values.size).round() * values.size;
}

function onMouseDown(event) {
	point = getPos(event.point);
	path = new Path();
	path.add(point);
}

function onMouseDrag(event) {
	var p = getPos(event.point);
	if (point != p) {
		path.add(p);
		point = p;
	}
}