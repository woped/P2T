package de.hpi.bpt.process.epc;

/**
 * EPC flow object types
 * 
 * @author Artem Polyvyanyy
 */
public enum FlowObjectType {
	FUNCTION,
	EVENT,
	CONNECTOR,
	PROCESS_INTERFACE,
	UNDEFINED;
	
	/**
	 * Get a flow object type
	 * @param obj Flow object
	 * @return Type of the object
	 */
	public static de.hpi.bpt.process.epc.FlowObjectType getType(IFlowObject obj) {
		if (obj instanceof IFunction) {
			return FUNCTION;
		}
		else if (obj instanceof IEvent) {
			return EVENT;
		}
		else if (obj instanceof IConnector) {
			return CONNECTOR;
		}
		else if (obj instanceof IProcessInterface) {
			return PROCESS_INTERFACE;
		}
		
		return UNDEFINED;
	}
	
}
