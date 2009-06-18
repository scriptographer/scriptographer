var values = {
	accel: 10,
	accelFriction: 0.8,
	friction: 0.65
};

tool.eventInterval = 1000 / 100; // 100 times a second

function onOptions() {
	values = Dialog.prompt('Friction:', {
		friction: { description: 'friction' },
		accel: { description: 'acceleration' },
		accelFriction: { description: 'acceleration friction' }
	}, values);
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
	acceleration = (acceleration + (norm * values.accel)) * values.accelFriction;
	velocity = (velocity + acceleration) * values.friction;
	point += velocity;
	path.lineTo(point);
	if (path.length > 400) {
		path.pointsToCurves();
		path = new Path();
		path.segments.add(point);
		paths.push(path);
	}
}