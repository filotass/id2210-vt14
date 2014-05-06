package system.peer;

import common.simulation.RequestResource;
import common.simulation.ResponseResource;
import se.sics.kompics.PortType;

public class RmPort extends PortType {{
	negative(RequestResource.class);
	positive(ResponseResource.class);
}}
