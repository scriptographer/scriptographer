////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/paths/creating-predefined-shapes/#regular-polygon-shaped-paths

// Create a triangle shaped path
var triangle = new Path.RegularPolygon(new Point(80, 100), 3, 50); 

// Create a decahedron shaped path  
var decahedron = new Path.RegularPolygon(new Point(200, 100), 10, 50);