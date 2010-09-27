////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	size: 50
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	size: {
		label: 'Radius',
		type: 'range',
		range: [0, 1000]
	}
};

var palette = new Palette('Scribbler', components, values);

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