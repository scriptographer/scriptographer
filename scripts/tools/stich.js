var dist = 3;
var size = 10;

function onOptions() {
	var values = Dialog.prompt('Stich:', [
		{ value: dist, description: 'Distance', width: 50 },
		{ value: size, description: 'Size', width: 50 }
	]);
	dist = values[0];
	size = values[1];
}

var mul, path, res;
function onMouseDown(event) {
	mul = 1;
	path = new Path();
	res = new Path();
}

function onMouseDrag(event) {
	path.segments.add(event.point);
	if (path.length > 10) {
		path.pointsToCurves();
		path.curvesToPoints(dist, 10000);
		for (var i = 0, j = path.curves.length; i < j; i++) {
			var bezier = path.curves[i];
			var pt = bezier.getPoint(0);
			var n = bezier.getNormal(1);
			if (n.x != 0 || n.y != 0) {
				n = n.normalize(size);
				res.segments.add(pt + (n * mul));
				mul *= -1;
			}
		}
		path.remove();
		path = new Path();
		path.lineTo(event.point);
	}
}