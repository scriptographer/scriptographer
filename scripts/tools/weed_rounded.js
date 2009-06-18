var size = 10;
var minAmount = 5;
var maxAmount = 15;
var minWidth = 0.1;
var maxWidth = 5;
tool.eventInterval = 1000 / 100; // 100 times a second

function onOptions() {
	var values = Dialog.prompt('Weed Rounded:', [
		{ value: size, description: 'Radius', width: 50 },
		{ value: minAmount, description: 'Minimal Amount', width: 50 },
		{ value: maxAmount, description: 'Maximal Amount', width: 50 },
		{ value: minWidth, description: 'Minimal Stroke Width', width: 50 },
		{ value: maxWidth, description: 'Maximal Stroke Width', width: 50 },
	]);
	if (values != null) {
		size = values[0];
		minAmount = values[1];
		maxAmount = values[2];
		minWidth = values[3];
		maxWidth = values[4];
	}
}

var branches;

function onMouseDown(event) {
	branches = [];
	var count = Math.rand(minAmount, maxAmount);
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
	this.path.moveTo(point);
	this.path.strokeColor.width = minWidth + Math.random() * (maxWidth - minWidth);
	group.appendChild(this.path);
	this.rotate = 0.2;
	this.count = 0;
	this.max = 0;
}

Branch.prototype.grow = function() {
	if (this.count++ < this.max) {
		this.vector = this.vector.rotate(this.rotate);
	} else {
		this.vector = this.vector.normalize() * ((1.0 - Math.random() * 0.5) * size);
		this.max = Math.round(Math.random() * Math.PI * 2.0 / Math.abs(this.rotate));
		this.rotate *= -1;
		this.count = 0;
	}
	this.point += this.vector;
	this.path.lineTo(this.point);
}

Branch.prototype.finish = function() {
	this.path.pointsToCurves();
}