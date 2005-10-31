var objects = getMatching({type: "path", selected: true});
if (objects != null) {
    values = prompt("Shadow:", 
    {type: "number", value: 225, title: "Angle:", width: 50},
    {type: "number", value: 4, title: "Length:", width: 50},
    {type: "number", value: 100, title: "Scale:", width: 50}
    );

    if (values != null) {
        var angle = values[0] * Math.PI / 180.0;
        var length = values[1];
        var scale = values[2] / 100.0;
        var matrix = new Matrix().scale(scale).translate(new Point(0, length).transform(new Matrix().rotate(angle)));
        var set = new ArtSet();
        for (var i = 0; i < objects.length; i++) {
            var obj = objects[i];
            var obj1 = obj.clone();
//            obj1.curvesToPoints(5, 100000);
            var obj2 = obj1.clone();
            obj2.transform(matrix);
            length = obj1.segments.length;
            for (var j = 0; j < length; j++) {
                var k = j + 1;
                if (k == length) k = 0;
                var shadow = new Art("path");
                shadow.segments.push(obj1.segments[j].point);
                shadow.segments.push(obj1.segments[k].point);
                shadow.segments.push(obj2.segments[k].point);
                shadow.segments.push(obj2.segments[j].point);
                set.add(shadow);
            }
            obj1.remove();
            obj2.remove();
        }
        set.pathfinderUnite();
        for (var i = 0; i < set.length; i++) {
            var obj = set[i];
            // obj.pointsToCurves(1.0, 1.0, 1, 0.1);
        }        
    }
}