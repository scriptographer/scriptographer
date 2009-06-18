var values = {
	radius: 10,
	tolerance: 5
};

checkValues();

var handle;
function checkValues() {
	var min = values.radius * 2;
	if (values.tolerance < min) values.tolerance = min;
	handle = values.radius * 0.5522847498; // kappa
}

function onOptions() {
	values = Dialog.prompt('Square Radius:', {
		radius: { description: 'Radius'},
		tolerance: { description: 'Tolerance' }
	}, values);
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
		curHandleSeg.point = prevPoint + (normal * values.radius);
		curHandleSeg.handleIn = normal * -handle;
	}
	var minDiff = Math.min(diff.x, diff.y);
	if (minDiff > values.tolerance) {
		var seg = path.segments.removeLast();
		path.segments.add(new Segment(curPoint - (normal * values.radius), null, normal * handle));
		path.segments.add(seg);
		curHandleSeg = path.segments.last;
		prevPoint = curHandleSeg.point.clone(); // clone as we want the unmodified one!
		path.segments.add(seg);
		curPoint = path.segments.last.point;
	}
}