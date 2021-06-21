package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.tctree;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractDirectedEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiDirectedGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex;

/**
 * 
 * @author Artem Polyvyanyy
 *
 */
public class TCTreeEdge<E extends IEdge<V>, V extends IVertex> extends AbstractDirectedEdge<TCTreeNode<E,V>> {

	@SuppressWarnings("unchecked")
	protected TCTreeEdge(AbstractMultiDirectedGraph g, TCTreeNode source, TCTreeNode target) {
		super(g, source, target);
	}
}
