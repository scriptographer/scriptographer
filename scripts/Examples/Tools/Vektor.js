////////////////////////////////////////////////////////////////////////////////
// Interface

var values = {
};

var components = {
	length: {
		label: 'Length', type: 'number', units: 'point', length: 10
	},
	fixLength: {
		label: 'Fix', type: 'checkbox'
	},
	angle: {
		label: 'Angle', type: 'number', units: 'degree', length: 10,
		range: [-360, 360]
	},
	fixAngle: {
		label: 'Fix', type: 'checkbox'
	},
	ruler: { type: 'ruler' },
	x: {
		label: 'X', type: 'number', units: 'point', length: 10
	},
	y: {
		label: 'Y', type: 'number', units: 'point', length: 10
	}
}

var palette = new Palette('Vektor', components, values);

palette.onChange = function(component) {
	var name = component.name, value = component.value;
	if (name == 'angle')
		value = value.toRadians(0);
	// Update Vector
	vector[name] = value;
	drawVector(true);
}

////////////////////////////////////////////////////////////////////////////////
// Vector

var vectorStart = document.bounds.center;
var vector = new Point(100, 0);

var vectorItem = null;

function processVector(end) {
	var previous = vector;
	vector = end - vectorStart;
	if (values.fixLength)
		vector.length = previous.length;
	if (values.fixAngle)
		vector.angle = previous.angle;
	drawVector(true);
}

function drawVector(remove) {
	if (vectorItem && remove)
		vectorItem.remove();
	var arrowStep = vector.normalize(10);
	var end = vectorStart + vector;
	vectorItem = new Group([
		new Path([vectorStart, end]),
		new Path([
			end + arrowStep.rotate((135).toRadians()),
			end,
			end + arrowStep.rotate((-135).toRadians())
		])
	]);
	// Update palette
	values.x = vector.x;
	values.y = vector.y;
	values.length = vector.length;
	values.angle = vector.angle.toDegrees();
//	palette.update();
}


document.currentStyle = {
	strokeWidth: 0.75,
	strokeColor: '#e4141b',
	dashArray: [],
	fillColor: null
};

drawVector(false);

////////////////////////////////////////////////////////////////////////////////
// Mouse Handling

var dashItem = null;

function onMouseDown(event) {
	var end = vectorStart + vector;
	if (end.getDistance(event.point) > 10) {
		vectorStart = end;
		dashItem = vectorItem;
		vectorItem = null;
	}
	processVector(event.point);
}

function onMouseDrag(event) {
	processVector(event.point);
}

function onMouseUp(event) {
	print(dashItem);
	if (dashItem) {
		dashItem.strokeWidth = 2;
		dashItem.dashArray = [1, 2];
		dashItem = null;
	}
}
