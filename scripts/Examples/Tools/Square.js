var values = { tolerance: 5 };

function onOptions() {
	values = Dialog.prompt('Square:', {
		tolerance: { description: 'Tolerance' }
	}, values);
}

var prevSeg, curSeg, path;
function onMouseDown(event) {
	path = new Path() {
		segments: [event.point, event.point]
	};
	prevSeg = path.segments.first;
	curSeg = path.segments.last;
}

function onMouseUp(event) {
	path.pointsToCurves(0, 0, 1, 0.001);
}

function onMouseDrag(event) {
	if (Key.isDown('space')) {
		path.segments.add(event.point);
		curSeg = path.segments.last;
	} else {
		var diff = (event.point - prevSeg.point).abs();
		var minDiff = Math.min(diff.x, diff.y);
		if (diff.x < diff.y) {
			curSeg.point = new Point(prevSeg.point.x, event.point.y);
		} else {
			curSeg.point = new Point(event.point.x, prevSeg.point.y);
		}
		if (minDiff > values.tolerance) {
			prevSeg = curSeg;
			path.segments.add(curSeg);
			curSeg = path.segments.last;
		}
	}
}