package de.hpi.bpt.process.epc;


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
			return SYSTEM;
		}
		else if (obj instanceof IDocument) {
			return DOCUMENT;
		}
		else if (obj instanceof IApplicationSystem) {
			return APPLICATION_SYSTEM;
		}
		else if (obj instanceof IOrganization) {
			return ORGANIZATION;
		}
		else if (obj instanceof IRole) {
			return ROLE;
		}
		else if (obj instanceof IOrganizationType) {
			return ORGANIZATION_TYPE;
		}
		
		return UNDEFINED;
	}
}
