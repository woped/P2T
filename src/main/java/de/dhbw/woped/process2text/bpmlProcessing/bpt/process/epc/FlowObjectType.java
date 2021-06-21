package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.IFlowObject;

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
			return de.hpi.bpt.process.epc.FlowObjectType.FUNCTION;
		}
		else if (obj instanceof IEvent) {
			return de.hpi.bpt.process.epc.FlowObjectType.EVENT;
		}
		else if (obj instanceof IConnector) {
			return de.hpi.bpt.process.epc.FlowObjectType.CONNECTOR;
		}
		else if (obj instanceof IProcessInterface) {
			return de.hpi.bpt.process.epc.FlowObjectType.PROCESS_INTERFACE;
		}
		
		return de.hpi.bpt.process.epc.FlowObjectType.UNDEFINED;
	}
	
}
