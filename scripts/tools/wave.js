// the mouse has to move at least 10 points
distanceThreshold = (10);

var path;

function onMouseDown(event) {
	path = new Path();
}

function onMouseDrag(event) {
	var rotation = event.count.isEven() ? 90 : -90;
	var vector = event.delta.rotate(rotation.toDegrees());
	path.lineTo(event.point + vector);
	path.segments.last.handleIn = -event.delta/2;
	path.segments.last.handleOut = event.delta/2;
}