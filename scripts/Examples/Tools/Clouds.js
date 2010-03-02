tool.distanceThreshold = 20;
var path;

function onMouseDown(event) {
	path = new Path() {
		strokeJoin: 'round',
		strokeCap: 'round'
	};
	path.moveTo(event.point);
}

function onMouseDrag(event) {
	var vector = (event.delta / 2).rotate((90).toRadians());
	var circlePoint = event.middlePoint + vector;
	path.arcTo(circlePoint, event.point);
}

function onOptions() {
	var values = Dialog.prompt('Clouds', {
		size: { description: 'Size', value: tool.distanceThreshold }
	});
	
	if(values)
		tool.distanceThreshold = values.size;
}