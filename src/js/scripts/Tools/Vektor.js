script.coordinateSystem = 'top-down';
script.angleUnits = 'degrees';

////////////////////////////////////////////////////////////////////////////////
// Interface

var values = {
};

var components = {
//	ruler0: { label: 'As Vector', type: 'ruler' },
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
	ruler1: { label: 'Coordinates', type: 'ruler' },
	x: {
		label: 'X', type: 'number', units: 'point', length: 10
	},
	y: {
		label: 'Y', type: 'number', units: 'point', length: 10
	},
	ruler2: { label: 'Instructions', type: 'ruler' },
	instructions: {
		type: 'text', fullSize: true,
		value: 'SHIFT = Add New Vector\nALT = Modify Last Vector'
	}
}

var palette = new Palette('Vektor', components, values);

palette.onChange = function(component) {
	var name = component.name, value = component.value;
	if (vector) {
		// Update Vector
		vector[name] = value;
		drawVector();
	}
}

////////////////////////////////////////////////////////////////////////////////
// Vector

var vectorStart, vector, vectorPrevious, vectorItem;

function processVector(event) {
	vector = event.point - vectorStart;
	if (vectorPrevious) {
		if (values.fixLength && values.fixAngle) {
			vector = vectorPrevious;
		} else if (values.fixLength) {
			vector.length = vectorPrevious.length;
		} else if (values.fixAngle) {
			vector = vector.project(vectorPrevious);
		}
	}
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
			end + arrowStep.rotate(135),
			end,
			end + arrowStep.rotate(-135)
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
	values.angle = vector.angle;
}

////////////////////////////////////////////////////////////////////////////////
// Mouse Handling

var dashItem;

function onMouseDown(event) {
	var end = vectorStart + vector;
	var create = true;
	if (event.modifiers.shift && vectorItem) {
		vectorStart = end;
	} else if (vector && (event.modifiers.option
			|| end && end.getDistance(event.point) < 10)) {
		create = false;
	} else {
		vectorStart = event.point;
	}
	if (create) {
		dashItem = vectorItem;
		vectorItem = null;
	}
	processVector(event);
	document.redraw();
}

function onMouseDrag(event) {
	if (!event.modifiers.shift && values.fixLength && values.fixAngle)
		vectorStart = event.point;
	processVector(event);
}

function onMouseUp(event) {
	if (dashItem) {
		dashItem.dashArray = [1, 2];
		dashItem = null;
	}
	vectorPrevious = vector;
}
