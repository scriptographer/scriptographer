//////////////////////////////////////////////////////////////////////////////
// Interface:

var values = {
	flip: false
};

var components = {
	size: {
		label: 'Minium Size', value: tool.distanceThreshold,
		onChange: function(value) {
			tool.distanceThreshold = value;
		}
	},
	flip: {
		label: 'Flip',
		type: 'checkbox'
	}
};

new Palette('Clouds', components, values);

//////////////////////////////////////////////////////////////////////////////
// Mouse handling:

tool.distanceThreshold = 20;

var path;
function onMouseDown(event) {
	path = new Path() {
		strokeJoin: 'round',
		strokeCap: 'round'
	};
	path.add(event.point);
}

function onMouseDrag(event) {
	var vector = (event.delta / 2).rotate((90).toRadians());
	
	if(values.flip && event.count.isEven())
		vector = vector * -1;
	
	var circlePoint = event.middlePoint + vector;
	path.arcTo(circlePoint, event.point);
}