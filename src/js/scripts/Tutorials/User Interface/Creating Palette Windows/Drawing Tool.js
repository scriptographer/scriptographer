////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/creating-palette-windows/

var components = { 
    threshold: {
		type: 'number', label: 'Minimum Distance',
		units: 'point', value: 0,
		onChange: function(value) {
			print('Minimum Distance was changed to', value);
			tool.minDistance = value;
		}
    }
};

var palette = new Palette('Drawing Tool', components);

var path;

function onMouseDown(event) {
	path = new Path();
	path.add(event.point);
}

function onMouseDrag(event) {
	path.add(event.point);
}
