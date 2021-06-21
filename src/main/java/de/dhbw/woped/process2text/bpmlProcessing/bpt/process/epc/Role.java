package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.IRole;
import de.hpi.bpt.process.epc.NonFlowObject;
import de.hpi.bpt.process.epc.NonFlowObjectType;

/**
 * EPC role implementation
 * @author Artem Polyvyanyy
 *
 */
public class Role extends NonFlowObject implements IRole {

	public Role() {
		super();
	}

	public Role(String name, String desc) {
		super(name, desc);
	}

	public Role(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.process.epc.meta.NonFlowObject#getType()
	 */
	@Override
	public NonFlowObjectType getType() {
		return NonFlowObjectType.ROLE;
	}
}