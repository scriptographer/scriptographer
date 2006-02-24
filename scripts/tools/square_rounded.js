function onInit() {
	tolerance = 10;
	radius = 5;
	checkValues();
}

function checkValues() {
	var min = radius * 2;
	if (tolerance < min) tolerance = min;
	handle = radius * 0.5522847498; // kappa
}

function onOptions() {
	var values = Dialog.prompt("Square Radius:", [
		{ value: radius, description: "Radius", width: 50 },
		{ value: tolerance, description: "Tolerance", width: 50 }
	]);
	radius = values[0];
	tolerance = values[1];
	checkValues();
}

function onMouseDown(event) {
	path = new Path();
	path.segments = [event.point, event.point];
	prevPoint = path.segments[0].point;
	curPoint = path.segments[1].point;
	curHandleSeg = null;
}

function onMouseUp(event) {
//	path.pointsToCurves(0, 0, 1, 0.001);
}
	var c = 0;

function onMouseDrag(event) {
	var point = event.point;
	var xDiff = Math.abs(point.x - prevPoint.x);
	var yDiff = Math.abs(point.y - prevPoint.y);
	if (xDiff < yDiff) {
		curPoint.x = prevPoint.x;
		curPoint.y = point.y;
	} else {
		curPoint.x = point.x;
		curPoint.y = prevPoint.y;
	}
	var normal = curPoint.subtract(prevPoint).normalize();
	if (curHandleSeg != null) {
		curHandleSeg.point = prevPoint.add(normal.multiply(radius));
		curHandleSeg.handleIn = normal.multiply(-handle);
	}
	var minDiff = Math.min(xDiff, yDiff);
	if (minDiff > tolerance) {
		var seg = path.segments.removeLast();
		path.segments.add(new Segment(curPoint.subtract(normal.multiply(radius)), null, normal.multiply(handle)));
		path.segments.add(seg);
		curHandleSeg = path.segments.last;
		prevPoint = curHandleSeg.point.clone(); // clone as we want the unmodified one!
		path.segments.add(seg);
		curPoint = path.segments.last.point;
	}
}