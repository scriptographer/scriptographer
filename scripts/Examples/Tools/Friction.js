//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = {
	accel: 10,
	accelFriction: 0.8,
	friction: 0.65
};

var components = {
	friction: { label: 'Friction' },
	accel: { label: 'Acceleration' },
	accelFriction: { label: 'Acceleration Friction' }
};

var palette = new Palette('Friction', components, values);

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

tool.eventInterval = 1000 / 100; // 100 times a second

var point, velocity, acceleration, path;
function onMouseDown(event) {
	point = event.point;
	velocity = new Point();
	acceleration = new Point();
	path = new Path();
	path.add(event.point);
}

function onMouseDrag(event) {
	var diff = event.point - point;
	var norm = diff.normalize();
	acceleration = (acceleration + (norm * values.accel)) * values.accelFriction;
	velocity = (velocity + acceleration) * values.friction;
	point += velocity;
	path.add(point);
	path.smooth();
}