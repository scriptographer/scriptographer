////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	button: {
		type: 'button', label: 'Button',
		value:'Click Me',
		onClick: function() {
			print('You clicked me!');
		}
	}
};

var palette = new Palette('Button Component', components);
