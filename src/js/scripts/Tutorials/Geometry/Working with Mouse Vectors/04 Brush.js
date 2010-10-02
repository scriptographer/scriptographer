// http://scriptographer.org/tutorials/geometry/working-with-mouse-vectors/#smoothing-and-coloring-the-body

tool.minDistance = 15;
tool.maxDistance = 45;

var path;

function onMouseDown(event) {
	path = new Path();
	path.fillColor = document.swatches.pick().color;

	path.add(event.point);
}

function onMouseDrag(event) {
	var step = event.delta / 2;
	step.angle += 90;
	
	var top = event.middlePoint + step;
	var bottom = event.middlePoint - step;
	
	path.add(top);
	path.insert(0, bottom);
	path.smooth();
}

function onMouseUp(event) {
	path.add(event.point);
	path.closed = true;
	path.smooth();
}