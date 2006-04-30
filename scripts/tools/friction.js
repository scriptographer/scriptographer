function onInit() {
	accel = 10;
	accelFriction = 0.8;
	friction = 0.65;
	setIdleEventInterval(1000 / 100); // 100 times a second
}

function onOptions() {
	var values = Dialog.prompt("Friction:", [
		{ value: friction, description: "friction", width: 50 },
		{ value: accel, description: "acceleration", width: 50 },
		{ value: accelFriction, description: "acceleration friction", width: 50 }
	]);
	if (values) {
		friction = values[0];
		accel = values[1];
		accelFriction = values[2];
	}
}

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
	var diff = event.point.subtract(point);
	var norm = diff.normalize();
	var len = norm.getLength();
	acceleration = acceleration.add(norm.multiply(len * accel)).multiply(accelFriction);
	velocity = velocity.add(acceleration).multiply(friction);
	point = point.add(velocity);
	path.lineTo(point);
	if (path.getLength() > 400) {
		path.pointsToCurves();
		path = new Path();
		path.segments.add(point);
		paths.push(path);
	}
}