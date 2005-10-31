var raster = null;
var dot = null;
var sel = null;

sel = getSelected();
for (var i = 0; i < sel.length; i++) {
    obj = sel[i];
    if (raster == null && obj.type == "raster") raster = obj;
    else if (dot == null && obj.type != "raster") dot = obj;
    if (raster != null && dot != null) break;
}

function setColor(art, color) {
    if (art.type == "path") {
        if (art.style.stroke.color != null) art.style.stroke.color = color;
        if (art.style.fill.color != null) art.style.fill.color = color;
    }
    var child = art.firstChild;
    while (child != null) {
        setColor(child, color);
        child = child.nextSibling;
    }
}

if (raster != null && dot != null) {
    values = prompt("Enter Raster Values:", 
        {type: "number", value: 10, title: "Grid Size:", width: 50}
    );

    if (values) {
        var size = values[0];


        function drawDot(x, y, dot, color) {
            var art = dot.clone();
            setColor(art, color);
            var m = new Matrix();
            m.translate(x * size, y * size);
            art.transform(m); 
            return art;
        }

        var group = new Art("group");
        var white = new Color(0);
        for (var i = 0; i < raster.width; i++) {
            for (var j = 0; j < raster.height; j++) {
                var col = raster.getPixel(i, j);
                if (!white.equals(col)) {
                    group.append(drawDot(i, raster.height - j, dot, col));
                }
            }
            if (i % 10 == 0) redraw();
        }
        for (var i = 0; i < sel.length; i++) {
            sel[i].selected = false;
        }
    }
}