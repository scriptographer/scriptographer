////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/paths/creating-predefined-shapes/#rectangular-shaped-paths-with-rounded-corners

var rectangle = new Rectangle(new Point(50, 50), new Point(150, 100)); 
var cornerSize = new Size(10, 10); 
var path = new Path.RoundRectangle(rectangle, cornerSize);