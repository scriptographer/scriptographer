var components = {
	length: { value: 220, label: 'Length' },
	radius: { value: 130, label: 'Radius' },
	position: { value: 221, label: 'Position' },
	num: { value: 16, label: 'Number of passes' }
};

var values = Dialog.prompt('Enter Spirograph Values', components);

if (values) {
	var path = new Path();
	for (var i = 0; i <= 360 * values.num; i++) {
		var theta = i.toRadians();
		path.add(new Point(
			values.length * Math.cos(theta) - values.position * Math.cos(values.length * theta / values.radius),
			values.length * Math.sin(theta) - values.position * Math.sin(values.length * theta / values.radius)
		));
	}
	path.pointsToCurves();
}