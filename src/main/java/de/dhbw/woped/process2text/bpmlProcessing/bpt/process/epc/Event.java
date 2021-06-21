package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.FlowObject;
import de.hpi.bpt.process.epc.FlowObjectType;
import de.hpi.bpt.process.epc.IEvent;

/**
 * EPC event implementation
 * 
 * @author Artem Polyvyanyy
 */
public class Event extends FlowObject implements IEvent {

	public Event() {
		super();
	}

	public Event(String name, String desc) {
		super(name, desc);
	}

	public Event(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.process.epc.flow.FlowObject#getType()
	 */
	@Override
	public FlowObjectType getType() {
		return FlowObjectType.EVENT;
	}

}
