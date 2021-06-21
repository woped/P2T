package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.bctree;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiGraphFragment;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex;

public class BCTComponent<E extends IEdge<V>, V extends IVertex> extends AbstractMultiGraphFragment<E, V> {

	public BCTComponent(IGraph<E, V> g) {
		super(g);
	}

}
