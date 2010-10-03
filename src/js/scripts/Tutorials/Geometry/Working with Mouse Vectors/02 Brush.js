////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/geometry/working-with-mouse-vectors/#using-event.delta

tool.minDistance = 15;

var path;

function onMouseDown(event) {
	path = new Path();
	path.strokeColor = '#00000';
	path.selected = true;

	path.add(event.point);
}

function onMouseDrag(event) {
	path.add(event.point);

	var step = event.delta;
	step.angle += 90;

	var top = event.middlePoint + step;
	var bottom = event.middlePoint - step;
	
	var line = new Path();
	line.strokeColor = '#000000';
	line.add(top);
	line.add(bottom);
}