package de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.AbstractDirectedHyperEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.AbstractMultiDirectedHyperGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.Vertex;

/**
 * Directed hyper edge implementation
 * Directed hyper edge is two typed sets of vertices: source and target vertices
 * 
 * @author Artem Polyvyanyy
 */
public class DirectedHyperEdge extends AbstractDirectedHyperEdge<Vertex>
{
	@SuppressWarnings("unchecked")
	protected DirectedHyperEdge(AbstractMultiDirectedHyperGraph g) {
		super(g);
	}
}
