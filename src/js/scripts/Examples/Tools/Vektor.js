////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	fixLength: false,
	fixAngle: false,
	showCircle: false,
	showAngleLength: false,
	showCoordinates: false
};

////////////////////////////////////////////////////////////////////////////////
// Vector

var vectorStart, vector, vectorPrevious;
var vectorItem, items, dashedItems;

function processVector(event, drag) {
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
	drawVector(drag);
}

function drawVector(drag) {
	if (items) {
		items.each(function(item) {
			item.remove();
		});
	}
	if (vectorItem)
		vectorItem.remove();
	items = [];
	var arrowVector = vector.normalize(10);
	var end = vectorStart + vector;
	vectorItem = new Group([
		new Path([vectorStart, end]),
		new Path([
			end + arrowVector.rotate(135),
			end,
			end + arrowVector.rotate(-135)
		])
	]);
	vectorItem.style = {
		strokeWidth: 0.75,
		strokeColor: '#e4141b',
		dashArray: [],
		fillColor: null
	};
	// Display:
	dashedItems = [];
	// Draw Circle
	if (values.showCircle) {
		dashedItems.push(new Path.Circle(vectorStart, vector.length));
	}
	// Draw Labels
	if (values.showAngleLength) {
		if (drawAngle(vectorStart, vector, !drag) && !drag) {
			drawLength(vectorStart, end, vector.angle < 0 ? -1 : 1, true);
		}
	}
	var quadrant = vector.quadrant;
	if (values.showCoordinates && !drag) {
		var lengthThreshold = 25;
		if (Math.abs(vector.x) >= lengthThreshold)
			drawLength(vectorStart, vectorStart + [vector.x, 0],
					[1, 3].contains(quadrant) ? -1 : 1, true, vector.x, 'x: ');
		if (Math.abs(vector.y) >= lengthThreshold)
			drawLength(vectorStart, vectorStart + [0, vector.y], 
					[1, 3].contains(quadrant) ? 1 : -1, true, vector.y, 'y: ');
	}
	dashedItems.each(function(item) {
		item.style = {
			strokeColor: '#000000',
			fillColor: null,
			dashArray: [1, 2]
		};
		items.push(item);
	});
	// Update palette
	values.x = vector.x;
	values.y = vector.y;
	values.length = vector.length;
	values.angle = vector.angle;
}

function drawAngle(center, vector, label) {
	var radius = 25, threshold = 10;
	if (vector.length > radius + threshold) {
		var from = new Point(radius, 0);
		var through = from.rotate(vector.angle / 2);
		var to = from.rotate(vector.angle);
		var end = center + to;
		dashedItems.push(new Path.Line(center,
				center + new Point(radius + threshold, 0)));
		dashedItems.push(new Path.Arc(center + from, center + through, end));
		var arrowVector = to.normalize(7.5).rotate(vector.angle < 0 ? -90 : 90);
		dashedItems.push(new Path([
				end + arrowVector.rotate(135),
				end,
				end + arrowVector.rotate(-135)
		]));
		if (label) {
			// Angle Label
			var text = new PointText(center
					+ through.normalize(radius + 10) + new Point(0, 3));
			text.content = vector.angle.format('0.###') + '\xb0';
			items.push(text);
		}
		return true;
	}
	return false;
}

function drawLength(from, to, sign, label, value, prefix) {
	var lengthSize = 5;
	var vector = to - from;
	var awayVector = vector.normalize(lengthSize).rotate(90 * sign);
	var upVector = vector.normalize(lengthSize).rotate(45 * sign);
	var downVector = upVector.rotate(-90 * sign);
	var lengthVector = vector.normalize(
			vector.length / 2 - lengthSize * Math.sqrt(2));
	var line = new Path();
	line.add(from + awayVector);
	line.lineBy(upVector);
	line.lineBy(lengthVector);
	line.lineBy(upVector);
	var middle = line.segments.last.point;
	line.lineBy(downVector);
	line.lineBy(lengthVector);
	line.lineBy(downVector);
	dashedItems.push(line);
	if (label) {
		// Length Label
		var textAngle = Math.abs(vector.angle) > 90
				? textAngle = 180 + vector.angle : vector.angle;
		// Label needs to move away by different amounts based on the
		// vector's quadrant:
		var away = (sign >= 0 ? [1, 4] : [2, 3]).contains(vector.quadrant)
				? 8 : 0;
		var text = new PointText(middle + awayVector.normalize(away + lengthSize));
		text.rotate(textAngle);
		text.paragraphStyle.justification = 'center';
		text.content = (prefix || '') + (value || vector.length).format('0.###');
		items.push(text);
	}
}

////////////////////////////////////////////////////////////////////////////////
// Mouse Handling

var dashItem;

function onMouseDown(event) {
	var end = vectorStart + vector;
	var create = false;
	if (event.modifiers.shift && vectorItem) {
		vectorStart = end;
		create = true;
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
	processVector(event, true);
}

function onMouseDrag(event) {
	if (!event.modifiers.shift && values.fixLength && values.fixAngle)
		vectorStart = event.point;
	processVector(event, true);
}

function onMouseUp(event) {
	processVector(event, false);
	if (dashItem) {
		dashItem.dashArray = [1, 2];
		dashItem = null;
	}
	vectorPrevious = vector;
}

////////////////////////////////////////////////////////////////////////////////
// Interface

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
	ruler1: { label: 'Coordinates', type: 'ruler' },
	x: {
		label: 'X', type: 'number', units: 'point', length: 10
	},
	y: {
		label: 'Y', type: 'number', units: 'point', length: 10
	},
	ruler2: { label: 'Display', type: 'ruler' },
	showAngleLength: {
		label: 'Vector', type: 'checkbox'
	},
	showCoordinates: {
		label: 'Coordinates', type: 'checkbox'
	},
	showCircle: {
		label: 'Circle', type: 'checkbox'
	},
	ruler3: { label: 'Instructions', type: 'ruler' },
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
		drawVector(false);
	}
}
