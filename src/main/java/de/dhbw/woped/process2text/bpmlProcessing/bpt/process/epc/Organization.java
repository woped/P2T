package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.IOrganization;
import de.hpi.bpt.process.epc.NonFlowObject;
import de.hpi.bpt.process.epc.NonFlowObjectType;

/**
 * EPC organization implementation
 * @author Artem Polyvyanyy
 *
 */
public class Organization extends NonFlowObject implements IOrganization {

	public Organization() {
		super();
	}

	public Organization(String name, String desc) {
		super(name, desc);
	}

	public Organization(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.process.epc.meta.NonFlowObject#getType()
	 */
	@Override
	public NonFlowObjectType getType() {
		return NonFlowObjectType.ORGANIZATION;
	}
}