////////////////////////////////////////////////////////////////////////////////
// Values

var values = {
	minScale: 0.2,
	maxScale: 0.8,
	rotation: 0.5,
	minBranch: 4,
	maxBranch: 6
};

////////////////////////////////////////////////////////////////////////////////
// Interface

var components = {
	minScale: {
		label: 'Min Scale',
		type: 'slider',
		range: [0, 1],
		onChange: function(value) {
			if (value > values.maxScale)
				values.maxScale = value;
		}
	},
	maxScale: {
		label: 'Max Scale',
		type: 'slider',
		range: [0, 1],
		onChange: function(value) {
			if (value < values.minScale)
				values.minScale = value;
		}
	},
	rotation: {
		label: 'Rotation',
		type: 'slider',
		range: [0, 1]
	},
	minBranch: {
		label: 'Min Branches',
		min: 0,
		onChange: function(value) {
			if (value > values.maxBranch)
				values.maxBranch = value;
		}
	},
	maxBranch: {
		label: 'Max Branches',
		min: 0,
		onChange: function(value) {
			if (value < values.minBranch)
				values.minBranch = value;
		}
	}
};

var palette = new Palette('Tree', components, values);

////////////////////////////////////////////////////////////////////////////////
// Mouse handling

var path;
function onMouseDown(event) {
	path = new Path();
	path.add(event.point);
}

function onMouseDrag(event) {
	path.add(event.point);
}

function onMouseUp(event) {
	if (path.segments.length > 1) {
		path.pointsToCurves();
		var group = new Group([path]);
		var branches = [{
			path: path, scale: 1.0, rotation: 0
		}];
		var count = 0;
		while (branches.length > 0) {
			var newBranches = [];
			for (var i in branches) {
				var branch = branches[i];
				if (branch.scale > 0.2) {
					var curPath = branch.path;
					var prevEndPoint = curPath.segments.last.point;
					var newCount = Math.rand(values.minBranch, values.maxBranch);
					for (var j = 0; j < newCount; j++) {
						var newPath = path.clone();
						var scale = branch.scale * (Math.random() * (values.maxScale - values.minScale) + values.minScale);
						var rotation = branch.rotation + (Math.random() - 0.5) * Math.PI * values.rotation;
						newPath.scale(scale);
						var curStartPoint = newPath.segments.first.point;
						newPath.position += prevEndPoint - curStartPoint;
						newPath.rotate(rotation, curStartPoint);

						group.appendTop(newPath);
						newBranches.push({
							path: newPath,
							scale: scale,
							rotation: rotation
						});

						count++;
						if (count > 1000) {
							newBranches = [];
							break;
						}
					}
				}
			}
			branches = newBranches;
		}
	} else {
		path.remove();
	}
}