// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#minimum-distance

tool.minDistance = 50; 
 
function onMouseDrag(event) { 
	var radius = event.delta.length / 2; 
	new Path.Circle(event.middlePoint, radius); 
}