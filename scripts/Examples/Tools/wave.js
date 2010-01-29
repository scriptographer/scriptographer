var values = {
	curviness: 0.5,
	distance: 10,
	offset: 10,
	mouseOffset: true
};

tool.distanceThreshold = values.distance;

function onOptions() {
	values = Dialog.prompt('Wave settings:', {
		curviness: { description: 'Curviness (between 0 and 1)' },
		distance: { description: 'Distance threshold' },
		offset: { description: 'Size' },
		mouseOffset: { description: 'Define size by mouse speed', type: 'checkbox'}
	}, values);
	tool.distanceThreshold = values.distance;
}

var path;

function onMouseDown(event) {
	path = new Path();
}

function onMouseDrag(event) {
	var rotation = event.count.isEven() ? 90 : -90;
	
	var step = event.delta.clone();
	
	if(!values.mouseOffset) {
		step.length = values.offset;
	}
	
	var vector = step.rotate(rotation.toDegrees());
	
	var segment = new Segment() {
		point: event.point + vector,
		handleIn: -event.delta * values.curviness,
		handleOut: event.delta * values.curviness
	};
	path.segments.push(segment);
}