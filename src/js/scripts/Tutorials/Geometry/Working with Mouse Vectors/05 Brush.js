////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/geometry/working-with-mouse-vectors/#adding-brush-stroke-ends

tool.fixedDistance = 15;

var path;
var strokeEnds = 6;

function onMouseDown(event) {
	path = new Path();
	path.fillColor = document.swatches.pick().color;
}

var lastPoint;
function onMouseDrag(event) {
	// If this is the first drag event,
	// add the strokes at the start:
	if(event.count == 1) {
		addStrokes(event.middlePoint, event.delta * -1);
	} else {
		var step = event.delta / 2;
		step.angle += 90;

		var top = event.middlePoint + step;
		var bottom = event.middlePoint - step;

		path.add(top);
		path.insert(0, bottom);
	}
	path.smooth();
	
	lastPoint = event.middlePoint;
}

function onMouseUp(event) {
	var delta = event.point - lastPoint;
	delta.length = tool.maxDistance;
	addStrokes(event.point, delta);
	path.closed = true;
	path.smooth();
}

function addStrokes(point, delta) {
	var step = delta.rotate(90);
	var strokePoints = strokeEnds * 2 + 1;
	point -= step / 2;
	step /= strokePoints - 1;
	for(var i = 0; i < strokePoints; i++) {
		var strokePoint = point + step * i;
		var offset = delta * (Math.random() * 0.3 + 0.1);
		if(i.isEven()) {
			offset *= -1;
		}
		strokePoint += offset;
		path.insert(0, strokePoint);
	}
}