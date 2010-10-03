////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/paths/working-with-path-items/#closing-paths

var myPath = new Path(); 
myPath.add(new Point(40, 40)); 
myPath.add(new Point(90, 90)); 
myPath.add(new Point(140, 40));
myPath.closed = true;