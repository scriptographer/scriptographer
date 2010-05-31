var hide = true;
var precision = 1;

var objects = [];
var velocity;
var path;
var timer;

function onMouseDown(event) {
	velocity = 0;
	path = new Path([event.point, event.point]);
}

function onMouseUp(event) {
	velocity = (event.point - event.downPoint).normalize(velocity);
	objects.push({ path: path, point: event.point, velocity: velocity });
	if (!timer)
		timer = nextStep.periodic(1);
}

function onMouseDrag(event) {
	velocity = (velocity + event.delta.length) * 0.8;
	path.segments.pop();
	path.add(event.point);
}

function nextStep() {
	for (var i = objects.length - 1; i >= 0; i--) {
		var obj = objects[i];
		var path = obj.path;
		if (!path.isValid()) {
			objects.splice(i, 1);
			continue;
		}
		var velocity = obj.velocity;
		path.segments.pop();
		var p1 = obj.point;
		var p2 = p1 + velocity;
		var length = velocity.length;
		var amount = Math.ceil(length / precision);
		if (amount > 0) {
			var vector = velocity.normalize(length / amount);
			// Hide path so it will be ignored in tests
			path.visible = !hide;
			for (var j = 0; j < amount; j++) {
				var result = document.hitTest(p1, 'all-except-fills', precision);
				if (result && result.curve) {
					var curve = result.curve;
					var normal = curve.getNormal(result.parameter);
					var tangent = curve.getTangent(result.parameter);
					p2 = result.point;
					velocity = -velocity;
					var angle = velocity.getDirectedAngle(tangent);
					velocity = velocity.rotate(Math.PI - 2 * angle);
					path.add(result.point);
					// Avoid another reflexion with the same object just
					// after being reflected:
					var next;
					do {
						p2 += velocity;
						next = document.hitTest(p2, 'all-except-fills',
								precision * 2);
					} while (next && next.item == result.item);
					break;
				}
				p1 += vector;
			}
			path.visible = true;
		}
		path.add(p2);
		obj.point = p2;
		obj.velocity = velocity;
	}
}
