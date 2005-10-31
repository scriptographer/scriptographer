function areaSign(a, b, c) {
  return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)
}

function compare(pt1, pt2, firstPt) {
    a = areaSign(firstPt, pt1, pt2);
    if (a > 0) return -1;
    else if (a < 0) return 1;
    else {
        dist1 = pt1.sub(firstPt);
        dist2 = pt2.sub(firstPt);
        prod1 = dist1.dotProduct(dist1);
        prod2 = dist2.dotProduct(dist2);
        if (prod1 < prod2) return -1;
        else if (prod1 > prod2) return 1;
        else return 0;
    }
}

function sort(points, initLow, initHigh) {
    if (initLow >= initHigh) return;
    low = initLow;
    high = initHigh - 1;
    while (low <= high) {
        while ((low <= high) && (low == initHigh || compare(points[low], points[initHigh], points[0]) != -1)) {
            low++;
        }
        while ((low <= high) && (high == initHigh || compare(points[high], points[initHigh], points[0]) != 1)) {
            high--;
        }
        if (low < high) {
            temp = points[high];
            points[high] = points[low];
            points[low] = temp;
        }
    }
    temp = points[initHigh];
    points[initHigh] = points[low];
    points[low] = temp;
    sort(points, initLow, low - 1);
    sort(points, low + 1, initHigh);
}

var objects = getMatching({type: "path", selected: true});
if (objects != null) {
    for (var i = 0; i < objects.length; i++) {
        var obj = objects[i];
        var ptsObj = obj.clone();
        ptsObj.curvesToPoints(1, 100000);
        var points = new Array();
        var length = ptsObj.segments.length;
        for (var j = 0; j < length; j++) {
            var pt = ptsObj.segments[j].point;
            // don't add double points:
            var add = true;
/*
            for (var k = 0; k < points.length; k++) {
                if (points[k].equals(pt)) {
                    add = false;
                    break;
                }
            }
*/
            if (add) points.push(pt);
        }
        var length = points.length;
        // find lowest:
        var firstPt = points[0]
        for (var j = 1; j < length; j++) {
            var pt = points[j];
            if ((firstPt.y < pt.y) || ((pt.y == firstPt.y) && (pt.x > firstPt.x))) {
                points[0] = pt;
                points[j] = firstPt;
                firstPt = pt;
            }
        }
        // sort the points
        sort(points, 1, length - 1)
//        print(points);
        // main graham algorithm
        var pointStack = new Array(points[1], points[0]);
        var j = 2;
        var trapped = false;
        while (j < length) {
            if (areaSign(pointStack[0], pointStack[1], points[j]) > 0) { // ungenaugikeiten...
                pointStack.unshift(points[j++]);
            } else {
                if (pointStack.length > 2) pointStack.shift();
                else {
                    if (trapped) {
                        j++;
                        trapped = false;
                    } else trapped = true;
                }
            }
        }
        var res = new Art("path");
        var length = pointStack.length;
        for (var j = 0; j < length; j++) {
            res.segments.push(pointStack[j]);
        }
      //  ptsObj.remove();
        redraw();
    }
}
