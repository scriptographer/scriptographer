// http://scriptographer.org/tutorials/geometry/working-with-mouse-vectors/#using-the-skeleton-points-to-make-a-body-around-our-line

tool.minDistance = 15;
tool.maxDistance = 45;

var path;

function onMouseDown(event) {
	path = new Path();
	path.strokeColor = '#00000';
	path.selected = true;

	path.add(event.point);
}

function onMouseDrag(event) {
	var step = event.delta;
	step.angle += 90;

	var top = event.middlePoint + step;
	var bottom = event.middlePoint - step;
	
	var line = new Path();
	line.strokeColor = '#000000';
	line.add(top);
	line.add(bottom);

	path.add(top);
	path.insert(0, bottom);
}