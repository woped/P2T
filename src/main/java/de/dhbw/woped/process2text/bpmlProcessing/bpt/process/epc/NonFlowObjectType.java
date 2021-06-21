package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.IApplicationSystem;
import de.hpi.bpt.process.epc.IDocument;
import de.hpi.bpt.process.epc.INonFlowObject;
import de.hpi.bpt.process.epc.IOrganization;
import de.hpi.bpt.process.epc.IOrganizationType;
import de.hpi.bpt.process.epc.IRole;
import de.hpi.bpt.process.epc.ISystem;

/**
 * EPC non flow object types
 * @author Artem Polyvyanyy
 *
 */
public enum NonFlowObjectType {
	SYSTEM,
	DOCUMENT,
	APPLICATION_SYSTEM,
	ORGANIZATION,
	ROLE,
	ORGANIZATION_TYPE,
	UNDEFINED;
	
	/**
	 * Get a non flow object type
	 * @param obj Non flow object
	 * @return Type of the object
	 */
	public static de.hpi.bpt.process.epc.NonFlowObjectType getType(INonFlowObject obj) {
		if (obj instanceof ISystem) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.SYSTEM;
		}
		else if (obj instanceof IDocument) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.DOCUMENT;
		}
		else if (obj instanceof IApplicationSystem) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.APPLICATION_SYSTEM;
		}
		else if (obj instanceof IOrganization) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.ORGANIZATION;
		}
		else if (obj instanceof IRole) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.ROLE;
		}
		else if (obj instanceof IOrganizationType) {
			return de.hpi.bpt.process.epc.NonFlowObjectType.ORGANIZATION_TYPE;
		}
		
		return de.hpi.bpt.process.epc.NonFlowObjectType.UNDEFINED;
	}
}
