//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = { size: 50 };
var components = {
	size: {
		label: 'Radius', type: 'range',
		min: 0, max: 1000, step: 0.5
	}
};

var palette = new Palette('Scribbler', components, values);

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

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