package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.Vertex;

/**
 * Graph edge implementation
 * 
 * @author Artem Polyvyanyy
 */
public class Edge extends AbstractEdge<Vertex>
{
	@SuppressWarnings("unchecked")
	protected Edge(AbstractMultiGraph g, Vertex v1, Vertex v2) {
		super(g, v1, v2);
	}	
}
