////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/images/working-with-images-and-pixels/#colors-of-pixels

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

	for(var y = 0; y < raster.height; y++) { 
		for(var x = 0; x < raster.width; x++) { 
			var color = raster.getPixel(x, y); 
			var position = new Point(x, y) * gridSize; 
			var path = new Path.Circle(position, gridSize / 2); 
			path.fillColor = color; 
		} 
	}
} else { 
	Dialog.alert('Please select an image first!') 
}