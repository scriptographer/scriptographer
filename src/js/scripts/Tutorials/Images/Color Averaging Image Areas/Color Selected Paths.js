// http://scriptographer.org/tutorials/images/color-averaging-image-areas/

var rasters = document.getItems({ 
    type: Raster, 
    selected: true 
}); 
 
var paths = document.getItems({ 
    type: Path, 
    selected: true 
}); 
// if the user has selected an image and at least one path 
if(rasters.length > 0 && paths.length > 0) { 
    var raster = rasters[0]; 
    // loop through the paths 
    for(var i = 0; i < paths.length; i++) { 
        var path = paths[i]; 
        var color = raster.getAverageColor(path); 
        path.fillColor = color; 
    } 
} else { 
    Dialog.alert('Please select an image and at least one path'); 
}