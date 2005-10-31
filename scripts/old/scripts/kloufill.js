var obj = new ArtSet().getMatching({type: "path", selected: true})[0];
if (obj != null) {
    values = prompt("Kloufil:",
{type: "number", value: 10, title: "dist 1", width: 50},
{type: "number", value: 100, title: "dist 2", width: 50}
);
    var dist1 = values[0];
    var dist2 = values[1];
    var art = new Art("path");
    obj = obj.clone();
    obj.curvesToPoints(dist1, 100000);
    var count = obj.segments.length;
    for (var i = 0; i < count; i++) {
        art.segments.push(obj.segments[i].point);
        var i2 = i + dist2;
        while (i2 >= count) i2 -= count;
        art.segments.push(obj.segments[i2].point);
    }
    art.segments.push(obj.segments[0].point);
    obj.remove();
}