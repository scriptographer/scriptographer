function extractChildren(obj) {
    var children = obj.children;
    for (var i = children.length - 1; i >= 0; i--) {   
        children[i].moveBefore(obj);
    }
    obj.remove();
}

// first break apart the compoundPaths:
var objects = getMatching({type: "compoundPath", selected: true});
for (var i = 0; i < objects.length; i++) {
    var obj = objects[i];
    extractChildren(obj);
}

// the same with groups:
var objects = getMatching({type: "group", selected: true});
for (var i = 0; i < objects.length; i++) {
    var obj = objects[i];
    extractChildren(obj);
}

// replace rasters by boxes:
var objects = getMatching({type: "raster", selected: true});
for (var i = 0; i < objects.length; i++) {
    var obj = objects[i];
    var rect = new Rect(0, 0, obj.height, obj.width);
    var rectObj = new Art("rect", rect);
    rectObj.transform(obj.matrix);
    rectObj.selected = true;
    rectObj.transform(new Matrix().translate(obj.bounds.topLeft.sub(rectObj.bounds.topLeft)));
    obj.remove();
}

// create the center object:
var centerObj = new Art("group");
var line = new Art("path");
line.segments.push([-2,-2]);
line.segments.push([2,2]);
line.selected = true;
centerObj.append(line);
var line = new Art("path");
line.segments.push([2,-2]);
line.segments.push([-2,2]);
line.selected = true;
centerObj.append(line);
centerObj.selected = true;

// add the centers:
var objects = getMatching({type: "any", selected: true});
for (var i = 0; i < objects.length; i++) {
    var obj = objects[i];
    if (obj.centerVisible) {
        centerObj.clone().transform(new Matrix().translate(obj.bounds.center)); 
    }
}
// remove the center object
centerObj.remove();

// change the fill and line styles:
var objects = getMatching({type: "path", selected: true});
for (var i = 0; i < objects.length; i++) {
    var obj = objects[i];
    obj.style.fill.color = null;
    obj.style.stroke.color = [1];
    obj.style.stroke.width = 1;
    obj.style.stroke.cap = "butt"
    obj.style.stroke.join = "mitter";
}
