var path;
tool.distanceThreshold = 20;

function onMouseDown(event) {
	path = new Path() {
		strokeJoin: 'round',
		strokeCap: 'round'
	};
	path.moveTo(event.point);
}

function onMouseDrag(event) {
	var center = event.point - event.delta / 2;
	var vector = (event.delta / 2).rotate((90).toRadians());
	var circlePoint = center + vector;
	path.arcTo(circlePoint, event.point);
}

function onOptions() {
	var values = Dialog.prompt('Clouds', {
		size: { description: 'Size', value: tool.distanceThreshold }
	});
	
	if(values)
		tool.distanceThreshold = values.size;
}