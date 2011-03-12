////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	slider: {
		type: 'slider', label: 'Slider',
		value: 5, range: [0, 25]
	}
};

var palette = new Palette('Slider Component', components);
