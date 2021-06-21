package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiDirectedGraphFragment;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.Vertex;

import java.util.Collection;
import java.util.Iterator;

/**
 * 
 * @author Artem Polyvyanyy
 *
 */
public class DirectedGraphFragment extends AbstractMultiDirectedGraphFragment<DirectedEdge,Vertex> {

	/**
	 * Constructor
	 * @param parent Parent graph of the fragment
	 */
	public DirectedGraphFragment(DirectedGraph parent) {
		super(parent);
	}

	/*
	 * (non-Javadoc)
	 * @see de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractMultiDirectedGraphFragment#addEdge(de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex, de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex)
	 */
	@Override
	public DirectedEdge addEdge(Vertex s, Vertex t) {
		if (this.graph!=null && this.graph.areAdjacent(s, t)) {
			if (s == null || t == null) return null;
			Collection<DirectedEdge> es = this.getEdgesWithSourceAndTarget(s, t);
			if (es.size()>0) {
				Iterator<DirectedEdge> i = es.iterator();
				while (i.hasNext()) {
					DirectedEdge e = i.next();
					if (e.getVertices().size()==2)
						return null;
				}
			}

			DirectedEdge e = new DirectedEdge(this, s, t);
			return e;
		}	
		
		return null;
	}

}
