function show(obj, index) {
    var child = obj.firstChild;
    while (child != null) {
        var str = "";
        for (var i = 0; i <= index; i++) str += "    ";
        print(str + child);
        show(child, index + 1);
        child = child.nextSibling;
    }
}

for (var i = 0; i < layers.length; i++) {
    var layer = layers[i];
    print(layer.title);
    show(layer, 0);
}