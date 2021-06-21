package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;


import de.hpi.bpt.process.epc.INonFlowObject;
import de.hpi.bpt.process.epc.Node;
import de.hpi.bpt.process.epc.NonFlowObjectType;

/**
 * Abstract non flow object implementation
 * @author Artem Polyvyanyy
 *
 */
public abstract class NonFlowObject extends Node implements INonFlowObject {

	public NonFlowObject() {
		super();
	}

	public NonFlowObject(String name, String desc) {
		super(name, desc);
	}

	public NonFlowObject(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.process.epc.meta.INonFlowObject#getType()
	 */
	public NonFlowObjectType getType() {
		return NonFlowObjectType.UNDEFINED;
	}
}
