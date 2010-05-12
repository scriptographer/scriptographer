//////////////////////////////////////////////////////////////////////////////
// Values:

var values = { size: 10 };

//////////////////////////////////////////////////////////////////////////////
// Interface:

var components = {
	size: {
		label: 'Size', type: 'number',
		steppers: true,
		min: 0.01
	}
};

var palette = new Palette('Grid', components, values);

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