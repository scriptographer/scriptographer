////////////////////////////////////////////////////////////////////////////////
// Values

tool.fixedDistance = 10;

var values = { size: tool.fixedDistance };

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	size: {
		label: 'Size', type: 'number',
		steppers: true,
		range: [0.01, 500],
		onChange: function(value) {
			tool.fixedDistance = value;
		}
	}
};

var palette = new Palette('Grid', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

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