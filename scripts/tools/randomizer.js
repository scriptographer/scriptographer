var size = 50;
var path;

function onOptions() {
	var values = Dialog.prompt('Randomizer:', [
		{ description: 'Radius', type: 'range', value: size, width: 200, min: 0, max: 1000, step: 0.5 }
	]);
	if (values)
		size = values[0];
}

function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
}

function onMouseUp(event) {
	//path.pointsToCurves(25, 10, 10.0, 10.0);
}

function onMouseDrag(event) {
	var point = event.point;
	path.curveTo(
		point + [size * (Math.random() - 0.5), size * (Math.random() - 0.5)],
		point + [size * (Math.random() - 0.5), size * (Math.random() - 0.5)],
		point + [size * (Math.random() - 0.5), size * (Math.random() - 0.5)]
	);
}