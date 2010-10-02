// http://scriptographer.org/tutorials/paths/using-color-and-style/#working-with-the-current-style-of-the-document

// Change the current style of the document: 
document.currentStyle = { 
	strokeColor: '#000000', 
	fillColor: '#ff0000', 
	strokeWidth: 3 
};

// This path will inherit the styles we just set: 
var firstPath = new Path.Circle(new Point(125, 50), 20); 

// Change the current stroke width of the document: 
document.currentStyle.strokeWidth = 6; 

// This path will have it's strokeWidth set to 6: 
var secondPath = new Path.Circle(new Point(175, 50), 20);