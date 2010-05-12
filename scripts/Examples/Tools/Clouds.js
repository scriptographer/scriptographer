//////////////////////////////////////////////////////////////////////////////
// Values:

tool.minDistance = 20;

var values = {
	flip: false,
	fixed: false
};

//////////////////////////////////////////////////////////////////////////////
// Interface:

var components = {
	minSize: {
		label: 'Minimum Size', value: tool.minDistance,
		min: 0,
		onChange: function(value) {
			tool.minDistance = value;
			if (values.fixed) {
				tool.maxDistance = value;
			} else {
				tool.maxDistance = null;
			}
		}
	},
	
	fixed: {
		label: 'Fixed Size',
		onChange: function(value) {
			if (value) {
				tool.maxDistance = tool.minDistance;
			} else {
				tool.maxDistance = null;
			}
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
	
	if (values.flip && event.count.isEven())
		vector = vector * -1;
	
	var circlePoint = event.middlePoint + vector;
	path.arcTo(circlePoint, event.point);
}