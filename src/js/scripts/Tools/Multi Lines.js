////////////////////////////////////////////////////////////////////////////////
// Values

tool.minDistance = 10;

var values = {
	lines: 6,
	size: 20,
	happy: false,
	smooth: true
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	lines: {
		label: 'Lines',
		range: [0, 100],
		min: 1,
		steppers: true
	},
	size: {
		label: 'Size',
		min: 0,
		steppers: true
	},
	smooth: { label: 'Smooth' },
	happy: { label: 'Happy' }
};

var palette = new Palette('Multi Lines', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var paths;
function onMouseDown(event) {
	paths = [];
	for (var i = 0; i < values.lines; i++) {
		var path = new Path();
		path.fillColor = null;
		path.closed = true;
		if (values.happy) {
			var swatch = document.swatches[(i + 4) % document.swatches.length];
			path.fillColor = swatch.color;
			path.strokeColor = null;
		} else {
			path.strokeColor = document.currentStyle.strokeColor || '#000000';
		}
		paths.push(path);
	}
}

function onMouseDrag(event) {
	
	var offset = event.delta;
	offset.angle += 90;

	var lineSize = values.size / values.lines;
	for (var i = 0; i < values.lines; i++) {
		var path = paths[values.lines - 1 - i];
		offset.length = (lineSize * i + lineSize / 2);
		path.add(event.middlePoint + offset);
		path.insert(0, event.middlePoint - offset);

		if (values.smooth)
			path.smooth();
	}
}