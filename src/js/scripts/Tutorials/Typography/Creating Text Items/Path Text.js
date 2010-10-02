// http://scriptographer.org/tutorials/typography/creating-text-items/#path-text

var position = new Point(100, 100); 
var path = new Path.Circle(position, 25); 
 
var textItem = new PathText(path); 
textItem.content = 'This text runs along the path.';