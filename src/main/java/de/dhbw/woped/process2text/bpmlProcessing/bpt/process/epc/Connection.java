package de.dhbw.woped.process2text.bpmlProcessing.bpt.process.epc;

import de.hpi.bpt.graph.abs.AbstractDirectedEdge;
import de.hpi.bpt.hypergraph.abs.AbstractGraphNotifier;
import de.hpi.bpt.process.epc.IConnection;
import de.hpi.bpt.process.epc.Node;

/**
 * EPC connection implementation
 * 
 * @author Artem Polyvyanyy
 */
public class Connection extends AbstractDirectedEdge<Node> implements IConnection<Node> {
	
	@SuppressWarnings("unchecked")
	protected Connection(AbstractGraphNotifier g, Node source, Node target) {
		super(null, source, target);
		this.source = source;
		this.target = target;
	}
	
	protected Connection (Node source, Node target) {
		super(null, source, target);
		this.source = source;
		this.target = target;
	}
}
