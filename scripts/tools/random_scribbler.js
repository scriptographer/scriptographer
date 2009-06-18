var point, path;
var values = {
	size: 10
};

function onOptions() {
	values = Dialog.prompt('Random Scribbler:', {
		size: { description: 'Size'}
	}, values);
}

function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
	point = event.point;
}

function onMouseUp(event) {
	// path.pointsToCurves(25, 10, 10.0, 10.0);
}

function onMouseDrag(event) {
	point += (Point.random() - 0.5) * values.size
	path.lineTo(point);
}