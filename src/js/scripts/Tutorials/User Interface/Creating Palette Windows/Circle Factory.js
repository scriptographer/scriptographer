////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/creating-palette-windows/

// Define the default values, to be passed to the
// new Palette() constructor
var values = {
	radius: 50,
	color: new RGBColor(1, 0.5, 0)
};

// Define the palette components
var components = {
	radius: { 
		type: 'number', label: 'Circle Radius', 
		range: [10, 100],
		units: 'point'
	},
	color: { 
		type: 'color', label: 'Circle Color'
	},

	// The newly added create button
	create: {
		type: 'button', value: 'Create',
		onClick: function() {
			// We use similar functionality as in the previous
			// example, with the added randomized center point.
			var myCircle = new Path.Circle(
				Point.random() * document.size, values.radius);
			myCircle.fillColor = values.color;
		}
	}
};

// Now create the palette window
var palette = new Palette('Circle Factory', components, values);
