package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.engine;

import de.hpi.bpt.hypergraph.abs.Vertex;

import java.util.Set;

/***
 * 
 * @author Artem Polyvyanyy
 *
 */
public interface IProcess {
	public boolean isTerminated();
	public Set<Vertex> getEnabledElements();
	public boolean fire(Vertex t);
	public void serialize();
}
