//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = {
	curviness: 0.5,
	distance: 10,
	offset: 10,
	mouseOffset: true
};

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

tool.distanceThreshold = values.distance;

var components = {
	curviness: {
		label: 'Curviness', type: 'slider',
		range: [0, 1], width: 100
	},
	distance: {
		label: 'Distance threshold', type: 'number',
		steppers: true,
		onChange: function(value) {
			tool.distanceThreshold = value;
		}
	},
	mouseOffset: {
		label: 'Dynamic size', type: 'checkbox',
		onChange: function(checked) {
			palette.components.offset.enabled = !checked;
		}
	},
	offset: {
		label: 'Size', type: 'number',
		enabled: false
	}
};

var palette = new Palette('Wave:', components, values);

var path;
function onMouseDown(event) {
	path = new Path();
}

function onMouseDrag(event) {
	var rotation = event.count.isEven() ? 90 : -90;
	
	var step = event.delta.clone();
	
	if(!values.mouseOffset)
		step.length = values.offset;

	var vector = step.rotate(rotation.toRadians());
	
	var segment = new Segment() {
		point: event.point + vector,
		handleIn: -event.delta * values.curviness,
		handleOut: event.delta * values.curviness
	};
	path.add(segment);
}