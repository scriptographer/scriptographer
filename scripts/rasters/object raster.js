include("raster.js");

function createDot(x, y, dot, radius) {
	if (radius > 0.1) {
		var art = dot.clone();
		var m = new Matrix();
		m.translate(x * size, y * size);
		m.scale(radius * scale);
		art.transform(m); 
		return art;
	}
}

if (initRaster()) {
	values = Dialog.prompt("Enter Raster Values:", [
		{ value: 10, description: "Grid Size:", width: 40 },
		{ value: 100, description: "Object Scale (%):", width: 40 }
	]);
	if (values) {
		var size = values[0], scale = values[1] / 100.0;
		executeRaster(createDot);
	}
}
