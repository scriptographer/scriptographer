// http://scriptographer.org/tutorials/interaction/creating-mouse-tools/#maximum-distance

tool.maxDistance = 5; 
 
function onMouseDrag(event) { 
	var radius = event.delta.length / 2; 
	new Path.Circle(event.middlePoint, radius); 
}