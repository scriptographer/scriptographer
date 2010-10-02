var paths = document.getItems(Path, { selected: true });
var rasters = document.getItems(Raster, { selected: true });
var failed;

if (!rasters.length || !paths.length) {
	Dialog.alert('Please select an image with paths on top of it\n' +
	'and execute the script again.\n\nColorizer will colorize the paths according to\n' + 
	'the average color of the pixels behind them.');
} else {
	var raster = rasters.first;
	
	for (var i = 0, l = paths.length; i < l; i++) {
		var path = paths[i];
		if (path.bounds.intersects(raster.bounds)) {
			path.fillColor = raster.getAverageColor(path);
		} else {
			failed = true;
		}
	}
	if (failed) {
		Dialog.alert('Colorizer colorizes paths according to\n' + 
		'the average color of the pixels behind them.\n\n' +
		'Some path items were not on top of the image\n' +
		'and weren\'t colored.');
	}
}