package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.epc;

import de.hpi.bpt.process.epc.IFlowObject;

/**
 * EPC function interface
 *
 * @author Artem Polyvyanyy
 */
public interface IFunction extends IFlowObject {
	
	/**
	 * Get function duration in milliseconds
	 * @return Function duration
	 */
	public long getDuration();
	
	/**
	 * Set function duration
	 * @param duration Duration in milliseconds
	 */
	public void setDuration(long duration);
}
