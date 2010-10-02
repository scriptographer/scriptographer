// http://scriptographer.org/tutorials/images/color-averaging-image-areas/#and-now-for-some-mouse-interaction

var raster; 
tool.distanceThreshold = 5; 
 
function onMouseDown(event) { 
    var rasters = document.getItems({ 
        type: Raster, 
        selected: true 
    }); 
    if(rasters.length > 0) { 
        raster = rasters[0]; 
    } else { 
        raster = null; 
        Dialog.alert('Please select an image first!') 
    } 
} 
 
function onMouseDrag(event) { 
    if(raster) { 
        var radius = event.delta.length / 2; 
        var path = new Path.Circle(event.point, 5); 
        var color = raster.getAverageColor(path); 
        path.fillColor = color; 
    } 
}