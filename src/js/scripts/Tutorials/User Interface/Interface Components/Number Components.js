////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	number1: {
		type: 'number', label: 'Number 1',
		value: 1
	},
	number2: {
		type: 'number', label: 'Number 2',
		value: 2.5,  fractionDigits: 1, increment: 0.5
	},
	number3: {
		type: 'number', label: 'Number 3',
		value: 5, range: [0, 10],
		steppers: true, units: 'point'
	}
};

var palette = new Palette('Number Components', components);
