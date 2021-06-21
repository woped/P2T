package de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.algo.tctree;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.IEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.IVertex;

import java.util.LinkedList;

/**
 * This EdgeList is an abstraction of the underlying list type, which stores edges.
 * 
 * @author Christian Wiggert
 *
 * @param <E> Edge class
 * @param <V> Vertex class
 */
public class EdgeList<E extends IEdge<V>, V extends IVertex> extends LinkedList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2649534465829537370L;

	public EdgeList(E edge) {
		super();
		this.add(edge);
	}
	
	public EdgeList() {
		super();
	}
}
