// http://scriptographer.org/tutorials/document-items/transforming-items/#rotating-items

var path = new Path.Rectangle(new Point(50, 50), new Size(100, 50)); 
path.rotate(-45, path.bounds.bottomLeft);