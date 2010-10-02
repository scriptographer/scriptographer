////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	radius: 10,
	tolerance: 5
};

checkValues();

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	radius: {
		description: 'Radius',
		min: 0,
		steppers: true
	},
	tolerance: {
		description: 'Tolerance',
		min: 0,
		steppers: true
	}
};

var palette = new Palette('Square Radius', components, values);
palette.onChange = function(component) {
	checkValues();
};

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var handle;
function checkValues() {
	var min = values.radius * 2;
	if (values.tolerance < min) values.tolerance = min;
	handle = values.radius * 0.5522847498; // kappa
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
	var normal = (curPoint - prevPoint);
	normal.length = 1;
	if (curHandleSeg) {
		curHandleSeg.point = prevPoint + (normal * values.radius);
		curHandleSeg.handleIn = normal * -handle;
	}
	var minDiff = Math.min(diff.x, diff.y);
	if (minDiff > values.tolerance) {
		var segment = new Segment(curPoint - (normal * values.radius), null, normal * handle);
		path.insert(path.segments.length - 1, segment);
		curHandleSeg = path.segments.last;
		prevPoint = curHandleSeg.point.clone(); // clone as we want the unmodified one!
		path.add(curHandleSeg);
		curPoint = path.segments.last.point;
	}
}