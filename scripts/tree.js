function onInit() {
    minScale = 0.2;
    maxScale = 0.8;
    rotationValue = 0.5;
    minBranch = 4;
    maxBranch = 6;
}

function onOptions() {
	/*
	var values = Dialog.prompt("Tree:", [
		new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "Minimal Scale", minScale),
		new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "Maximal Scale", maxScale),
		new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "Rotation", rotationValue),
		new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "Minimal Branch Number", minBranch),
		new Dialog.PromptItem(Dialog.PromptItem.TYPE_NUMBER, "Maximal Branch Number", maxBranch)
	]);
	*/

	print(minScale);
//	var values = Dialog.prompt("Tree:", [
    var values = Dialog["prompt(java.lang.String,org.mozilla.javascript.NativeArray)"]("Tree:", [
        { value: minScale, description: "Minimal Scale", width: 50 },
        { value: maxScale, description: "Maximal Scale", width: 50 },
        { value: rotationValue, description: "Rotation", width: 50 },
        { value: minBranch, description: "Minimal Branch Number", width: 50 },
        { value: maxBranch, description: "Maximal Branch Number", width: 50 }
	]);

    if (values != null) {
        minScale = values[0];
        maxScale = values[1];
        rotationValue = values[2];
        minBranch = values[3];
        maxBranch = values[4];
        if (minScale > maxScale) maxScale = minScale;
        if (minBranch > maxBranch) maxBranch = minBranch;
    }
}

function onMouseDown(event) {
    path = new Path();
}

function onMouseUp(event) {
	var t = new Date().getTime();
	if (path.segments.length > 0) {
	/*
		var bez = path.segments[path.segments.length - 2].curve;
		var pt = bez.getPoint(1);
		var obj = new Path();
		obj.segments.push(pt);
		obj.segments.push(pt.add(bez.getTangent(0.99).normalize(200.0)));
	*/
		
		path.pointsToCurves();
		var group = new Group();
		group.append(path);
		var branches = [ { path: path, scale: 1.0, rotation: 0 } ];
		var count = 0;
		while (branches.length != 0) {
			var newBranches = [];
			for (var i in branches) {
				var branch = branches[i];
				if (branch.scale > 0.2) {
					var curPath = branch.path;
					var prevEndPoint = curPath.segments[curPath.segments.length - 1].point;
					var newCount = Math.round(Math.random() * (maxBranch - minBranch) + minBranch);
					for (var j = 0; j < newCount; j++) {
						var newPath = path.clone();
						var scale = branch.scale * (Math.random() * (maxScale - minScale) + minScale);
						var rotation = branch.rotation + (Math.random() - 0.5) * Math.PI * rotationValue;
						
						newPath.transform(Matrix.getScaleInstance(scale));
						var curStartPoint = newPath.segments[0].point;
						var matrix = Matrix.getTranslateInstance(prevEndPoint.subtract(curStartPoint));
						matrix.rotate(rotation, curStartPoint.subtract(newPath.bounds.center));

						newPath.transform(matrix);

						group.append(newPath);
						newBranches.push( { path: newPath, scale: scale, rotation: rotation } );

						count++;
						if (count > 1000) {
							newBranches = [];
							break;
						}
					}
				}
			}
			branches  = newBranches;
		}
	}
	print(new Date().getTime() - t);
}

function onMouseDrag(event) {
	path.segments.add(new Segment(event.point));
}