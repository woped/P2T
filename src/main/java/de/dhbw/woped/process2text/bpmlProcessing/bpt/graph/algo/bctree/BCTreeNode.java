package de.dhbw.woped.process2text.bpmlProcessing.bpt.graph.algo.bctree;

import de.hpi.bpt.graph.abs.IEdge;
import de.hpi.bpt.graph.algo.bctree.BCTComponent;
import de.hpi.bpt.graph.algo.bctree.BCType;
import de.hpi.bpt.hypergraph.abs.IVertex;

import java.util.Collection;
import java.util.Vector;

public class BCTreeNode<E extends IEdge<V>, V extends IVertex> {
	private BCType nodeType;
	
	private de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V> parentNode;
	private Vector<de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V>> childNodes;
	
	private BCTComponent<E,V> graph;
	private V point;
	
	
	public BCTreeNode(BCTComponent<E,V> g) {
		this.parentNode = null;
		this.childNodes = new Vector<de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V>>();

		this.graph = g;
		this.point = null;
		
		this.nodeType = BCType.B;
	}
	
	public BCTreeNode(V p) {
		parentNode = null;
		childNodes = new Vector<de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V>>();

		this.graph = null;
		this.point = p;
		
		this.nodeType = BCType.C;
	}
	
	public BCType getNodeType() {
		return nodeType;
	}
	
	public de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V> getParentNode() {
		return this.parentNode;
	}
	
	public BCTComponent<E,V> getGraph() {
		return this.graph;
	}
	
	public V getPoint() {
		return this.point;
	}
	
	public Collection<de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V>> getChildren() {
		return this.childNodes;
	}
	
	public void addChild(de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V> node) {
		this.childNodes.add(node);
	}
	
	public void removeChild(de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V> node) {
		this.childNodes.remove(node);
	}
	
	public void setParent(de.hpi.bpt.graph.algo.bctree.BCTreeNode<E,V> node) {
		this.parentNode = node;
	}
}
