package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.epc;


import de.hpi.bpt.process.epc.INode;
import de.hpi.bpt.process.epc.NonFlowObjectType;

/**
 * EPC non flow object interface
 * @author Artem Polyvyanyy
 *
 */
public interface INonFlowObject extends INode {
	
	/**
	 * Get non flow object type
	 * @return Non flow object type
	 */
	public NonFlowObjectType getType();
}
