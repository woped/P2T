package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.epc;


import de.hpi.bpt.process.epc.IApplicationSystem;
import de.hpi.bpt.process.epc.NonFlowObject;
import de.hpi.bpt.process.epc.NonFlowObjectType;

/**
 * EPC application system implementation
 * 
 * @author Artem Polyvyanyy
 */
public class ApplicationSystem extends NonFlowObject implements IApplicationSystem {

	public ApplicationSystem() {
		super();
	}

	public ApplicationSystem(String name, String desc) {
		super(name, desc);
	}

	public ApplicationSystem(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.process.epc.meta.NonFlowObject#getType()
	 */
	@Override
	public NonFlowObjectType getType() {
		return NonFlowObjectType.APPLICATION_SYSTEM;
	}
}