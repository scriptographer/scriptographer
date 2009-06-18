var values = {
	distance: 3,
	size: 10
};

function onOptions() {
	values = Dialog.prompt('Stitch:', {
		distance: { description: 'Distance' },
		size: { description: 'Size' }
	}, values);
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
		path.curvesToPoints(values.distance, 10000);
		for (var i = 0, j = path.curves.length; i < j; i++) {
			var bezier = path.curves[i];
			var pt = bezier.getPoint(0);
			var n = bezier.getNormal(1);
			if (n.x != 0 || n.y != 0) {
				n = n.normalize(values.size);
				res.segments.add(pt + (n * mul));
				mul *= -1;
			}
		}
		path.remove();
		path = new Path();
		path.lineTo(event.point);
	}
}