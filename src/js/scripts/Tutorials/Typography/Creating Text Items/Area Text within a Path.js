// http://scriptographer.org/tutorials/typography/creating-text-items/#area-text

var position = new Point(100, 100); 
var path = new Path.Circle(position, 50); 
 
var textItem = new AreaText(path); 
textItem.content = 'This text runs within the shape of the path.';