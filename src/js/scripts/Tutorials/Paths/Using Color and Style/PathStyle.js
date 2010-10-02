// http://scriptographer.org/tutorials/paths/using-color-and-style/#the-pathstyle-object

var firstPath = new Path.Circle(new Point(50, 50), 50); 
firstPath.strokeColor = '#ff0000'; 
print(firstPath.strokeColor); // { red: 1, green: 0.0, blue: 0.0 } 
 
var secondPath = new Path.Circle(new Point(150, 50), 50);

// secondPath doesn't have a strokeColor yet: 
print(secondPath.strokeColor); // null 

// Apply the style of firstPath to that of secondPath: 
secondPath.style = firstPath.style;

// Now secondPath has the same strokeColor as firstPath:  
print(secondPath.strokeColor); // { red: 1, green: 0.0, blue: 0.0 }