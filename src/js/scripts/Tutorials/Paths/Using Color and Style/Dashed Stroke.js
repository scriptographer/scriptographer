////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/paths/using-color-and-style/#dashed-stroke

var myPath = new Path(); 
myPath.add(new Point(20, 70)); 
myPath.add(new Point(40, 100)); 
myPath.add(new Point(100, 20)); 
 
myPath.strokeColor = '#ff0000'; 
myPath.strokeWidth = 5; 
 
myPath.dashArray = [3, 3, 5, 3];