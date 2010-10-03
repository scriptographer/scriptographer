////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	size: 50
};

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var path;
function onMouseDown(event) {
	path = new Path();
	path.add(event.point);
}

function onMouseDrag(event) {
	path.curveTo(
		event.point + (Point.random() - 0.5) * values.size,
		event.point + (Point.random() - 0.5) * values.size,
		event.point + (Point.random() - 0.5) * values.size
	);
}

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	size: {
		label: 'Radius',
		type: 'slider',
		range: [0, 1000]
	}
};

var palette = new Palette('Scribbler', components, values);
