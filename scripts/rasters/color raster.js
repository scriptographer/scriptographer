var size = 10;
var raster = null, dot = null;
var sel = activeDocument.selectedItems;

for (var i = 0; i < sel.length; i++) {
	obj = sel[i];
	if (raster == null && obj instanceof Raster) raster = obj;
	else if (dot == null && !(obj instanceof Raster)) dot = obj;
	if (raster != null && dot != null) break;
}

function setColor(art, color) {
	if (art instanceof Path) {
		if (art.style.stroke.color) art.style.stroke.color = color;
		if (art.style.fill.color) art.style.fill.color = color;
	}
	var child = art.firstChild;
	while (child) {
		setColor(child, color);
		child = child.nextSibling;
	}
}

function createDot(x, y, dot, color) {
	var art = dot.clone();
	setColor(art, color);
	var m = new Matrix();
	m.translate(x * size, y * size);
	art.transform(m); 
	return art;
}

if (raster != null && dot != null) {
 	values = Dialog.prompt("Enter Raster Values:", [
		{ value: size, description: "Grid Size:", width: 50 }
	]);

	if (values) {
		activeDocument.deselectAll();
		size = values[0];

		var group = new Group();
		var white = new GrayColor(0);

		for (var y = 0; y < raster.height; y++) {
			for (var x = 0; x < raster.width; x++) {
				var col = raster.getPixel(x, y);
				if (!white.equals(col)) {
					group.appendChild(createDot(x, raster.height - y, dot, col));
				}
			}
			activeDocument.redraw();
		}
	}
}