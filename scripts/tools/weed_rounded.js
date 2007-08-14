function onInit() {
	size = 10;
	minAmount = 5;
	maxAmount = 15;
	minWidth = 0.1;
	maxWidth = 5;
	setIdleEventInterval(1000 / 100); // 100 times a second
}

function onOptions() {
	values = Dialog.prompt("Weed Rounded:", [
		{ value: size, description: "Radius", width: 50 },
		{ value: minAmount, description: "Minimal Amount", width: 50 },
		{ value: maxAmount, description: "Maximal Amount", width: 50 },
		{ value: minWidth, description: "Minimal Stroke Width", width: 50 },
		{ value: maxWidth, description: "Maximal Stroke Width", width: 50 },
	]);
	if (values != null) {
		size = values[0];
		minAmount = values[1];
		maxAmount = values[2];
		minWidth = values[3];
		maxWidth = values[4];
	}
}

function onMouseDown(event) {
	branches = [];
	var count = Math.floor(minAmount + Math.random() * (maxAmount - minAmount));
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
	this.vector = new Point(1, 0).transform(new Matrix().rotate(Math.random() * 2 * Math.PI));
	this.path = new Path();
	this.path.moveTo(point);
	this.path.style.stroke.width = minWidth + Math.random() * (maxWidth - minWidth);
	group.appendChild(this.path);
	this.rotate = 0.2;
	this.count = 0;
	this.max = 0;
}

Branch.prototype.grow = function() {
	if (this.count++ < this.max) {
		this.vector = this.vector.transform(new Matrix().rotate(this.rotate));
	} else {
		this.vector = this.vector.normalize().multiply((1.0 - Math.random() * 0.5) * size);
		this.max = Math.round(Math.random() * Math.PI * 2.0 / Math.abs(this.rotate));
		this.rotate *= -1; //(Math.round(Math.random()) * 2 - 1);
		this.count = 0;
	}
	this.point = this.point.add(this.vector);
	this.path.lineTo(this.point);
}

Branch.prototype.finish = function() {
	this.path.pointsToCurves();
}