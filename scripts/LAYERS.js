for (var i in documents) {
    var doc = documents[i];
    print("---" + doc + "---");
    for (var j in doc.layers) {
        var layer = doc.layers[j];
        print(layer);
    }
}