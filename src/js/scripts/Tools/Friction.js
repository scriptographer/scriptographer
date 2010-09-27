////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	accel: 10,
	accelFriction: 0.8,
	friction: 0.35
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	accel: {
		label: 'Acceleration',
		type: 'slider',
		range: [0.5, 20]
	},
	friction: {
		label: 'Friction',
		type: 'slider',
		range: [0, 0.99]
	},
	accelFriction: {
		label: 'Acceleration Friction',
		type: 'slider',
		range: [0.4, 1]
	}
};

var palette = new Palette('Friction', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

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
	var vector = event.point - point;
	vector.length = values.accel;
	acceleration = (acceleration + vector) * values.accelFriction;
	velocity = (velocity + acceleration) * (1 - values.friction);
	point += velocity;
	path.add(point);
	path.smooth();
}