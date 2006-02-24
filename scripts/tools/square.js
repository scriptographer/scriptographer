function onInit() {
    tolerance = 5;
}

function onOptions() {
    tolerance = Dialog.prompt("Square:", [ { value: tolerance, description: "Tolerance", width: 50 } ])[0];
}

function onMouseDown(event) {
    path = new Path();
	path.segments = [event.point, event.point];
	prevSeg = path.segments[0];
	curSeg = path.segments[1];
}

function onMouseUp(event) {
	path.pointsToCurves(0, 0, 1, 0.001);
}

function onMouseDrag(event) {
	if (Key.isDown(Key.VK_SPACE)) {
		path.segments.add(event.point);
		curSeg = path.segments.last;
	} else {
		var xDiff = Math.abs(event.point.x - prevSeg.point.x);
		var yDiff = Math.abs(event.point.y - prevSeg.point.y);
		var minDiff = Math.min(xDiff, yDiff);
		if (xDiff < yDiff) {
			curSeg.point.x = prevSeg.point.x;
			curSeg.point.y = event.point.y;
		} else {
			curSeg.point.x = event.point.x;
			curSeg.point.y = prevSeg.point.y;
		}
		if (minDiff > tolerance) {
			prevSeg = curSeg;
			path.segments.add(curSeg);
			curSeg = path.segments.last;
		}
	}
}