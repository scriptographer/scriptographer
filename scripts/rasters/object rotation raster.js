include("raster.js");

var size = 10;
var scale = 100;

function createDot(x, y, dot, radius) {
	if (radius > 0) {
		var art = dot.clone();
		art.transform(new Matrix()
			.translate(x * size, y * size)
			.scale(radius * scale)
			.rotate(radius * Math.PI * 2)
		); 
		return art;
	}
	return null;
}

if (initRaster()) {
	values = Dialog.prompt("Enter Raster Values:", [
		{ value: size, description: "Grid Size:", width: 50},
		{ value: scale, description: "Object Scale (%):", width: 50}
	]);
	if (values) {
		size = values[0]
		scale = values[1] / 100.0;
		executeRaster(createDot);
	}
}
