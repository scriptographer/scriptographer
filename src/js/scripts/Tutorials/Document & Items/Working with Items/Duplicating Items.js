////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/document-items/working-with-items/#duplicating-items

var circlePath = new Path.Circle(new Point(50, 50), 25); 

// clone the path and store it in a variable 
var clonedPath = circlePath.clone(); 

// move the cloned path 50pt to the right: 
clonedPath.position += new Point(50, 0); 

// select the cloned path 
clonedPath.selected = true;