////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	button: {
		type: 'list', label: 'List',
		value:'Two',
		options: ['One', 'Two', 'Three']
	}
};

var palette = new Palette('List Component', components);
