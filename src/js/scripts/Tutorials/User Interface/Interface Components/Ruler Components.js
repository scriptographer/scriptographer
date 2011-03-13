////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = { 
	number1: {
		type: 'number', label: 'Number 1',
		value: 1
	},
	ruler1: {
		type: 'ruler'
	},
	number2: {
		type: 'number', label: 'Number 2',
		range: [0, 10], units: 'point', steppers: true,
		value: 5
	},
	ruler2: {
		type: 'ruler', label: 'Ruler'
	},
	number3: {
		type: 'number', label: 'Number 3',
		value: 5, range: [0, 10],
		steppers: true, units: 'point'
	}
};

var palette = new Palette('Ruler Components', components);
