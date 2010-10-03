////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/paths/using-color-and-style/#swatches

var myPath = new Path(); 
myPath.add(new Point(20, 70)); 
myPath.add(new Point(40, 100)); 
myPath.add(new Point(100, 20)); 
 
var greenSwatch = document.swatches['CMYK Green']; 
 
myPath.fillColor = greenSwatch.color;