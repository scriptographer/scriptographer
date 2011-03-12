////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	string: {
		type: 'string', label: 'String', length: 40,
		value: 'This is a long string that needs more space than usual'
	}
};

var palette = new Palette('String Component', components);
