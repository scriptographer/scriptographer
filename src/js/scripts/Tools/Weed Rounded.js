////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	size: 10,
	minAmount: 5,
	maxAmount: 15,
	minWidth: 0.1,
	maxWidth: 5
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	size: { label: 'Radius' },
	minAmount: {
		label: 'Minimal Amount',
		min: 0
	},
	maxAmount: {
		label: 'Maximal Amount',
		min: 0
	},
	minWidth: {
		label: 'Minimal Stroke Width',
		min: 0
	},
	maxWidth: {
		label: 'Maximal Stroke Width',
		min: 0
	}
};

var palette = new Palette('Weed Rounded', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

tool.eventInterval = 1000 / 100; // 100 times a second

var branches;

function onMouseDown(event) {
	branches = [];
	var count = Math.rand(values.minAmount, values.maxAmount);
	var group = new Group();
	for (var i = 0; i < count; i++)
		branches.push(new Branch(event.point, group));
}

function onMouseUp(event) {
	for (var i in branches)
		branches[i].finish();
}

function onMouseDrag(event) {
	for (var i in branches)
		branches[i].grow();
}

// Branch:
function Branch(point, group) {
	this.point = point;
	this.vector = new Point(1, 0).rotate(Math.random() * 2 * Math.PI);
	this.path = new Path();
	this.path.add(point);
	this.path.strokeWidth = values.minWidth + Math.random() * (values.maxWidth - values.minWidth);
	group.appendTop(this.path);
	this.rotate = 0.2;
	this.count = 0;
	this.max = 0;
}

Branch.prototype.grow = function() {
	if (this.count++ < this.max) {
		this.vector = this.vector.rotate(this.rotate);
	} else {
		this.vector.length = (1.0 - Math.random() * 0.5) * values.size;
		this.max = Math.round(Math.random() * Math.PI * 2.0 / Math.abs(this.rotate));
		this.rotate *= -1;
		this.count = 0;
	}
	this.point += this.vector;
	this.path.add(this.point);
};

Branch.prototype.finish = function() {
	this.path.pointsToCurves();
};