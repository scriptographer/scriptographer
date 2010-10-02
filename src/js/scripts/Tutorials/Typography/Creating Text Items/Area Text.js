// http://scriptographer.org/tutorials/typography/creating-text-items/#area-text

var topLeft = new Point(50, 100); 
var size = new Size(100, 50); 
var rectangle = new Rectangle(topLeft, size); 
 
var textItem = new AreaText(rectangle); 
textItem.content = 'This text runs within the shape of the path.';