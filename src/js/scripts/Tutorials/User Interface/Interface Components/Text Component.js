////////////////////////////////////////////////////////////////////////////////
// This script belongs to the following tutorial:
//
// http://scriptographer.org/tutorials/user-interface/interface-components/

var components = {
	text: {
		type: 'text', label: 'Text',
		value: 'This is a readonly text\nwith two lines of text.'
	}
};

var palette = new Palette('Text Component', components);
