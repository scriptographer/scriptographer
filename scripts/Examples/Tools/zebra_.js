var values = {
	offset: 10,
	distance: 10,
	mouseOffset: false,
	speedScale: 1.5
};

function onOptions() {
	values = Dialog.prompt('Zebra:', {
		distance: { description: 'Distance threshold' },
		offset: { description: 'Size' },
		mouseOffset: { description: 'Define size by mouse speed', type: 'checkbox'},
		speedScale: { description: 'Mouse speed scaling factor'}
	}, values);
	tool.distanceThreshold = values.distance;
}

function onMouseDrag(event) {

	// the vector in the direction that the mouse moved
	var step = event.delta.clone();
	
	if (values.mouseOffset) {
		step *= values.speedScale;
	} else {
		// normalize step to a specific length
		step.length = values.offset;
	}
	
	// find the middle point between the last and the current position
	var middle = event.point - event.delta / 2;	
	
	// the top point: the middle point + the step rotated by -90 degrees	
	var top = middle + step.rotate((-90).toRadians());

	// the bottom point: the middle point + the step rotated by 90 degrees	
	var bottom = middle + step.rotate((90).toRadians());
	
	//now create a line using the top and bottom points
	new Path.Line(top, bottom);
}