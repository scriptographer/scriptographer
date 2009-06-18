var tolerance = 10;
var radius = 5;
checkValues();

var handle;
function checkValues() {
	var min = radius * 2;
	if (tolerance < min) tolerance = min;
	handle = radius * 0.5522847498; // kappa
}

function onOptions() {
	var values = Dialog.prompt('Square Radius:', [
		{ value: radius, description: 'Radius', width: 50 },
		{ value: tolerance, description: 'Tolerance', width: 50 }
	]);
	radius = values[0];
	tolerance = values[1];
	checkValues();
}

var path;
function onMouseDown(event) {
	path = new Path();
	path.segments = [event.point, event.point];
	prevPoint = path.segments.first.point;
	curPoint = path.segments.last.point;
	curHandleSeg = null;
}

function onMouseUp(event) {
//	path.pointsToCurves(0, 0, 1, 0.001);
}

var c = 0;

var curPoint, prevPoint, curHandleSeg;
function onMouseDrag(event) {
	var point = event.point;
	var diff = (point - prevPoint).abs();
	if (diff.x < diff.y) {
		curPoint.x = prevPoint.x;
		curPoint.y = point.y;
	} else {
		curPoint.x = point.x;
		curPoint.y = prevPoint.y;
	}
	var normal = (curPoint - prevPoint).normalize();
	if (curHandleSeg != null) {
		curHandleSeg.point = prevPoint + (normal * radius);
		curHandleSeg.handleIn = normal * -handle;
	}
	var minDiff = Math.min(diff.x, diff.y);
	if (minDiff > tolerance) {
		var seg = path.segments.removeLast();
		path.segments.add(new Segment(curPoint - (normal * radius), null, normal * handle));
		path.segments.add(seg);
		curHandleSeg = path.segments.last;
		prevPoint = curHandleSeg.point.clone(); // clone as we want the unmodified one!
		path.segments.add(seg);
		curPoint = path.segments.last.point;
	}
}