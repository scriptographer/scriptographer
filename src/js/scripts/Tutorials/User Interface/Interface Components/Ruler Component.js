////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = { 
	number1: {
		type: 'number', label: 'Number 1',
		value: 1
	},
	ruler: {
		type: 'ruler'
	},
	number2: {
		type: 'number', label: 'Number 2',
		value: 5, range: [0, 10],
		steppers: true, units: 'point'
	}
};

var palette = new Palette('Ruler Component', components);
