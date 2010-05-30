var components = {
	length: { value: 220, label: 'Length' },
	radius: { value: 130, label: 'Radius' },
	position: { value: 221, label: 'Position' },
	num: { value: 16, label: 'Number of passes' }
};

var values = Dialog.prompt('Enter Spirograph Values', components);

if (values) {
	var step = (1).toRadians();
	var theta = 0;

	var path = new Path();
	var i = 0;
	while(i <= 360 * values.num) {
		path.add(new Point(
			values.length * Math.cos(theta) - values.position * Math.cos(values.length * theta / values.radius),
			values.length * Math.sin(theta) - values.position * Math.sin(values.length * theta / values.radius)
		));
		theta += step;
		i++;
	}
	path.pointsToCurves();
}