package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IDirectedEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IDirectedGraph;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex;

public class ReflexiveTransitiveClosure<E extends IDirectedEdge<V>,V extends IVertex> extends TransitiveClosure<E, V> {

	public ReflexiveTransitiveClosure(IDirectedGraph<E, V> g) {
		super(g);
	}
	
	@Override
	protected void calculateMatrix() {
		super.calculateMatrix();
		
		for (int i=0; i<this.verticesAsList.size(); i++) {
			this.matrix[i][i] = true;
		}
	}
}


