var values = { scale: 0.9 };

function onOptions() {
	values = Dialog.prompt('Grow:', {
		scale: { description: 'Scale' }
	}, values);
}

var path;
function onMouseDown(event) {
	path = new Path();
	path.moveTo(event.point);
}

function onMouseUp(event) {
	path.pointsToCurves();
	var group = new Group([path]);
	var count = 0;
	while (count++ < 100) {
		var lastB = path.curves.last;
		var p2 = lastB.getPoint(1);
		var a2 = lastB.getTangent(1).angle;
		var obj = path.clone();
		obj.scale(values.scale);
		group.appendChild(obj);
		if (obj.bounds.width < 1 && obj.bounds.height < 1)
			break;
		var firstB = obj.curves[0];
		var p1 = firstB.getPoint(0);
		var a1 = firstB.getTangent(0).angle;
		obj.rotate(a2 - a1, p1);
		obj.position += p2 - p1;
		path = obj;
	}
}

function onMouseDrag(event) {
	path.lineTo(event.point);
}