include('Raster.js');

function createDot(x, y, dots, radius) {
	if (radius > 0) {
		// Pick a dot at random:
		var item = dots[Math.floor(dots.length * Math.random())].clone();
		item.position += new Point(x, y) * values.size;
		item.rotate(radius * values.rotation * Math.PI / 180.0, item.position);
		item.scale((radius * radius * values.gradiation + radius * (1.0 - values.gradiation)) * values.scale);
		return item;
	}
}

if (initRaster()) {
	var components = {
		size: { value: 10, label: 'Grid Size'} ,
		scale: { value: 100, label: 'Object Scale (%)'},
		gradiation: { value: 0.5, label: 'Gradiation'},
		rotation: { value: 360.0, label: 'Rotation'}
	};
	var values = Dialog.prompt('Enter Raster Values:', components);
	if (values) {
		values.scale /= 100;
		executeRaster(createDot, true);
	}
}