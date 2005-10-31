var size = 10, step = 1, f = 1;

var raster = new ArtSet().getMatching({type: "raster", selected: true})[0];
if (raster != null) {
    var group = new Art("group");

    var obj = new Art("rect", new Rect(0, 0, size, size));
    obj.curvesToPoints(step, 100000);

    function drawDot(x, y, darkness) {
        var art = new Art("path");
        var count = obj.segments.length;
        var dist = Math.round((f * size / step) * darkness);
        for (var i = 0; i < count; i++) {
            art.segments.push(obj.segments[i].point);
            var i2 = i + dist;
            while (i2 >= count) i2 -= count;
            art.segments.push(obj.segments[i2].point);
        }
        art.segments.push(obj.segments[0].point);
        var m = new Matrix();
        m.translate(x * size, y * size);
        art.transform(m); 
        return art;
    }

    for (var i = 0; i < raster.width; i++) {
        for (var j = 0; j < raster.height; j++) {
            var col = raster.getPixel(i, j);
            col.type = "gray";
            group.append(drawDot(i, raster.height - j, col.gray));
        }
        documents[0].redraw();
    }
    obj.remove();
}