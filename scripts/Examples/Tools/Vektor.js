////////////////////////////////////////////////////////////////////////////////
// Interface

var values = {
};

var components = {
	ruler0: { label: 'Vector', type: 'ruler' },
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
	ruler1: { label: 'Point', type: 'ruler' },
	x: {
		label: 'X', type: 'number', units: 'point', length: 10
	},
	y: {
		label: 'Y', type: 'number', units: 'point', length: 10
	},
	ruler2: { label: 'Instructions', type: 'ruler' },
	instructions: {
		type: 'text', fullSize: true,
		value: 'SHIFT = Add Vector\nALT = Modify Vector'
	}
}

var palette = new Palette('Vektor', components, values);

palette.onChange = function(component) {
	var name = component.name, value = component.value;
	if (name == 'angle')
		value = value.toRadians(0);
	// Update Vector
	vector[name] = value;
	drawVector();
}

////////////////////////////////////////////////////////////////////////////////
// Vector

var vectorStart, vector, vectorItem;

function processVector(end) {
	var previous = vector;
	vector = end - vectorStart;
	if (values.fixLength)
		vector.length = previous.length;
	if (values.fixAngle)
		vector.angle = previous.angle;
	drawVector();
}

function drawVector() {
	if (vectorItem)
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
	vectorItem.style = {
		strokeWidth: 0.75,
		strokeColor: '#e4141b',
		dashArray: [],
		fillColor: null
	};
	// Update palette
	values.x = vector.x;
	values.y = vector.y;
	values.length = vector.length;
	values.angle = vector.angle.toDegrees();
//	palette.update();
}

////////////////////////////////////////////////////////////////////////////////
// Mouse Handling

var dashItem;

function onMouseDown(event) {
	var end = vectorStart + vector;
	var create = true;
	if (event.modifiers.shift && vectorItem) {
		vectorStart = end;
	} else if (event.modifiers.option || end && end.getDistance(event.point) < 10) {
		create = false;
	} else {
		vectorStart = event.point;
	}
	if (create) {
		dashItem = vectorItem;
		vectorItem = null;
	}
	processVector(event.point);
	document.redraw();
}

function onMouseDrag(event) {
	if (values.fixLength && values.fixAngle)
		vectorStart = event.point;
	processVector(event.point);
}

function onMouseUp(event) {
	if (dashItem) {
		dashItem.dashArray = [1, 2];
		dashItem = null;
	}
}
