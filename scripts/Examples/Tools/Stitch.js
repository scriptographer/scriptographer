//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = {
	distance: 3,
	size: 10
};

var components = {
	distance: { label: 'Distance' },
	size: { label: 'Size' }
};

var palette = new Palette('Stitch', components, values);

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

var mul, path, res;
function onMouseDown(event) {
	mul = 1;
	path = new Path();
	path.add(event.point);
	res = new Path();
}

function onMouseDrag(event) {
	path.add(event.point);
	if (path.length > 10) {
		path.pointsToCurves();
		path.curvesToPoints(values.distance, 10000);
		for (var i = 0, j = path.curves.length; i < j; i++) {
			var bezier = path.curves[i];
			var pt = bezier.getPoint(0);
			var n = bezier.getNormal(1);
			if (n.x != 0 || n.y != 0) {
				n = n.normalize(values.size);
				res.add(pt + (n * mul));
				mul *= -1;
			}
		}
		path.remove();
		path = new Path();
		path.add(event.point);
	}
}