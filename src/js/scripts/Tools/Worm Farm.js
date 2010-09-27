////////////////////////////////////////////////////////////////////////////////
// Values

var values  = {
	minDistance: 10,
	varyThickness: true
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = { 
	minDistance: {
		type: 'number', label: 'Size',
		units: 'point',
		range: [0, 1000],
		onChange: function(value) {
			tool.minDistance = value;
		} 
	},
	varyThickness: {
		type: 'checkbox', label: 'Vary Thickness'
	}
};

var palette = new Palette('Worm Farm', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

tool.minDistance = values.minDistance;

var worm;
var angle = (90).toRadians();

// Every time the user clicks the mouse to drag we create a path
// and when a user drags the mouse we add points to it
function onMouseDown(event) {
	worm = new Path();
	worm.fillColor = '#ffffff';
	worm.strokeColor = '#000000';
	worm.add(event.point);
}

function onMouseDrag(event) {
	// the vector in the direction that the mouse moved
	var step = event.delta;

	// if the vary thickness checkbox is marked
	// divide the length of the step vector by two:
	if (values.varyThickness) {
		step.length = step.length / 2;
	} else {
		// otherwise set the length of the step vector to half of minDistance
		step.length = values.minDistance / 2;
	}
	
	// the top point: the middle point + the step rotated by -90 degrees
	//   -----*
	//   |
	//   ------
	var top = event.middlePoint + step.rotate(-angle);

	// the bottom point: the middle point + the step rotated by 90 degrees
	//   ------
	//   |
	//   -----*
	var bottom = event.middlePoint + step.rotate(angle);
	
	// add the top point to the end of the path
	worm.add(top);
	
	// insert the bottom point in the beginning of the path
	worm.insert(0, bottom);
	
	// make a new line path from top to bottom
	var line = new Path.Line(top, bottom);
	line.strokeColor = '#000000';
	
	// smooth the segments of the path
	worm.smooth();
}

function onMouseUp(event) {
	worm.closed = true;
	worm.add(event.point);
	worm.smooth();
}