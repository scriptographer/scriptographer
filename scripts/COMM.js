portList = Packages.javax.comm.CommPortIdentifier.getPortIdentifiers();

// print out all serial ports:
while (portList.hasMoreElements()) {
	portId = portList.nextElement();
	if (portId.getPortType() == Packages.javax.comm.CommPortIdentifier.PORT_SERIAL) {
		print(portId.name);
	} 
}