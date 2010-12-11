////////////////////////////////////////////////////////////////////////////////
// Values

tool.fixedDistance = 3;

var values = {
	distance: tool.fixedDistance,
	size: 10
};

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var path;
var side = 1;

function onMouseDown(event) {
	path = new Path();
}

function onMouseDrag(event) {
	var step = event.delta;
	step.angle += side * 90;
	step.length = values.size;
	path.add(event.point + step);
	side *= -1;
}

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	distance: {
		label: 'Distance', type: 'number',
		units: 'point', range: [1, 100],
		onChange: function(value) {
			tool.fixedDistance = value;
		}
	},
	size: {
		label: 'Size', type: 'number',
		units: 'point', range: [1, 100]
	}
};

var palette = new Palette('Stitch', components, values);
