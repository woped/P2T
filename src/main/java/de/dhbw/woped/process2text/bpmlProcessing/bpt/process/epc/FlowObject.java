package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;


import de.hpi.bpt.process.epc.FlowObjectType;
import de.hpi.bpt.process.epc.IFlowObject;
import de.hpi.bpt.process.epc.Node;

/**
 * EPC flow object implementation
 * 
 * @author Artem Polyvyanyy
 */
public abstract class FlowObject extends Node implements IFlowObject {

	public FlowObject() {
		super();
	}

	public FlowObject(String name, String desc) {
		super(name, desc);
	}

	public FlowObject(String name) {
		super(name);
	}

	public FlowObjectType getType() {
		return FlowObjectType.UNDEFINED;
	}
}
