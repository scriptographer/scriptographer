// http://scriptographer.org/tutorials/document-items/transforming-items/#scaling-an-item-around-a-center-point

// Let's make a path and then scale it by 50% from {x: 0, y: 0}:
var circlePath = new Path.Circle(new Point(50, 50), 25); 
circlePath.scale(0.5, new Point(0, 0));