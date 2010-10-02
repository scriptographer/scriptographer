// find all selected images in the document 
var rasters = document.getItems({ 
	type: Raster, 
	selected: true 
});

// check if any images were selected 
if(rasters.length > 0) { 
	// get the first raster in the rasters array 
	var raster = rasters[0]; 

	var gridSize = 10; 
	var rectSize = new Size(gridSize / 2, gridSize); 

	for(var x = 0; x < raster.width; x++) { 
		for(var y = 0; y < raster.height; y++) { 
			var color = raster.getPixel(x, y); 
			var position = new Point(x, y) * gridSize; 
			var radius = (gridSize / 2) * color.gray; 
			var path = new Path.Rectangle(position, rectSize); 
			path.scale(color.gray); 
			var rotation = 180 * color.gray; 
			path.rotate(rotation); 
		} 
	}
} else { 
	Dialog.alert('Please select an image first!') 
}