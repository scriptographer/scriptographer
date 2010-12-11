////////////////////////////////////////////////////////////////////////////////
// Values

tool.minDistance = 20;

var values = {
	flip: false,
	fixed: false
};

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var path;
function onMouseDown(event) {
	path = new Path() {
		strokeJoin: 'round',
		strokeCap: 'round'
	};
	path.add(event.point);
}

function onMouseDrag(event) {
	if (values.flip) {
		// Look at whether event.count is even to decide if we draw an arc
		// in clockwise or counter-clockwise direction.
		path.arcTo(event.point, event.count.isEven());
	} else {
		path.arcTo(event.point);
	}
}

////////////////////////////////////////////////////////////////////////////////
// Interface

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
		type: 'boolean'
	}
};

new Palette('Clouds', components, values);
