package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.rpst;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractDirectedEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiDirectedGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IDirectedEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex;

public class RPSTEdge<E extends IDirectedEdge<V>, V extends IVertex> extends AbstractDirectedEdge<RPSTNode<E,V>> {

	@SuppressWarnings("unchecked")
	protected RPSTEdge(AbstractMultiDirectedGraph g, RPSTNode source, RPSTNode target) {
		super(g, source, target);
	}
}
