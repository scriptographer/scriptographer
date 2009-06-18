var values = Dialog.prompt('Enter Spirograph Values', {
	length: { value: 220, description: 'Length' },
	radius: { value: 130, description: 'Radius' },
	position: { value: 221, description: 'Position' },
	num: { value: 16, description: 'Number of passes' }
});

if (values) {
	var step = (1).toRadians();
	var theta = 0;

	var path = new Path();
	var i = 0;
	while(i <= 360 * values.num) {
		path.lineTo(new Point(
			values.length * Math.cos(theta) - values.position * Math.cos(values.length * theta / values.radius),
			values.length * Math.sin(theta) - values.position * Math.sin(values.length * theta / values.radius)
		));
		theta += step;
		i++;
	}
	path.pointsToCurves();
}