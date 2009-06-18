var accel = 10;
var accelFriction = 0.8;
var friction = 0.65;
tool.eventInterval = 1000 / 100; // 100 times a second

function onOptions() {
	var values = Dialog.prompt('Friction:', [
		{ value: friction, description: 'friction', width: 50 },
		{ value: accel, description: 'acceleration', width: 50 },
		{ value: accelFriction, description: 'acceleration friction', width: 50 }
	]);
	if (values) {
		friction = values[0];
		accel = values[1];
		accelFriction = values[2];
	}
}

var point, velocity, acceleration, path, paths;
function onMouseDown(event) {
	point = event.point;
	velocity = new Point();
	acceleration = new Point();
	path = new Path();
	path.moveTo(event.point);
	paths = [];
	paths.push(path);
}

function onMouseUp(event) {
	path.pointsToCurves();
	path = new Path();
	// concatenate smoothed paths
	for(var i = 0; i < paths.length; i++) {
		path.segments.addAll(paths[i].segments);
		paths[i].remove();
	}
}

function onMouseDrag(event) {
	var diff = event.point - point;
	var norm = diff.normalize();
	acceleration = (acceleration + (norm * accel)) * (accelFriction);
	velocity = (velocity + acceleration) * friction;
	point += velocity;
	path.lineTo(point);
	if (path.length > 400) {
		path.pointsToCurves();
		path = new Path();
		path.segments.add(point);
		paths.push(path);
	}
}