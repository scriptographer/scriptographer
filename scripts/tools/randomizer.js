var path;
var values = { size: 50 };

function onOptions() {
	values = Dialog.prompt('Randomizer:', {
		size: { description: 'Radius', type: 'range', min: 0, max: 1000, step: 0.5 }
	}, values);
}

function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
}

function onMouseDrag(event) {
	var point = event.point;
	path.curveTo(
		point + (Point.random() - 0.5) * values.size,
		point + (Point.random() - 0.5) * values.size,
		point + (Point.random() - 0.5) * values.size
	);
}